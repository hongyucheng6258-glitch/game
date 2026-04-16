package com.dianjing.service;

import com.dianjing.dto.response.DashboardStatsResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface StatisticsService {

    DashboardStatsResponse getPlatformStats();

    List<Map<String, Object>> getProviderRanking(int limit);

    Map<String, Object> getProviderStatistics(Long providerId);

    List<Map<String, Object>> getRatingRanking(int limit);

    List<Map<String, Object>> getSalesRanking(int limit);

    List<Map<String, Object>> getPopularRanking(int limit);

    List<Map<String, Object>> getRevenueRanking(int limit);

    List<Map<String, Object>> getOrderTrend(int days);

    List<Map<String, Object>> getRevenueTrend(int days);
}
