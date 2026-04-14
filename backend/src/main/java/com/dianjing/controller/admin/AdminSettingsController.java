package com.dianjing.controller.admin;

import com.dianjing.common.Result;
import com.dianjing.service.SystemSettingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/settings")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSettingsController {

    private final SystemSettingService systemSettingService;

    public AdminSettingsController(SystemSettingService systemSettingService) {
        this.systemSettingService = systemSettingService;
    }

    @GetMapping
    public Result<Map<String, Object>> getSettings() {
        return Result.success(systemSettingService.getAllSettings());
    }

    @PutMapping
    public Result<Map<String, Object>> updateSettings(@RequestBody Map<String, Object> newSettings) {
        systemSettingService.updateSettings(newSettings);
        return Result.success(systemSettingService.getAllSettings());
    }
}
