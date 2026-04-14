package com.dianjing.service;

import com.dianjing.dto.request.PaymentRequest;
import com.dianjing.dto.request.RechargeRequest;
import com.dianjing.entity.PaymentRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface PaymentService {

    PaymentRecord payOrder(Long userId, PaymentRequest request);

    PaymentRecord recharge(Long userId, RechargeRequest request);

    Page<PaymentRecord> getRecords(Long userId, Integer type, Pageable pageable);

    Map<String, Object> getStatistics(Long userId);

    PaymentRecord withdraw(Long userId, java.math.BigDecimal amount, String paymentMethod);

    PaymentRecord savePaymentRecord(PaymentRecord paymentRecord);

    Map<String, Object> getAdminStatistics();
}
