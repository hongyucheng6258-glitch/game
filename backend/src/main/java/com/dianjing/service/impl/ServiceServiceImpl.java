package com.dianjing.service.impl;

import com.dianjing.common.BusinessException;
import com.dianjing.common.Constants;
import com.dianjing.dto.request.ServiceCreateRequest;
import com.dianjing.dto.request.ServiceUpdateRequest;
import com.dianjing.dto.response.ServiceDetailResponse;
import com.dianjing.entity.Service;
import com.dianjing.entity.ServiceTagRelation;
import com.dianjing.entity.User;
import com.dianjing.mapper.OrderMapper;
import com.dianjing.mapper.ReviewMapper;
import com.dianjing.mapper.ServiceMapper;
import com.dianjing.mapper.ServiceTagMapper;
import com.dianjing.mapper.ServiceTagRelationMapper;
import com.dianjing.service.LevelService;
import com.dianjing.service.ServiceCacheService;
import com.dianjing.service.ServiceService;
import com.dianjing.service.SystemSettingService;
import com.dianjing.service.UserService;
import com.dianjing.service.ActivityService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class ServiceServiceImpl implements ServiceService {

    private final ServiceMapper serviceMapper;
    private final ServiceTagMapper serviceTagMapper;
    private final ServiceTagRelationMapper serviceTagRelationMapper;
    private final OrderMapper orderMapper;
    private final ReviewMapper reviewMapper;
    private final SystemSettingService systemSettingService;
    private final UserService userService;
    private final ServiceCacheService serviceCacheService;
    private final LevelService levelService;
    private final ActivityService activityService;

    public ServiceServiceImpl(ServiceMapper serviceMapper, ServiceTagMapper serviceTagMapper,
                              ServiceTagRelationMapper serviceTagRelationMapper, OrderMapper orderMapper,
                              ReviewMapper reviewMapper, SystemSettingService systemSettingService,
                              UserService userService, ServiceCacheService serviceCacheService,
                              LevelService levelService, ActivityService activityService) {
        this.serviceMapper = serviceMapper;
        this.serviceTagMapper = serviceTagMapper;
        this.serviceTagRelationMapper = serviceTagRelationMapper;
        this.orderMapper = orderMapper;
        this.reviewMapper = reviewMapper;
        this.systemSettingService = systemSettingService;
        this.userService = userService;
        this.serviceCacheService = serviceCacheService;
        this.levelService = levelService;
        this.activityService = activityService;
    }

    private void verifyRealNameAuthentication(Long providerId) {
        User provider = userService.getUserById(providerId);
        System.out.println("=== 实名认证验证 ===");
        System.out.println("用户ID: " + providerId);
        System.out.println("用户名: " + provider.getUsername());
        System.out.println("真实姓名: " + provider.getRealName());
        System.out.println("身份证号: " + provider.getIdCard());
        System.out.println("真实姓名是否为空: " + (provider.getRealName() == null || provider.getRealName().isEmpty()));
        System.out.println("身份证号是否为空: " + (provider.getIdCard() == null || provider.getIdCard().isEmpty()));
        System.out.println("====================");
        
        if (provider.getRealName() == null || provider.getRealName().isEmpty() || 
            provider.getIdCard() == null || provider.getIdCard().isEmpty()) {
            throw new BusinessException(400, "请先完成实名认证后再发布服务");
        }
    }

    @Override
    @Transactional
    public Service create(Long providerId, ServiceCreateRequest request) {
        verifyRealNameAuthentication(providerId);
        validatePrice(providerId, request.getPrice());
        
        Service service = new Service();
        service.setProviderId(providerId);
        service.setGameType(request.getGameType());
        service.setServiceType(request.getServiceType());
        service.setTitle(request.getTitle());
        service.setDescription(request.getDescription());
        service.setPrice(request.getPrice());
        service.setDuration(request.getDuration());
        service.setStatus(Constants.SERVICE_PENDING_REVIEW);
        service.setRating(java.math.BigDecimal.ZERO);
        service.setReviewCount(0);
        service.setSalesCount(0);
        Service createdService = serviceMapper.save(service);

        // 保存标签关联
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            for (Long tagId : request.getTagIds()) {
                if (serviceTagMapper.existsById(tagId)) {
                    ServiceTagRelation relation = new ServiceTagRelation();
                    relation.setServiceId(createdService.getId());
                    relation.setTagId(tagId);
                    serviceTagRelationMapper.save(relation);
                }
            }
        }
        
        // 清除推荐服务缓存
        serviceCacheService.evictRecommendedServices(request.getGameType());
        serviceCacheService.evictRecommendedServices(null);
        
        return createdService;
    }

    @Override
    @Transactional
    public Service getById(Long id) {
        Service cachedService = serviceCacheService.getCachedServiceDetail(id);
        if (cachedService != null) {
            return cachedService;
        }
        Service service = serviceMapper.findById(id)
                .orElseThrow(() -> new BusinessException(404, "服务不存在"));
        updateServiceStats(service);
        serviceCacheService.cacheServiceDetail(service);
        return service;
    }

    @Override
    public ServiceDetailResponse getDetailById(Long id) {
        com.dianjing.entity.Service service = getById(id);
        ServiceDetailResponse resp = ServiceDetailResponse.fromEntity(service);
        
        // 设置服务者信息
        try {
            User provider = userService.getUserById(service.getProviderId());
            resp.setProviderName(provider.getUsername());
            resp.setProviderAvatar(provider.getAvatar());
        } catch (Exception e) {
            // 服务者不存在时忽略
        }
        
        // 设置标签
        List<ServiceTagRelation> tagRelations = serviceTagRelationMapper.findByServiceId(service.getId());
        List<String> tagNames = tagRelations.stream()
                .map(rel -> serviceTagMapper.findById(rel.getTagId()).orElse(null))
                .filter(tag -> tag != null)
                .map(tag -> tag.getName())
                .collect(Collectors.toList());
        resp.setTags(tagNames);
        
        // 设置活动优惠信息
        java.util.Map<String, Object> activityInfo = activityService.getActivityPriceForService(service.getId(), service.getPrice());
        if (activityInfo.containsKey("activityId")) {
            resp.setActivityId((Long) activityInfo.get("activityId"));
            resp.setActivityTitle((String) activityInfo.get("activityTitle"));
            resp.setActivityPrice((java.math.BigDecimal) activityInfo.get("activityPrice"));
            resp.setActivityDiscountRate((java.math.BigDecimal) activityInfo.get("activityDiscountRate"));
            resp.setActivityType((Integer) activityInfo.get("activityType"));
        }
        
        return resp;
    }

    private void updateServiceStats(Service service) {
        long salesCount = orderMapper.countByServiceIdAndStatus(service.getId(), Constants.ORDER_COMPLETED);
        long reviewCount = reviewMapper.countByServiceId(service.getId());
        Double averageRating = reviewMapper.getAverageRatingByService(service.getId());
        
        service.setSalesCount((int) salesCount);
        service.setReviewCount((int) reviewCount);
        service.setRating(averageRating != null ? BigDecimal.valueOf(averageRating) : BigDecimal.ZERO);
        serviceMapper.save(service);
        
        serviceCacheService.cacheServiceDetail(service);
    }

    private void validatePrice(Long providerId, BigDecimal price) {
        BigDecimal minPrice = systemSettingService.getBigDecimal("minPrice", java.math.BigDecimal.ONE);
        BigDecimal systemMaxPrice = systemSettingService.getBigDecimal("maxPrice", new java.math.BigDecimal("999999"));
        
        if (price.compareTo(minPrice) < 0) {
            throw new BusinessException(400, "服务价格不能低于最低价格: " + minPrice + "元");
        }
        
        // 获取服务商等级信息，应用最高服务价格限制（等级特权）
        var levelInfo = levelService.getUserLevelInfo(providerId);
        BigDecimal maxServicePrice = levelInfo.getMaxServicePrice();
        
        if (price.compareTo(maxServicePrice) > 0) {
            throw new BusinessException(400, "服务价格不能高于您当前等级允许的最高价格: " + maxServicePrice + 
                    "元（当前等级: Lv." + levelInfo.getCurrentLevel() + " " + levelInfo.getCurrentLevelName() +
                    "，升级后可发布更高价格的服务）");
        }
        
        // 系统全局最高价作为兜底限制
        if (price.compareTo(systemMaxPrice) > 0) {
            throw new BusinessException(400, "服务价格不能高于系统最高价格: " + systemMaxPrice + "元");
        }
    }

    @Override
    @Transactional
    public Service update(Service service) {
        Service updatedService = serviceMapper.save(service);
        serviceCacheService.evictServiceDetail(service.getId());
        serviceCacheService.evictRecommendedServices(service.getGameType());
        serviceCacheService.evictRecommendedServices(null);
        return updatedService;
    }

    @Override
    @Transactional
    public Service update(Long providerId, Long id, ServiceUpdateRequest request) {
        Service service = serviceMapper.findById(id)
                .orElseThrow(() -> new BusinessException(404, "服务不存在"));
        if (!service.getProviderId().equals(providerId)) {
            throw new BusinessException(403, "无权操作此服务");
        }
        // 如果服务已上架或正在更新上架相关信息，需要验证实名认证
        if (service.getStatus() == Constants.SERVICE_ONLINE) {
            verifyRealNameAuthentication(providerId);
        }
        if (request.getGameType() != null) service.setGameType(request.getGameType());
        if (request.getServiceType() != null) service.setServiceType(request.getServiceType());
        if (request.getTitle() != null) service.setTitle(request.getTitle());
        if (request.getDescription() != null) service.setDescription(request.getDescription());
        if (request.getPrice() != null) {
            validatePrice(providerId, request.getPrice());
            service.setPrice(request.getPrice());
        }
        if (request.getDuration() != null) service.setDuration(request.getDuration());

        // 更新标签关联
        if (request.getTagIds() != null) {
            serviceTagRelationMapper.deleteByServiceId(id);
            for (Long tagId : request.getTagIds()) {
                if (serviceTagMapper.existsById(tagId)) {
                    ServiceTagRelation relation = new ServiceTagRelation();
                    relation.setServiceId(id);
                    relation.setTagId(tagId);
                    serviceTagRelationMapper.save(relation);
                }
            }
        }
        Service updatedService = serviceMapper.save(service);
        serviceCacheService.evictServiceDetail(id);
        serviceCacheService.evictRecommendedServices(service.getGameType());
        serviceCacheService.evictRecommendedServices(null);
        return updatedService;
    }

    @Override
    @Transactional
    public void delete(Long providerId, Long id) {
        Service service = getById(id);
        if (!service.getProviderId().equals(providerId)) {
            throw new BusinessException(403, "无权操作此服务");
        }
        serviceTagRelationMapper.deleteByServiceId(id);
        serviceMapper.delete(service);
        serviceCacheService.evictServiceDetail(id);
        serviceCacheService.evictRecommendedServices(service.getGameType());
        serviceCacheService.evictRecommendedServices(null);
    }

    @Override
    @Transactional
    public void toggleStatus(Long providerId, Long id) {
        Service service = serviceMapper.findById(id)
                .orElseThrow(() -> new BusinessException(404, "服务不存在"));
        if (!service.getProviderId().equals(providerId)) {
            throw new BusinessException(403, "无权操作此服务");
        }
        // 如果要上架服务，需要先验证实名认证
        if (service.getStatus() != Constants.SERVICE_ONLINE) {
            verifyRealNameAuthentication(providerId);
        }
        service.setStatus(service.getStatus() == Constants.SERVICE_ONLINE ? Constants.SERVICE_OFFLINE : Constants.SERVICE_ONLINE);
        serviceMapper.save(service);
        serviceCacheService.evictServiceDetail(id);
        serviceCacheService.evictRecommendedServices(service.getGameType());
        serviceCacheService.evictRecommendedServices(null);
    }

    @Override
    @Transactional
    public Page<com.dianjing.entity.Service> search(String keyword, String gameType, Integer serviceType, Integer status,
                                                   java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice,
                                                   String sort, Long providerId, Boolean includeAllStatus, Pageable pageable) {
        Sort finalSort = Sort.by(Sort.Order.desc("id"));
        if (sort != null) {
            switch (sort) {
                case "price_asc":
                    finalSort = Sort.by(Sort.Order.asc("price"));
                    break;
                case "price_desc":
                    finalSort = Sort.by(Sort.Order.desc("price"));
                    break;
                case "rating":
                    finalSort = Sort.by(Sort.Order.desc("rating"));
                    break;
                case "sales":
                    finalSort = Sort.by(Sort.Order.desc("salesCount"));
                    break;
                case "newest":
                    finalSort = Sort.by(Sort.Order.desc("id"));
                    break;
                default:
                    break;
            }
        }
        
        Pageable finalPageable = finalSort.isSorted() 
                ? PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), finalSort) 
                : pageable;
        
        Page<com.dianjing.entity.Service> services = serviceMapper.findAll((root, query, cb) -> {
            var predicates = cb.conjunction();
            if (providerId != null) {
                predicates = cb.and(predicates, cb.equal(root.get("providerId"), providerId));
            } else {
                if (includeAllStatus == null || !includeAllStatus) {
                    predicates = cb.and(predicates, cb.equal(root.get("status"), Constants.SERVICE_ONLINE));
                }
            }
            if (keyword != null && !keyword.isEmpty()) {
                predicates = cb.and(predicates, cb.or(
                    cb.like(root.get("title"), "%" + keyword + "%"),
                    cb.like(root.get("description"), "%" + keyword + "%")
                ));
            }
            if (gameType != null && !gameType.isEmpty()) {
                predicates = cb.and(predicates, cb.equal(root.get("gameType"), gameType));
            }
            if (serviceType != null) {
                predicates = cb.and(predicates, cb.equal(root.get("serviceType"), serviceType));
            }
            if (status != null) {
                predicates = cb.and(predicates, cb.equal(root.get("status"), status));
            }
            if (minPrice != null) {
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            return predicates;
        }, finalPageable);
        
        services.getContent().forEach(this::updateServiceStats);
        return services;
    }

    @Override
    @Transactional
    public Page<com.dianjing.entity.Service> getByProviderId(Long providerId, Pageable pageable) {
        Pageable sortedPageable = pageable.getSort().isSorted()
                ? pageable
                : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Order.desc("id")));
        Page<com.dianjing.entity.Service> services = serviceMapper.findAll((root, query, cb) ->
            cb.equal(root.get("providerId"), providerId), sortedPageable);
        
        services.getContent().forEach(this::updateServiceStats);
        return services;
    }

    @Override
    @Transactional
    public Page<com.dianjing.entity.Service> getOnlineServices(String gameType, Integer serviceType, Pageable pageable) {
        Page<com.dianjing.entity.Service> services = serviceMapper.findServices(gameType, serviceType, pageable);
        
        services.getContent().forEach(this::updateServiceStats);
        return services;
    }

    @Override
    @Transactional
    public List<Service> getRecommended(String gameType, Pageable pageable) {
        List<Service> cachedServices = serviceCacheService.getCachedRecommendedServices(gameType);
        if (cachedServices != null) {
            return cachedServices;
        }
        
        List<Service> services = serviceMapper.findServices(gameType, null, pageable).getContent();
        
        services.forEach(this::updateServiceStats);
        
        serviceCacheService.cacheRecommendedServices(gameType, services);
        return services;
    }

    @Override
    @Transactional
    public List<com.dianjing.entity.Service> getByProviderId(Long providerId) {
        List<com.dianjing.entity.Service> services = serviceMapper.findByProviderIdOrderByIdDesc(providerId);
        
        services.forEach(this::updateServiceStats);
        return services;
    }

    @Override
    public long countByProviderId(Long providerId) {
        return serviceMapper.countByProviderId(providerId);
    }

    @Override
    public long countOnlineByProviderId(Long providerId) {
        return serviceMapper.countByProviderIdAndStatus(providerId, Constants.SERVICE_ONLINE);
    }

    @Override
    public long countAll() {
        return serviceMapper.count();
    }

    @Override
    @Transactional
    public void approveService(Long id) {
        Service service = getById(id);
        verifyRealNameAuthentication(service.getProviderId());
        service.setStatus(Constants.SERVICE_ONLINE);
        update(service);
    }

    @Override
    @Transactional
    public void rejectService(Long id) {
        Service service = getById(id);
        service.setStatus(Constants.SERVICE_REJECTED);
        update(service);
    }
}
