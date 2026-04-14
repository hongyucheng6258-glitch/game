package com.dianjing.controller;

import com.dianjing.common.Result;
import com.dianjing.dto.response.LevelResponse;
import com.dianjing.dto.response.UserLevelInfoResponse;
import com.dianjing.entity.User;
import com.dianjing.service.LevelService;
import com.dianjing.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "等级管理", description = "用户等级相关接口")
@RestController
@RequestMapping("/api/v1/level")
public class LevelController {

    private final LevelService levelService;
    private final UserService userService;

    public LevelController(LevelService levelService, UserService userService) {
        this.levelService = levelService;
        this.userService = userService;
    }

    @Operation(summary = "获取所有等级列表")
    @GetMapping("/list")
    public Result<List<LevelResponse>> getAllLevels() {
        return Result.success(levelService.getAllLevels());
    }

    @Operation(summary = "获取指定等级信息")
    @GetMapping("/{level}")
    public Result<LevelResponse> getLevelByLevel(@PathVariable Integer level) {
        return Result.success(levelService.getLevelByLevel(level));
    }

    @Operation(summary = "获取当前用户等级信息")
    @GetMapping("/my-info")
    public Result<UserLevelInfoResponse> getMyLevelInfo() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByUsername(username);
        return Result.success(levelService.getUserLevelInfo(user.getId()));
    }

    @Operation(summary = "获取指定用户等级信息")
    @GetMapping("/user-info/{userId}")
    public Result<UserLevelInfoResponse> getUserLevelInfo(@PathVariable Long userId) {
        return Result.success(levelService.getUserLevelInfo(userId));
    }
}
