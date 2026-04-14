package com.dianjing.controller;

import com.dianjing.common.Result;
import com.dianjing.dto.response.DashboardStatsResponse;
import com.dianjing.service.StatisticsService;
import com.dianjing.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final UserService userService;

    public StatisticsController(StatisticsService statisticsService, UserService userService) {
        this.statisticsService = statisticsService;
        this.userService = userService;
    }

    private Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getUserByUsername(username).getId();
    }

    @GetMapping("/ranking")
    public Result<List<Map<String, Object>>> getProviderRanking(
            @RequestParam(defaultValue = "10") int limit) {
        return Result.success(statisticsService.getProviderRanking(limit));
    }

    @GetMapping("/platform")
    public Result<DashboardStatsResponse> getPlatformStats() {
        return Result.success(statisticsService.getPlatformStats());
    }

    @GetMapping("/provider/{providerId}")
    public Result<Map<String, Object>> getProviderStatistics(@PathVariable Long providerId) {
        return Result.success(statisticsService.getProviderStatistics(providerId));
    }

    @GetMapping("/provider/statistics")
    public Result<Map<String, Object>> getCurrentProviderStatistics() {
        Long userId = getCurrentUserId();
        return Result.success(statisticsService.getProviderStatistics(userId));
    }

    @GetMapping("/trend/orders")
    public Result<List<Map<String, Object>>> getOrderTrend(
            @RequestParam(defaultValue = "7") int days) {
        return Result.success(statisticsService.getOrderTrend(days));
    }

    @GetMapping("/trend/revenue")
    public Result<List<Map<String, Object>>> getRevenueTrend(
            @RequestParam(defaultValue = "7") int days) {
        return Result.success(statisticsService.getRevenueTrend(days));
    }
}
