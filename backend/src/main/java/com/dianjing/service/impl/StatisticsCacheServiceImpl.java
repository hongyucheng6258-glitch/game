package com.dianjing.service.impl;

import com.dianjing.dto.response.DashboardStatsResponse;
import com.dianjing.service.StatisticsCacheService;
import com.dianjing.util.RedisUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class StatisticsCacheServiceImpl implements StatisticsCacheService {

    private static final String PLATFORM_STATS_KEY = "statistics:platform";
    private static final String PROVIDER_RANKING_KEY = "statistics:ranking:provider";
    private static final String RATING_RANKING_KEY = "statistics:ranking:rating";
    private static final String SALES_RANKING_KEY = "statistics:ranking:sales";
    private static final String POPULAR_RANKING_KEY = "statistics:ranking:popular";
    private static final String PROVIDER_STATS_PREFIX = "statistics:provider:";
    private static final long STATS_TTL = 10;
    private static final long RANKING_TTL = 5;
    private static final long PROVIDER_STATS_TTL = 30;

    private final RedisUtil redisUtil;
    private final ObjectMapper objectMapper;

    public StatisticsCacheServiceImpl(RedisUtil redisUtil, ObjectMapper objectMapper) {
        this.redisUtil = redisUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    public void cachePlatformStats(DashboardStatsResponse stats) {
        try {
            String json = objectMapper.writeValueAsString(stats);
            redisUtil.set(PLATFORM_STATS_KEY, json, STATS_TTL, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize platform stats", e);
        }
    }

    @Override
    public DashboardStatsResponse getCachedPlatformStats() {
        String json = redisUtil.get(PLATFORM_STATS_KEY);
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, DashboardStatsResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize platform stats", e);
        }
    }

    @Override
    public void evictPlatformStats() {
        redisUtil.delete(PLATFORM_STATS_KEY);
    }

    @Override
    public void cacheProviderRanking(List<Map<String, Object>> ranking) {
        try {
            String json = objectMapper.writeValueAsString(ranking);
            redisUtil.set(PROVIDER_RANKING_KEY, json, RANKING_TTL, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize provider ranking", e);
        }
    }

    @Override
    public List<Map<String, Object>> getCachedProviderRanking() {
        return getCachedRanking(PROVIDER_RANKING_KEY);
    }

    @Override
    public void cacheRatingRanking(List<Map<String, Object>> ranking) {
        try {
            String json = objectMapper.writeValueAsString(ranking);
            redisUtil.set(RATING_RANKING_KEY, json, RANKING_TTL, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize rating ranking", e);
        }
    }

    @Override
    public List<Map<String, Object>> getCachedRatingRanking() {
        return getCachedRanking(RATING_RANKING_KEY);
    }

    @Override
    public void cacheSalesRanking(List<Map<String, Object>> ranking) {
        try {
            String json = objectMapper.writeValueAsString(ranking);
            redisUtil.set(SALES_RANKING_KEY, json, RANKING_TTL, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize sales ranking", e);
        }
    }

    @Override
    public List<Map<String, Object>> getCachedSalesRanking() {
        return getCachedRanking(SALES_RANKING_KEY);
    }

    @Override
    public void cachePopularRanking(List<Map<String, Object>> ranking) {
        try {
            String json = objectMapper.writeValueAsString(ranking);
            redisUtil.set(POPULAR_RANKING_KEY, json, RANKING_TTL, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize popular ranking", e);
        }
    }

    @Override
    public List<Map<String, Object>> getCachedPopularRanking() {
        return getCachedRanking(POPULAR_RANKING_KEY);
    }

    @Override
    public void cacheProviderStatistics(Long providerId, Map<String, Object> stats) {
        try {
            String key = PROVIDER_STATS_PREFIX + providerId;
            String json = objectMapper.writeValueAsString(stats);
            redisUtil.set(key, json, PROVIDER_STATS_TTL, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize provider stats", e);
        }
    }

    @Override
    public Map<String, Object> getCachedProviderStatistics(Long providerId) {
        String key = PROVIDER_STATS_PREFIX + providerId;
        String json = redisUtil.get(key);
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize provider stats", e);
        }
    }

    @Override
    public void evictProviderStatistics(Long providerId) {
        String key = PROVIDER_STATS_PREFIX + providerId;
        redisUtil.delete(key);
    }

    @Override
    public void evictAllStatisticsCaches() {
        redisUtil.delete(PLATFORM_STATS_KEY);
        redisUtil.delete(PROVIDER_RANKING_KEY);
        redisUtil.delete(RATING_RANKING_KEY);
        redisUtil.delete(SALES_RANKING_KEY);
        redisUtil.delete(POPULAR_RANKING_KEY);
        redisUtil.keys(PROVIDER_STATS_PREFIX + "*").forEach(redisUtil::delete);
    }

    private List<Map<String, Object>> getCachedRanking(String key) {
        String json = redisUtil.get(key);
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize ranking", e);
        }
    }
}
