package com.dianjing.controller;

import com.dianjing.common.PageResult;
import com.dianjing.common.Result;
import com.dianjing.dto.request.WithdrawalRequest;
import com.dianjing.entity.WithdrawalApplication;
import com.dianjing.service.UserService;
import com.dianjing.service.WithdrawalService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/withdrawals")
public class WithdrawalController {

    private final WithdrawalService withdrawalService;
    private final UserService userService;

    public WithdrawalController(WithdrawalService withdrawalService, UserService userService) {
        this.withdrawalService = withdrawalService;
        this.userService = userService;
    }

    private Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getUserByUsername(username).getId();
    }

    /**
     * 申请提现
     */
    @PostMapping
    public Result<WithdrawalApplication> apply(@Valid @RequestBody WithdrawalRequest request) {
        Long userId = getCurrentUserId();
        WithdrawalApplication application = withdrawalService.apply(userId, request);
        return Result.success(application);
    }

    /**
     * 我的提现记录
     */
    @GetMapping
    public Result<PageResult<WithdrawalApplication>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = getCurrentUserId();
        PageRequest pageable = PageRequest.of(page - 1, size);
        Page<WithdrawalApplication> pageResult = withdrawalService.getUserApplications(userId, pageable);
        return Result.success(new PageResult<>(pageResult.getTotalElements(),
                pageResult.getTotalPages(), page, pageResult.getContent()));
    }
}
