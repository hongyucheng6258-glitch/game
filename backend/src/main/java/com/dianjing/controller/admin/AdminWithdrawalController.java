package com.dianjing.controller.admin;

import com.dianjing.common.PageResult;
import com.dianjing.common.Result;
import com.dianjing.dto.request.AuditRequest;
import com.dianjing.entity.WithdrawalApplication;
import com.dianjing.service.UserService;
import com.dianjing.service.WithdrawalService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/withdrawals")
@PreAuthorize("hasRole('ADMIN')")
public class AdminWithdrawalController {

    private final WithdrawalService withdrawalService;
    private final UserService userService;

    public AdminWithdrawalController(WithdrawalService withdrawalService, UserService userService) {
        this.withdrawalService = withdrawalService;
        this.userService = userService;
    }

    private Long getCurrentAdminId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getUserByUsername(username).getId();
    }

    /**
     * 提现申请列表
     */
    @GetMapping
    public Result<PageResult<WithdrawalApplication>> list(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page - 1, size, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Order.desc("id")));
        Page<WithdrawalApplication> pageResult = withdrawalService.getAllApplications(status, pageable);
        return Result.success(new PageResult<>(pageResult.getTotalElements(),
                pageResult.getTotalPages(), page, pageResult.getContent()));
    }

    /**
     * 审核提现申请
     */
    @PostMapping("/{id}/audit")
    public Result<Void> audit(@PathVariable Long id, @Valid @RequestBody AuditRequest request) {
        Long adminId = getCurrentAdminId();
        withdrawalService.audit(adminId, id, request);
        return Result.success();
    }
}
