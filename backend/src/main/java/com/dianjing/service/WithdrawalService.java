package com.dianjing.service;

import com.dianjing.dto.request.AuditRequest;
import com.dianjing.dto.request.WithdrawalRequest;
import com.dianjing.entity.WithdrawalApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface WithdrawalService {

    WithdrawalApplication apply(Long userId, WithdrawalRequest request);

    Page<WithdrawalApplication> getUserApplications(Long userId, Pageable pageable);

    Page<WithdrawalApplication> getAllApplications(Integer status, Pageable pageable);

    void audit(Long adminId, Long id, AuditRequest request);
    
    void validateWithdrawal(Long userId, BigDecimal amount);
}
