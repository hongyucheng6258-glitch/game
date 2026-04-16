package com.dianjing.controller.admin;

import com.dianjing.common.BusinessException;
import com.dianjing.common.PageResult;
import com.dianjing.common.Result;
import com.dianjing.dto.request.UserUpdateRequest;
import com.dianjing.entity.User;
import com.dianjing.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 用户列表（分页，支持关键词搜索）
     */
    @GetMapping
    public Result<PageResult<User>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer role,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page - 1, size, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Order.desc("id")));
        Page<User> userPage = userService.getAllUsers(keyword, role, status, pageable);
        userPage.getContent().forEach(u -> u.setPassword(null));
        return Result.success(new PageResult<>(userPage.getTotalElements(),
                userPage.getTotalPages(), page, userPage.getContent()));
    }

    /**
     * 用户详情
     */
    @GetMapping("/{id}")
    public Result<User> getDetail(@PathVariable Long id) {
        User user = userService.getUserById(id);
        user.setPassword(null);
        return Result.success(user);
    }

    /**
     * 更新用户状态（启用/禁用）
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody java.util.Map<String, Integer> body) {
        Integer status = body.get("status");
        userService.updateStatus(id, status);
        return Result.success();
    }

    /**
     * 更新用户角色
     */
    @PutMapping("/{id}/role")
    public Result<Void> updateRole(@PathVariable Long id, @RequestBody java.util.Map<String, Integer> body) {
        Integer role = body.get("role");
        userService.updateRole(id, role);
        return Result.success();
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    public Result<User> updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
        User user = userService.adminUpdateUser(id, request);
        user.setPassword(null);
        return Result.success(user);
    }

    /**
     * 重置用户密码
     */
    @PutMapping("/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        String newPassword = body.get("newPassword");
        userService.adminResetPassword(id, newPassword);
        return Result.success();
    }

    /**
     * 调整用户余额
     */
    @PutMapping("/{id}/balance")
    public Result<Void> adjustBalance(@PathVariable Long id, @RequestBody java.util.Map<String, Object> body) {
        Object amountObj = body.get("amount");
        if (amountObj == null) {
            throw new BusinessException(400, "调整金额不能为空");
        }
        BigDecimal amount = new BigDecimal(amountObj.toString());
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            throw new BusinessException(400, "调整金额不能为0");
        }
        String remark = (String) body.get("remark");
        if (remark == null || remark.trim().isEmpty()) {
            throw new BusinessException(400, "调整备注不能为空");
        }
        userService.adminAdjustBalance(id, amount, remark);
        return Result.success();
    }
}
