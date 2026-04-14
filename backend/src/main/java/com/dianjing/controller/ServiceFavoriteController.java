package com.dianjing.controller;

import com.dianjing.common.Result;
import com.dianjing.entity.ServiceFavorite;
import com.dianjing.entity.Service;
import com.dianjing.mapper.ServiceMapper;
import com.dianjing.service.ServiceFavoriteService;
import com.dianjing.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/favorites")
public class ServiceFavoriteController {

    private final ServiceFavoriteService serviceFavoriteService;
    private final UserService userService;
    private final ServiceMapper serviceMapper;

    public ServiceFavoriteController(ServiceFavoriteService serviceFavoriteService, UserService userService, ServiceMapper serviceMapper) {
        this.serviceFavoriteService = serviceFavoriteService;
        this.userService = userService;
        this.serviceMapper = serviceMapper;
    }

    private Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getUserByUsername(username).getId();
    }

    @PostMapping("/{serviceId}")
    public Result<Void> addFavorite(@PathVariable Long serviceId) {
        Long userId = getCurrentUserId();
        serviceFavoriteService.favorite(userId, serviceId);
        return Result.success();
    }

    @DeleteMapping("/{serviceId}")
    public Result<Void> removeFavorite(@PathVariable Long serviceId) {
        Long userId = getCurrentUserId();
        serviceFavoriteService.unfavorite(userId, serviceId);
        return Result.success();
    }

    @GetMapping("/check/{serviceId}")
    public Result<Map<String, Object>> checkFavorite(@PathVariable Long serviceId) {
        Long userId = getCurrentUserId();
        Map<String, Object> result = new HashMap<>();
        result.put("favorited", serviceFavoriteService.isFavorited(userId, serviceId));
        return Result.success(result);
    }

    @GetMapping
    public Result<com.dianjing.common.PageResult<Map<String, Object>>> listFavorites(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = getCurrentUserId();
        PageRequest pageable = PageRequest.of(page - 1, size);
        Page<ServiceFavorite> favoritePage = serviceFavoriteService.list(userId, pageable);
        
        // 转换为包含服务详情的列表
        List<Map<String, Object>> items = favoritePage.getContent().stream().map(favorite -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", favorite.getId());
            item.put("serviceId", favorite.getServiceId());
            item.put("createdAt", favorite.getCreatedAt());
            
            // 添加服务详情
            Service service = serviceMapper.findById(favorite.getServiceId()).orElse(null);
            if (service != null) {
                Map<String, Object> serviceMap = new HashMap<>();
                serviceMap.put("id", service.getId());
                serviceMap.put("title", service.getTitle());
                serviceMap.put("description", service.getDescription());
                serviceMap.put("price", service.getPrice());
                serviceMap.put("gameType", service.getGameType());
                serviceMap.put("serviceType", service.getServiceType());
                serviceMap.put("duration", service.getDuration());
                serviceMap.put("rating", service.getRating());
                serviceMap.put("reviewCount", service.getReviewCount());
                serviceMap.put("salesCount", service.getSalesCount());
                serviceMap.put("providerId", service.getProviderId());
                item.put("service", serviceMap);
            }
            
            return item;
        }).collect(Collectors.toList());
        
        return Result.success(new com.dianjing.common.PageResult<>(favoritePage.getTotalElements(),
                favoritePage.getTotalPages(), page, items));
    }
}
