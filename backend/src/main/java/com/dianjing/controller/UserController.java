package com.dianjing.controller;

import com.dianjing.common.Result;
import com.dianjing.dto.request.PasswordUpdateRequest;
import com.dianjing.dto.request.UserUpdateRequest;
import com.dianjing.entity.User;
import com.dianjing.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/info")
    public Result<User> getUserInfo() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByUsername(username);
        user.setPassword(null);
        return Result.success(user);
    }

    @PutMapping("/info")
    public Result<User> updateUserInfo(@Valid @RequestBody UserUpdateRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByUsername(username);
        User updated = userService.updateUserInfo(user.getId(), request);
        updated.setPassword(null);
        return Result.success(updated);
    }

    @PutMapping("/password")
    public Result<Void> updatePassword(@Valid @RequestBody PasswordUpdateRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByUsername(username);
        userService.updatePassword(user.getId(), request);
        return Result.success();
    }

    @PostMapping("/avatar")
    public Result<String> updateAvatar(@RequestParam("file") MultipartFile file) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByUsername(username);
        String avatarUrl = userService.updateAvatar(user.getId(), file);
        return Result.success(avatarUrl);
    }

    @GetMapping("/balance")
    public Result<BigDecimal> getBalance() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByUsername(username);
        return Result.success(userService.getBalance(user.getId()));
    }

    @GetMapping("/profile")
    public Result<User> getProfile() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            if ("anonymousUser".equals(username)) {
                return Result.error(401, "用户未登录");
            }
            User user = userService.getUserByUsername(username);
            if (user == null) {
                return Result.error(401, "用户不存在");
            }
            user.setPassword(null);
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(401, "用户未登录");
        }
    }

    @PutMapping("/profile")
    public Result<User> updateProfile(@Valid @RequestBody UserUpdateRequest request) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            if ("anonymousUser".equals(username)) {
                return Result.error(401, "用户未登录");
            }
            User user = userService.getUserByUsername(username);
            if (user == null) {
                return Result.error(401, "用户不存在");
            }
            User updated = userService.updateUserInfo(user.getId(), request);
            updated.setPassword(null);
            return Result.success(updated);
        } catch (Exception e) {
            return Result.error(401, "用户未登录");
        }
    }

    @GetMapping("/id/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        user.setPassword(null);
        return Result.success(user);
    }
}
