package com.dianjing.controller;

import com.dianjing.common.PageResult;
import com.dianjing.common.Result;
import com.dianjing.dto.request.PaymentRequest;
import com.dianjing.dto.request.RechargeRequest;
import com.dianjing.entity.PaymentRecord;
import com.dianjing.service.PaymentService;
import com.dianjing.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final UserService userService;

    public PaymentController(PaymentService paymentService, UserService userService) {
        this.paymentService = paymentService;
        this.userService = userService;
    }

    private Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getUserByUsername(username).getId();
    }

    @PostMapping("/pay")
    public Result<PaymentRecord> pay(@Valid @RequestBody PaymentRequest request) {
        Long userId = getCurrentUserId();
        PaymentRecord record = paymentService.payOrder(userId, request);
        return Result.success(record);
    }

    @PostMapping("/recharge")
    public Result<PaymentRecord> recharge(@Valid @RequestBody RechargeRequest request) {
        Long userId = getCurrentUserId();
        PaymentRecord record = paymentService.recharge(userId, request);
        return Result.success(record);
    }

    @GetMapping("/records")
    public Result<PageResult<PaymentRecord>> listRecords(
            @RequestParam(required = false) Integer type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return listPaymentRecords(type, page, size);
    }

    @GetMapping("/payment-records")
    public Result<PageResult<PaymentRecord>> listPaymentRecords(
            @RequestParam(required = false) Integer type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = getCurrentUserId();
        PageRequest pageable = PageRequest.of(page - 1, size);
        Page<PaymentRecord> recordPage = paymentService.getRecords(userId, type, pageable);
        return Result.success(new PageResult<>(recordPage.getTotalElements(),
                recordPage.getTotalPages(), page, recordPage.getContent()));
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        Long userId = getCurrentUserId();
        return Result.success(paymentService.getStatistics(userId));
    }
}
