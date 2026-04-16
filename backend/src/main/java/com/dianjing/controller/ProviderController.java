package com.dianjing.controller;

import com.dianjing.common.Result;
import com.dianjing.entity.Service;
import com.dianjing.mapper.ServiceMapper;
import com.dianjing.service.StatisticsService;
import com.dianjing.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/provider")
public class ProviderController {

    private final StatisticsService statisticsService;
    private final UserService userService;
    private final ServiceMapper serviceMapper;

    public ProviderController(StatisticsService statisticsService, UserService userService, ServiceMapper serviceMapper) {
        this.statisticsService = statisticsService;
        this.userService = userService;
        this.serviceMapper = serviceMapper;
    }

    private Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getUserByUsername(username).getId();
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> getProviderStatistics() {
        Long userId = getCurrentUserId();
        return Result.success(statisticsService.getProviderStatistics(userId));
    }

    @GetMapping("/services")
    public Result<Page<Service>> getMyServices(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = getCurrentUserId();
        Page<Service> services = serviceMapper.findByProviderIdOrderByIdDesc(userId, PageRequest.of(page - 1, size));
        return Result.success(services);
    }
}
