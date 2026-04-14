package com.dianjing.service;

import java.math.BigDecimal;
import java.util.Map;

public interface SystemSettingService {
    Map<String, Object> getAllSettings();
    void updateSettings(Map<String, Object> settings);
    String getString(String key, String defaultValue);
    Integer getInt(String key, Integer defaultValue);
    BigDecimal getBigDecimal(String key, BigDecimal defaultValue);
    Boolean getBoolean(String key, Boolean defaultValue);
}
