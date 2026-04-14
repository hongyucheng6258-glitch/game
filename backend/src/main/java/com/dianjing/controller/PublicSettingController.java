package com.dianjing.controller;

import com.dianjing.common.Result;
import com.dianjing.service.SystemSettingService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/public/settings")
public class PublicSettingController {

    private final SystemSettingService systemSettingService;

    public PublicSettingController(SystemSettingService systemSettingService) {
        this.systemSettingService = systemSettingService;
    }

    /**
     * 获取公开设置信息（供普通用户使用）
     */
    @GetMapping
    public Result<Map<String, Object>> getPublicSettings() {
        Map<String, Object> publicSettings = new HashMap<>();
        publicSettings.put("withdrawCommissionRate", systemSettingService.getBigDecimal("withdrawCommissionRate", java.math.BigDecimal.valueOf(5)));
        publicSettings.put("minWithdrawAmount", systemSettingService.getBigDecimal("minWithdrawAmount", java.math.BigDecimal.TEN));
        publicSettings.put("maxWithdrawAmount", systemSettingService.getBigDecimal("maxWithdrawAmount", java.math.BigDecimal.valueOf(50000)));
        publicSettings.put("dailyWithdrawLimit", systemSettingService.getInt("dailyWithdrawLimit", 3));
        return Result.success(publicSettings);
    }
}
