package com.dianjing.service.impl;

import com.dianjing.entity.Service;
import com.dianjing.service.ServiceCacheService;
import com.dianjing.util.RedisUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class ServiceCacheServiceImpl implements ServiceCacheService {

    private static final String SERVICE_DETAIL_PREFIX = "service:detail:";
    private static final String RECOMMENDED_SERVICES_PREFIX = "service:recommended:";
    private static final long SERVICE_DETAIL_TTL = 10;
    private static final long RECOMMENDED_TTL = 5;

    private final RedisUtil redisUtil;
    private final ObjectMapper objectMapper;

    public ServiceCacheServiceImpl(RedisUtil redisUtil, ObjectMapper objectMapper) {
        this.redisUtil = redisUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    public void cacheServiceDetail(Service service) {
        try {
            String key = SERVICE_DETAIL_PREFIX + service.getId();
            String json = objectMapper.writeValueAsString(service);
            redisUtil.set(key, json, SERVICE_DETAIL_TTL, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize service", e);
        }
    }

    @Override
    public Service getCachedServiceDetail(Long serviceId) {
        String key = SERVICE_DETAIL_PREFIX + serviceId;
        String json = redisUtil.get(key);
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, Service.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize service", e);
        }
    }

    @Override
    public void evictServiceDetail(Long serviceId) {
        String key = SERVICE_DETAIL_PREFIX + serviceId;
        redisUtil.delete(key);
    }

    @Override
    public void cacheRecommendedServices(String gameType, List<Service> services) {
        try {
            String key = RECOMMENDED_SERVICES_PREFIX + (gameType != null ? gameType : "all");
            String json = objectMapper.writeValueAsString(services);
            redisUtil.set(key, json, RECOMMENDED_TTL, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize services", e);
        }
    }

    @Override
    public List<Service> getCachedRecommendedServices(String gameType) {
        String key = RECOMMENDED_SERVICES_PREFIX + (gameType != null ? gameType : "all");
        String json = redisUtil.get(key);
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<Service>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize services", e);
        }
    }

    @Override
    public void evictRecommendedServices(String gameType) {
        String key = RECOMMENDED_SERVICES_PREFIX + (gameType != null ? gameType : "all");
        redisUtil.delete(key);
    }

    @Override
    public void evictAllServiceCaches() {
        redisUtil.keys(SERVICE_DETAIL_PREFIX + "*").forEach(redisUtil::delete);
        redisUtil.keys(RECOMMENDED_SERVICES_PREFIX + "*").forEach(redisUtil::delete);
    }
}
