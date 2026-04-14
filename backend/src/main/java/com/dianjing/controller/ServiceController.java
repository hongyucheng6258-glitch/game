
package com.dianjing.controller;

import com.dianjing.common.PageResult;
import com.dianjing.common.Result;
import com.dianjing.dto.request.ServiceCreateRequest;
import com.dianjing.dto.request.ServiceUpdateRequest;
import com.dianjing.dto.response.ServiceDetailResponse;
import com.dianjing.entity.Review;
import com.dianjing.entity.Service;
import com.dianjing.service.ReviewService;
import com.dianjing.service.ServiceService;
import com.dianjing.service.UserService;
import com.dianjing.service.ActivityService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/services")
public class ServiceController {

    private final ServiceService serviceService;
    private final UserService userService;
    private final ReviewService reviewService;
    private final ActivityService activityService;

    public ServiceController(ServiceService serviceService, UserService userService, ReviewService reviewService, ActivityService activityService) {
        this.serviceService = serviceService;
        this.userService = userService;
        this.reviewService = reviewService;
        this.activityService = activityService;
    }

    private Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getUserByUsername(username).getId();
    }

    @GetMapping
    public Result<PageResult<ServiceDetailResponse>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String gameType,
            @RequestParam(required = false) Integer serviceType,
            @RequestParam(required = false) java.math.BigDecimal minPrice,
            @RequestParam(required = false) java.math.BigDecimal maxPrice,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean providerOnly,
            @RequestParam(required = false) Long providerId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page - 1, size);
        Long finalProviderId = providerId;
        if (providerOnly != null && providerOnly) {
            finalProviderId = getCurrentUserId();
        }
        // 当查询自己的服务时，includeAllStatus 为 true，可以看到所有状态；否则为 false，只显示上架服务
        Boolean includeAllStatus = (finalProviderId != null);
        Page<Service> servicePage = serviceService.search(keyword, gameType, serviceType, null, minPrice, maxPrice, sort, finalProviderId, includeAllStatus, pageable);
        
        // 转换为带服务商信息的响应对象
        List<ServiceDetailResponse> responseList = servicePage.getContent().stream()
                .map(service -> {
                    ServiceDetailResponse response = ServiceDetailResponse.fromEntity(service);
                    try {
                        com.dianjing.entity.User provider = userService.getUserById(service.getProviderId());
                        response.setProviderName(provider.getUsername());
                        response.setProviderAvatar(provider.getAvatar());
                    } catch (Exception e) {
                        // 忽略异常，保持原有数据
                    }
                    // 设置活动优惠信息
                    java.util.Map<String, Object> activityInfo = activityService.getActivityPriceForService(service.getId(), service.getPrice());
                    if (activityInfo.containsKey("activityId")) {
                        response.setActivityId((Long) activityInfo.get("activityId"));
                        response.setActivityTitle((String) activityInfo.get("activityTitle"));
                        response.setActivityPrice((java.math.BigDecimal) activityInfo.get("activityPrice"));
                        response.setActivityDiscountRate((java.math.BigDecimal) activityInfo.get("activityDiscountRate"));
                        response.setActivityType((Integer) activityInfo.get("activityType"));
                    }
                    return response;
                })
                .collect(java.util.stream.Collectors.toList());
        
        return Result.success(new PageResult<>(servicePage.getTotalElements(),
                servicePage.getTotalPages(), page, responseList));
    }

    @GetMapping("/{id}")
    public Result<com.dianjing.dto.response.ServiceDetailResponse> getDetail(@PathVariable Long id) {
        return Result.success(serviceService.getDetailById(id));
    }

    @GetMapping("/provider/{providerId}")
    public Result<List<Service>> getProviderServices(@PathVariable Long providerId) {
        return Result.success(serviceService.getByProviderId(providerId));
    }

    @GetMapping("/recommend")
    public Result<List<Service>> recommend(@RequestParam(required = false) String gameType) {
        PageRequest pageable = PageRequest.of(0, 10);
        return Result.success(serviceService.getRecommended(gameType, pageable));
    }

    @PostMapping
    public Result<Service> create(@Valid @RequestBody ServiceCreateRequest request) {
        Long userId = getCurrentUserId();
        Service service = serviceService.create(userId, request);
        return Result.success(service);
    }

    @GetMapping("/my")
    public Result<List<Service>> listMyServices() {
        Long userId = getCurrentUserId();
        return Result.success(serviceService.getByProviderId(userId));
    }

    @PutMapping("/{id}")
    public Result<Service> update(@PathVariable Long id, @Valid @RequestBody ServiceUpdateRequest request) {
        Long userId = getCurrentUserId();
        Service service = serviceService.update(userId, id, request);
        return Result.success(service);
    }

    @PutMapping("/{id}/status")
    public Result<Void> toggleStatus(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        serviceService.toggleStatus(userId, id);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        serviceService.delete(userId, id);
        return Result.success();
    }

    @GetMapping("/{serviceId}/reviews")
    public Result<PageResult<Review>> listServiceReviews(
            @PathVariable Long serviceId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page - 1, size);
        Page<Review> reviewPage = reviewService.getByServiceId(serviceId, pageable);
        return Result.success(new PageResult<>(reviewPage.getTotalElements(),
                reviewPage.getTotalPages(), page, reviewPage.getContent()));
    }
}
