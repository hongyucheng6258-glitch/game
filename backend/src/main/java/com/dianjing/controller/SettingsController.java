package com.dianjing.controller;

import com.dianjing.common.Result;
import com.dianjing.service.SystemSettingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/settings")
public class SettingsController {

    private final SystemSettingService systemSettingService;

    public SettingsController(SystemSettingService systemSettingService) {
        this.systemSettingService = systemSettingService;
    }

    @GetMapping
    public Result<Map<String, Object>> getSettings() {
        return Result.success(systemSettingService.getAllSettings());
    }
}