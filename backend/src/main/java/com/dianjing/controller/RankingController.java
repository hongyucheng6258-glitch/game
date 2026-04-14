package com.dianjing.controller;

import com.dianjing.common.Result;
import com.dianjing.service.StatisticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/rankings")
public class RankingController {

    private final StatisticsService statisticsService;

    public RankingController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    /**
     * 公开接口 - 通用排行榜接口
     */
    @GetMapping("/{type}")
    public Result<List<Map<String, Object>>> getRanking(
            @PathVariable String type,
            @RequestParam(defaultValue = "50") int limit) {
        return switch (type.toLowerCase()) {
            case "rating" -> Result.success(statisticsService.getRatingRanking(limit));
            case "sales" -> Result.success(statisticsService.getSalesRanking(limit));
            case "popular" -> Result.success(statisticsService.getPopularRanking(limit));
            default -> Result.success(List.of());
        };
    }

    /**
     * 公开接口 - 服务商评分排行榜
     */
    @GetMapping("/rating")
    public Result<List<Map<String, Object>>> getRatingRanking(
            @RequestParam(defaultValue = "50") int limit) {
        return Result.success(statisticsService.getRatingRanking(limit));
    }

    /**
     * 公开接口 - 服务商销量排行榜
     */
    @GetMapping("/sales")
    public Result<List<Map<String, Object>>> getSalesRanking(
            @RequestParam(defaultValue = "50") int limit) {
        return Result.success(statisticsService.getSalesRanking(limit));
    }

    /**
     * 公开接口 - 服务商人气排行榜
     */
    @GetMapping("/popular")
    public Result<List<Map<String, Object>>> getPopularRanking(
            @RequestParam(defaultValue = "50") int limit) {
        return Result.success(statisticsService.getPopularRanking(limit));
    }
}
