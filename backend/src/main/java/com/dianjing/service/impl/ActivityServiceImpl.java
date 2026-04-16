package com.dianjing.service.impl;

import com.dianjing.common.BusinessException;
import com.dianjing.common.Constants;
import com.dianjing.dto.request.ActivityCreateRequest;
import com.dianjing.dto.response.ActivityResponse;
import com.dianjing.entity.Activity;
import com.dianjing.entity.ActivityServiceItem;
import com.dianjing.mapper.ActivityMapper;
import com.dianjing.mapper.ActivityServiceItemMapper;
import com.dianjing.mapper.ServiceMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ActivityServiceImpl implements com.dianjing.service.ActivityService {

    private final ActivityMapper activityMapper;
    private final ActivityServiceItemMapper activityServiceItemMapper;
    private final ServiceMapper serviceMapper;

    public ActivityServiceImpl(ActivityMapper activityMapper, ActivityServiceItemMapper activityServiceItemMapper, ServiceMapper serviceMapper) {
        this.activityMapper = activityMapper;
        this.activityServiceItemMapper = activityServiceItemMapper;
        this.serviceMapper = serviceMapper;
    }

    @Override
    @Transactional
    public Activity create(Long adminId, ActivityCreateRequest request) {
        validateActivityRequest(request);

        Activity activity = new Activity();
        activity.setTitle(request.getTitle());
        activity.setDescription(request.getDescription());
        activity.setType(request.getType());
        activity.setDiscountRate(request.getDiscountRate());
        activity.setStartTime(request.getStartTime());
        activity.setEndTime(request.getEndTime());
        activity.setImage(request.getImage());
        activity.setCreatedBy(adminId);

        LocalDateTime now = LocalDateTime.now();
        if (request.getStartTime().isAfter(now)) {
            activity.setStatus(Constants.ACTIVITY_STATUS_NOT_STARTED);
        } else if (request.getStartTime().isBefore(now) && request.getEndTime().isAfter(now)) {
            activity.setStatus(Constants.ACTIVITY_STATUS_ACTIVE);
        } else {
            throw new BusinessException(400, "结束时间不能早于当前时间");
        }

        Activity saved = activityMapper.save(activity);

        if (request.getType() != Constants.ACTIVITY_TYPE_GLOBAL_DISCOUNT && request.getServices() != null) {
            for (ActivityCreateRequest.ActivityServiceItem item : request.getServices()) {
                validateServiceItem(item, request.getType());
                ActivityServiceItem as = new ActivityServiceItem();
                as.setActivityId(saved.getId());
                as.setServiceId(item.getServiceId());
                if (request.getType() == Constants.ACTIVITY_TYPE_SERVICE_SPECIAL_PRICE) {
                    as.setSpecialPrice(item.getSpecialPrice());
                }
                activityServiceItemMapper.save(as);
            }
        }

        return saved;
    }

    @Override
    @Transactional
    public Activity update(Long adminId, Long id, ActivityCreateRequest request) {
        Activity activity = activityMapper.findById(id)
                .orElseThrow(() -> new BusinessException(404, "活动不存在"));

        validateActivityRequest(request);

        activity.setTitle(request.getTitle());
        activity.setDescription(request.getDescription());
        activity.setType(request.getType());
        activity.setDiscountRate(request.getDiscountRate());
        activity.setStartTime(request.getStartTime());
        activity.setEndTime(request.getEndTime());
        activity.setImage(request.getImage());

        activityServiceItemMapper.deleteByActivityId(id);

        if (request.getType() != Constants.ACTIVITY_TYPE_GLOBAL_DISCOUNT && request.getServices() != null) {
            for (ActivityCreateRequest.ActivityServiceItem item : request.getServices()) {
                validateServiceItem(item, request.getType());
                ActivityServiceItem as = new ActivityServiceItem();
                as.setActivityId(id);
                as.setServiceId(item.getServiceId());
                if (request.getType() == Constants.ACTIVITY_TYPE_SERVICE_SPECIAL_PRICE) {
                    as.setSpecialPrice(item.getSpecialPrice());
                }
                activityServiceItemMapper.save(as);
            }
        }

        return activityMapper.save(activity);
    }

    @Override
    @Transactional
    public void delete(Long adminId, Long id) {
        if (!activityMapper.existsById(id)) {
            throw new BusinessException(404, "活动不存在");
        }
        activityServiceItemMapper.deleteByActivityId(id);
        activityMapper.deleteById(id);
    }

    @Override
    @Transactional
    public Activity publish(Long adminId, Long id) {
        Activity activity = activityMapper.findById(id)
                .orElseThrow(() -> new BusinessException(404, "活动不存在"));
        if (activity.getStatus() == Constants.ACTIVITY_STATUS_ACTIVE) {
            throw new BusinessException(400, "活动已上线");
        }
        activity.setStatus(Constants.ACTIVITY_STATUS_ACTIVE);
        return activityMapper.save(activity);
    }

    @Override
    @Transactional
    public Activity end(Long adminId, Long id) {
        Activity activity = activityMapper.findById(id)
                .orElseThrow(() -> new BusinessException(404, "活动不存在"));
        activity.setStatus(Constants.ACTIVITY_STATUS_ENDED);
        return activityMapper.save(activity);
    }

    @Override
    public Activity getById(Long id) {
        return activityMapper.findById(id)
                .orElseThrow(() -> new BusinessException(404, "活动不存在"));
    }

    @Override
    public ActivityResponse getDetailResponse(Long id) {
        Activity activity = getById(id);
        return convertToResponse(activity);
    }

    @Override
    public Page<Activity> getAll(Pageable pageable) {
        return activityMapper.findAllByOrderByIdDesc(pageable);
    }

    @Override
    public List<ActivityResponse> getActiveActivities() {
        LocalDateTime now = LocalDateTime.now();
        List<Activity> activities = activityMapper.findActiveActivities(now);
        return activities.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getActivityPriceForService(Long serviceId, BigDecimal servicePrice) {
        LocalDateTime now = LocalDateTime.now();
        Map<String, Object> result = new HashMap<>();

        List<Activity> globalActivities = activityMapper.findActiveGlobalActivities(now);
        if (!globalActivities.isEmpty()) {
            Activity bestGlobal = globalActivities.stream()
                    .min(Comparator.comparing(Activity::getDiscountRate))
                    .orElse(globalActivities.get(0));
            BigDecimal activityPrice = servicePrice.multiply(bestGlobal.getDiscountRate())
                    .setScale(2, java.math.RoundingMode.HALF_UP);
            result.put("activityId", bestGlobal.getId());
            result.put("activityTitle", bestGlobal.getTitle());
            result.put("activityPrice", activityPrice);
            result.put("activityDiscountRate", bestGlobal.getDiscountRate());
            result.put("activityType", bestGlobal.getType());
            return result;
        }

        List<Activity> serviceActivities = activityMapper.findActiveServiceActivities(now);
        // 收集所有匹配的活动，选择折扣最低（优惠最大）的
        Activity bestServiceActivity = null;
        BigDecimal bestActivityPrice = null;
        for (Activity activity : serviceActivities) {
            List<ActivityServiceItem> relations = activityServiceItemMapper.findByActivityId(activity.getId());
            for (ActivityServiceItem as : relations) {
                if (as.getServiceId().equals(serviceId)) {
                    BigDecimal activityPrice;
                    if (activity.getType() == Constants.ACTIVITY_TYPE_SERVICE_SPECIAL_PRICE && as.getSpecialPrice() != null) {
                        activityPrice = as.getSpecialPrice();
                    } else {
                        activityPrice = servicePrice.multiply(activity.getDiscountRate())
                                .setScale(2, java.math.RoundingMode.HALF_UP);
                    }
                    if (bestActivityPrice == null || activityPrice.compareTo(bestActivityPrice) < 0) {
                        bestActivityPrice = activityPrice;
                        bestServiceActivity = activity;
                    }
                    break;
                }
            }
        }
        if (bestServiceActivity != null) {
            result.put("activityId", bestServiceActivity.getId());
            result.put("activityTitle", bestServiceActivity.getTitle());
            result.put("activityPrice", bestActivityPrice);
            result.put("activityDiscountRate", bestServiceActivity.getDiscountRate());
            result.put("activityType", bestServiceActivity.getType());
            return result;
        }

        return result;
    }

    @Override
    @Transactional
    public void updateActivityStatuses() {
        LocalDateTime now = LocalDateTime.now();
        List<Activity> allActivities = activityMapper.findAll();
        for (Activity activity : allActivities) {
            if (activity.getStatus() == Constants.ACTIVITY_STATUS_ENDED) continue;
            if (activity.getStartTime().isAfter(now)) {
                activity.setStatus(Constants.ACTIVITY_STATUS_NOT_STARTED);
            } else if (activity.getEndTime().isBefore(now)) {
                activity.setStatus(Constants.ACTIVITY_STATUS_ENDED);
            } else {
                activity.setStatus(Constants.ACTIVITY_STATUS_ACTIVE);
            }
            activityMapper.save(activity);
        }
    }

    private void validateActivityRequest(ActivityCreateRequest request) {
        if (request.getEndTime().isBefore(request.getStartTime()) || request.getEndTime().isEqual(request.getStartTime())) {
            throw new BusinessException(400, "结束时间必须晚于开始时间");
        }

        if (request.getType() == Constants.ACTIVITY_TYPE_GLOBAL_DISCOUNT || request.getType() == Constants.ACTIVITY_TYPE_SERVICE_DISCOUNT) {
            if (request.getDiscountRate() == null) {
                throw new BusinessException(400, "折扣类型活动必须设置折扣率");
            }
            if (request.getDiscountRate().compareTo(BigDecimal.ZERO) <= 0 || request.getDiscountRate().compareTo(BigDecimal.ONE) > 0) {
                throw new BusinessException(400, "折扣率必须在0-1之间");
            }
        }

        if (request.getType() == Constants.ACTIVITY_TYPE_SERVICE_SPECIAL_PRICE) {
            if (request.getServices() == null || request.getServices().isEmpty()) {
                throw new BusinessException(400, "特价活动必须选择服务");
            }
        }
    }

    private void validateServiceItem(ActivityCreateRequest.ActivityServiceItem item, Integer type) {
        if (!serviceMapper.existsById(item.getServiceId())) {
            throw new BusinessException(404, "服务ID " + item.getServiceId() + " 不存在");
        }
        if (type == Constants.ACTIVITY_TYPE_SERVICE_SPECIAL_PRICE && item.getSpecialPrice() == null) {
            throw new BusinessException(400, "特价活动必须设置特价");
        }
        if (type == Constants.ACTIVITY_TYPE_SERVICE_SPECIAL_PRICE && item.getSpecialPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(400, "特价必须大于0");
        }
    }

    private ActivityResponse convertToResponse(Activity activity) {
        ActivityResponse response = new ActivityResponse();
        response.setId(activity.getId());
        response.setTitle(activity.getTitle());
        response.setDescription(activity.getDescription());
        response.setType(activity.getType());
        response.setDiscountRate(activity.getDiscountRate());
        response.setStartTime(activity.getStartTime());
        response.setEndTime(activity.getEndTime());
        response.setStatus(activity.getStatus());
        response.setImage(activity.getImage());

        if (activity.getType() != Constants.ACTIVITY_TYPE_GLOBAL_DISCOUNT) {
            List<ActivityServiceItem> relations = activityServiceItemMapper.findByActivityId(activity.getId());
            List<ActivityResponse.ActivityServiceItem> items = relations.stream().map(as -> {
                ActivityResponse.ActivityServiceItem item = new ActivityResponse.ActivityServiceItem();
                item.setServiceId(as.getServiceId());
                item.setSpecialPrice(as.getSpecialPrice());
                serviceMapper.findById(as.getServiceId()).ifPresent(s -> {
                    item.setServiceTitle(s.getTitle());
                    item.setServicePrice(s.getPrice());
                });
                return item;
            }).collect(Collectors.toList());
            response.setServices(items);
        }

        return response;
    }
}
