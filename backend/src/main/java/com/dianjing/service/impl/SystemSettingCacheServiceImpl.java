package com.dianjing.service.impl;

import com.dianjing.service.SystemSettingCacheService;
import com.dianjing.util.RedisUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class SystemSettingCacheServiceImpl implements SystemSettingCacheService {

    private static final String SETTINGS_KEY = "system:settings";
    private static final String SETTING_PREFIX = "system:setting:";
    private static final long SETTINGS_TTL = 60;

    private final RedisUtil redisUtil;
    private final ObjectMapper objectMapper;

    public SystemSettingCacheServiceImpl(RedisUtil redisUtil, ObjectMapper objectMapper) {
        this.redisUtil = redisUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    public void cacheAllSettings(Map<String, Object> settings) {
        try {
            String json = objectMapper.writeValueAsString(settings);
            redisUtil.set(SETTINGS_KEY, json, SETTINGS_TTL, TimeUnit.MINUTES);
            
            // 同时缓存单个设置
            for (Map.Entry<String, Object> entry : settings.entrySet()) {
                cacheSetting(entry.getKey(), entry.getValue());
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize settings", e);
        }
    }

    @Override
    public Map<String, Object> getCachedAllSettings() {
        String json = redisUtil.get(SETTINGS_KEY);
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize settings", e);
        }
    }

    @Override
    public void cacheSetting(String key, Object value) {
        try {
            String cacheKey = SETTING_PREFIX + key;
            String json = objectMapper.writeValueAsString(value);
            redisUtil.set(cacheKey, json, SETTINGS_TTL, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize setting", e);
        }
    }

    @Override
    public Object getCachedSetting(String key) {
        String cacheKey = SETTING_PREFIX + key;
        String json = redisUtil.get(cacheKey);
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize setting", e);
        }
    }

    @Override
    public String getCachedString(String key, String defaultValue) {
        Object value = getCachedSetting(key);
        return value != null ? value.toString() : defaultValue;
    }

    @Override
    public Integer getCachedInt(String key, Integer defaultValue) {
        Object value = getCachedSetting(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public BigDecimal getCachedBigDecimal(String key, BigDecimal defaultValue) {
        Object value = getCachedSetting(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public Boolean getCachedBoolean(String key, Boolean defaultValue) {
        Object value = getCachedSetting(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(value.toString());
    }

    @Override
    public void evictSetting(String key) {
        String cacheKey = SETTING_PREFIX + key;
        redisUtil.delete(cacheKey);
        redisUtil.delete(SETTINGS_KEY);
    }

    @Override
    public void evictAllSettings() {
        redisUtil.delete(SETTINGS_KEY);
        redisUtil.keys(SETTING_PREFIX + "*").forEach(redisUtil::delete);
    }
}
