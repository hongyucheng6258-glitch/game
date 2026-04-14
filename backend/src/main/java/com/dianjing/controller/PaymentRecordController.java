package com.dianjing.controller;

import com.dianjing.common.PageResult;
import com.dianjing.common.Result;
import com.dianjing.dto.request.RechargeRequest;
import com.dianjing.entity.PaymentRecord;
import com.dianjing.service.PaymentService;
import com.dianjing.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payment-records")
public class PaymentRecordController {

    private final PaymentService paymentService;
    private final UserService userService;

    public PaymentRecordController(PaymentService paymentService, UserService userService) {
        this.paymentService = paymentService;
        this.userService = userService;
    }

    private Long getCurrentUserId() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            if ("anonymousUser".equals(username)) {
                throw new Exception("用户未登录");
            }
            return userService.getUserByUsername(username).getId();
        } catch (Exception e) {
            throw new RuntimeException("用户未登录");
        }
    }

    /**
     * 余额充值
     */
    @PostMapping("/recharge")
    public Result<PaymentRecord> recharge(@Valid @RequestBody RechargeRequest request) {
        Long userId = getCurrentUserId();
        PaymentRecord record = paymentService.recharge(userId, request);
        return Result.success(record);
    }

    /**
     * 支付/充值记录
     */
    @GetMapping
    public Result<PageResult<PaymentRecord>> listRecords(
            @RequestParam(required = false) Integer type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = getCurrentUserId();
        PageRequest pageable = PageRequest.of(page - 1, size);
        Page<PaymentRecord> recordPage = paymentService.getRecords(userId, type, pageable);
        return Result.success(new PageResult<>(recordPage.getTotalElements(),
                recordPage.getTotalPages(), page, recordPage.getContent()));
    }
}
