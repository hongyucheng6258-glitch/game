package com.dianjing.service;

import com.dianjing.dto.response.DashboardStatsResponse;

import java.util.List;
import java.util.Map;

public interface StatisticsCacheService {
    void cachePlatformStats(DashboardStatsResponse stats);
    DashboardStatsResponse getCachedPlatformStats();
    void evictPlatformStats();
    
    void cacheProviderRanking(List<Map<String, Object>> ranking);
    List<Map<String, Object>> getCachedProviderRanking();
    
    void cacheRatingRanking(List<Map<String, Object>> ranking);
    List<Map<String, Object>> getCachedRatingRanking();
    
    void cacheSalesRanking(List<Map<String, Object>> ranking);
    List<Map<String, Object>> getCachedSalesRanking();
    
    void cachePopularRanking(List<Map<String, Object>> ranking);
    List<Map<String, Object>> getCachedPopularRanking();
    
    void cacheProviderStatistics(Long providerId, Map<String, Object> stats);
    Map<String, Object> getCachedProviderStatistics(Long providerId);
    void evictProviderStatistics(Long providerId);
    
    void evictAllStatisticsCaches();
}
