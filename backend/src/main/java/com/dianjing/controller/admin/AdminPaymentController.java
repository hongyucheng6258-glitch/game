package com.dianjing.controller.admin;

import com.dianjing.common.PageResult;
import com.dianjing.common.Result;
import com.dianjing.entity.PaymentRecord;
import com.dianjing.mapper.PaymentRecordMapper;
import com.dianjing.service.PaymentService;
import com.dianjing.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/payments")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPaymentController {

    private final PaymentService paymentService;
    private final UserService userService;
    private final PaymentRecordMapper paymentRecordMapper;

    public AdminPaymentController(PaymentService paymentService, UserService userService, PaymentRecordMapper paymentRecordMapper) {
        this.paymentService = paymentService;
        this.userService = userService;
        this.paymentRecordMapper = paymentRecordMapper;
    }

    /**
     * 支付记录列表（分页）
     */
    @GetMapping
    public Result<PageResult<PaymentRecord>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page - 1, size, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Order.desc("id")));
        Page<PaymentRecord> recordPage;
        
        if (keyword != null && !keyword.isEmpty()) {
            // 尝试按用户ID或订单ID搜索
            try {
                Long idKeyword = Long.parseLong(keyword);
                recordPage = paymentRecordMapper.findByUserIdOrOrderId(idKeyword, idKeyword, pageable);
            } catch (NumberFormatException e) {
                // 如果不是数字，则返回所有记录
                recordPage = paymentRecordMapper.findAll(pageable);
            }
        } else if (type != null && status != null) {
            recordPage = paymentRecordMapper.findByTypeAndStatus(type, status, pageable);
        } else if (type != null) {
            recordPage = paymentRecordMapper.findByType(type, pageable);
        } else if (status != null) {
            recordPage = paymentRecordMapper.findByStatus(status, pageable);
        } else {
            recordPage = paymentRecordMapper.findAll(pageable);
        }
        
        return Result.success(new PageResult<>(recordPage.getTotalElements(),
                recordPage.getTotalPages(), page, recordPage.getContent()));
    }

    /**
     * 财务统计
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = paymentService.getAdminStatistics();
        return Result.success(stats);
    }
}
