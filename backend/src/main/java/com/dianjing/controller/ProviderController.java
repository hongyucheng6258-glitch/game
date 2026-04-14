package com.dianjing.controller;

import com.dianjing.common.Result;
import com.dianjing.service.StatisticsService;
import com.dianjing.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/provider")
public class ProviderController {

    private final StatisticsService statisticsService;
    private final UserService userService;

    public ProviderController(StatisticsService statisticsService, UserService userService) {
        this.statisticsService = statisticsService;
        this.userService = userService;
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
}
