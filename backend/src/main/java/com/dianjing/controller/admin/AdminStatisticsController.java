package com.dianjing.controller.admin;

import com.dianjing.common.Result;
import com.dianjing.dto.response.DashboardStatsResponse;
import com.dianjing.service.StatisticsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/statistics")
@PreAuthorize("hasRole('ADMIN')")
public class AdminStatisticsController {

    private final StatisticsService statisticsService;

    public AdminStatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    /**
     * 仪表盘统计数据
     */
    @GetMapping("/dashboard")
    public Result<DashboardStatsResponse> getDashboardStats() {
        return Result.success(statisticsService.getPlatformStats());
    }
}
