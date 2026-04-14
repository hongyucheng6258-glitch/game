package com.dianjing.service.impl;

import com.dianjing.entity.SystemSetting;
import com.dianjing.mapper.SystemSettingMapper;
import com.dianjing.service.SystemSettingCacheService;
import com.dianjing.service.SystemSettingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SystemSettingServiceImpl implements SystemSettingService {

    private final SystemSettingMapper systemSettingMapper;
    private final SystemSettingCacheService systemSettingCacheService;

    public SystemSettingServiceImpl(SystemSettingMapper systemSettingMapper,
                                   SystemSettingCacheService systemSettingCacheService) {
        this.systemSettingMapper = systemSettingMapper;
        this.systemSettingCacheService = systemSettingCacheService;
        initializeDefaultSettings();
    }

    private void initializeDefaultSettings() {
        Map<String, Object> defaults = new HashMap<>();
        defaults.put("siteName", "电竞陪玩平台");
        defaults.put("siteDescription", "");
        defaults.put("commissionRate", 10);
        defaults.put("minPrice", 1);
        defaults.put("maxPrice", 999999);
        defaults.put("autoConfirmDays", 7);
        defaults.put("minWithdrawAmount", 10);
        defaults.put("maxWithdrawAmount", 50000);
        defaults.put("dailyWithdrawLimit", 3);
        defaults.put("withdrawCommissionRate", 5);
        defaults.put("allowRegister", true);
        defaults.put("defaultRole", 0);
        defaults.put("orderPaymentTimeoutMinutes", 30);
        defaults.put("orderPendingServiceTimeoutMinutes", 60);
        defaults.put("serviceTimeoutHours", 24);
        defaults.put("providerPenaltyEnabled", true);
        defaults.put("providerPenaltyScore", 5);
        defaults.put("providerPenaltyAmount", 10);

        for (Map.Entry<String, Object> entry : defaults.entrySet()) {
            String key = entry.getKey();
            var existing = systemSettingMapper.findBySettingKey(key);
            if (existing.isEmpty()) {
                SystemSetting setting = new SystemSetting();
                setting.setSettingKey(key);
                setting.setSettingValue(String.valueOf(entry.getValue()));
                setting.setSettingType(entry.getValue().getClass().getSimpleName());
                systemSettingMapper.save(setting);
            } else if (key.equals("maxPrice")) {
                // 更新已有的 maxPrice 设置
                SystemSetting setting = existing.get();
                setting.setSettingValue(String.valueOf(entry.getValue()));
                systemSettingMapper.save(setting);
            }
        }
    }

    @Override
    public Map<String, Object> getAllSettings() {
        Map<String, Object> cachedSettings = systemSettingCacheService.getCachedAllSettings();
        if (cachedSettings != null) {
            return cachedSettings;
        }
        Map<String, Object> settings = new HashMap<>();
        List<SystemSetting> all = systemSettingMapper.findAll();
        for (SystemSetting setting : all) {
            settings.put(setting.getSettingKey(), parseValue(setting.getSettingValue(), setting.getSettingType()));
        }
        systemSettingCacheService.cacheAllSettings(settings);
        return settings;
    }

    @Override
    @Transactional
    public void updateSettings(Map<String, Object> settings) {
        for (Map.Entry<String, Object> entry : settings.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            SystemSetting setting = systemSettingMapper.findBySettingKey(key)
                    .orElse(new SystemSetting());
            setting.setSettingKey(key);
            setting.setSettingValue(String.valueOf(value));
            setting.setSettingType(value.getClass().getSimpleName());
            systemSettingMapper.save(setting);
        }
        systemSettingCacheService.evictAllSettings();
    }

    @Override
    public String getString(String key, String defaultValue) {
        String cachedValue = systemSettingCacheService.getCachedString(key, null);
        if (cachedValue != null) {
            return cachedValue;
        }
        String value = systemSettingMapper.findBySettingKey(key)
                .map(SystemSetting::getSettingValue)
                .orElse(defaultValue);
        systemSettingCacheService.cacheSetting(key, value);
        return value;
    }

    @Override
    public Integer getInt(String key, Integer defaultValue) {
        Integer cachedValue = systemSettingCacheService.getCachedInt(key, null);
        if (cachedValue != null) {
            return cachedValue;
        }
        try {
            Integer value = systemSettingMapper.findBySettingKey(key)
                    .map(s -> Integer.parseInt(s.getSettingValue()))
                    .orElse(defaultValue);
            systemSettingCacheService.cacheSetting(key, value);
            return value;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public BigDecimal getBigDecimal(String key, BigDecimal defaultValue) {
        BigDecimal cachedValue = systemSettingCacheService.getCachedBigDecimal(key, null);
        if (cachedValue != null) {
            return cachedValue;
        }
        try {
            BigDecimal value = systemSettingMapper.findBySettingKey(key)
                    .map(s -> new BigDecimal(s.getSettingValue()))
                    .orElse(defaultValue);
            systemSettingCacheService.cacheSetting(key, value);
            return value;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public Boolean getBoolean(String key, Boolean defaultValue) {
        Boolean cachedValue = systemSettingCacheService.getCachedBoolean(key, null);
        if (cachedValue != null) {
            return cachedValue;
        }
        Boolean value = systemSettingMapper.findBySettingKey(key)
                .map(s -> Boolean.parseBoolean(s.getSettingValue()))
                .orElse(defaultValue);
        systemSettingCacheService.cacheSetting(key, value);
        return value;
    }

    private Object parseValue(String value, String type) {
        if (value == null) return null;
        switch (type) {
            case "Integer":
                return Integer.parseInt(value);
            case "BigDecimal":
                return new BigDecimal(value);
            case "Boolean":
                return Boolean.parseBoolean(value);
            default:
                return value;
        }
    }
}
