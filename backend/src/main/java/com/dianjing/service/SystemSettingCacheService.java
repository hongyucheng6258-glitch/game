package com.dianjing.service;

import java.math.BigDecimal;
import java.util.Map;

public interface SystemSettingCacheService {
    void cacheAllSettings(Map<String, Object> settings);
    Map<String, Object> getCachedAllSettings();
    
    void cacheSetting(String key, Object value);
    Object getCachedSetting(String key);
    
    String getCachedString(String key, String defaultValue);
    Integer getCachedInt(String key, Integer defaultValue);
    BigDecimal getCachedBigDecimal(String key, BigDecimal defaultValue);
    Boolean getCachedBoolean(String key, Boolean defaultValue);
    
    void evictSetting(String key);
    void evictAllSettings();
}
