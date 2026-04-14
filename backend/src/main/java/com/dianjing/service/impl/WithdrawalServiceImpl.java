package com.dianjing.service.impl;

import com.dianjing.common.BusinessException;
import com.dianjing.common.Constants;
import com.dianjing.dto.request.AuditRequest;
import com.dianjing.dto.request.WithdrawalRequest;
import com.dianjing.entity.User;
import com.dianjing.entity.WithdrawalApplication;
import com.dianjing.mapper.UserMapper;
import com.dianjing.mapper.WithdrawalApplicationMapper;
import com.dianjing.service.LevelService;
import com.dianjing.service.PaymentService;
import com.dianjing.service.SystemSettingService;
import com.dianjing.service.UserCacheService;
import com.dianjing.service.WithdrawalService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class WithdrawalServiceImpl implements WithdrawalService {

    private final WithdrawalApplicationMapper withdrawalApplicationMapper;
    private final UserMapper userMapper;
    private final PaymentService paymentService;
    private final SystemSettingService systemSettingService;
    private final UserCacheService userCacheService;
    private final LevelService levelService;

    public WithdrawalServiceImpl(WithdrawalApplicationMapper withdrawalApplicationMapper,
                                 UserMapper userMapper,
                                 PaymentService paymentService,
                                 SystemSettingService systemSettingService,
                                 UserCacheService userCacheService,
                                 LevelService levelService) {
        this.withdrawalApplicationMapper = withdrawalApplicationMapper;
        this.userMapper = userMapper;
        this.paymentService = paymentService;
        this.systemSettingService = systemSettingService;
        this.userCacheService = userCacheService;
        this.levelService = levelService;
    }

    @Override
    public void validateWithdrawal(Long userId, BigDecimal amount) {
        var levelInfo = levelService.getUserLevelInfo(userId);
        
        // 获取系统基础设置
        BigDecimal minWithdrawAmount = systemSettingService.getBigDecimal("minWithdrawAmount", BigDecimal.TEN);
        BigDecimal maxWithdrawAmount = systemSettingService.getBigDecimal("maxWithdrawAmount", new BigDecimal("50000"));
        
        if (amount.compareTo(minWithdrawAmount) < 0) {
            throw new BusinessException(400, "提现金额不能低于" + minWithdrawAmount + "元");
        }
        if (amount.compareTo(maxWithdrawAmount) > 0) {
            throw new BusinessException(400, "提现金额不能高于" + maxWithdrawAmount + "元");
        }
        
        // 使用用户等级的每日提现次数限制（等级特权）
        Integer dailyWithdrawLimit = levelInfo.getDailyWithdrawalLimit();
        
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        long todayCount = withdrawalApplicationMapper.countByUserIdAndCreatedAtAfter(userId, todayStart);
        if (todayCount >= dailyWithdrawLimit) {
            throw new BusinessException(400, "每日提现次数已达上限: " + dailyWithdrawLimit + "次（当前等级: Lv." + levelInfo.getCurrentLevel() + " " + levelInfo.getCurrentLevelName() + "）");
        }
    }

    @Override
    @Transactional
    public WithdrawalApplication apply(Long userId, WithdrawalRequest request) {
        User user = userMapper.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        
        validateWithdrawal(userId, request.getAmount());
        
        // 获取用户等级信息
        var levelInfo = levelService.getUserLevelInfo(userId);
        
        // 获取系统基础提现抽成比例
        BigDecimal baseCommissionRate = systemSettingService.getBigDecimal("withdrawCommissionRate", new BigDecimal("5"));
        
        // 应用等级提现手续费折扣
        BigDecimal withdrawalFeeDiscount = levelInfo.getWithdrawalFeeDiscount();
        BigDecimal actualCommissionRate = baseCommissionRate.multiply(withdrawalFeeDiscount).setScale(2, java.math.RoundingMode.HALF_UP);
        
        // 计算手续费和实际到账金额
        BigDecimal feeAmount = request.getAmount().multiply(actualCommissionRate).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal actualAmount = request.getAmount().subtract(feeAmount);
        
        // 检查余额是否足够（扣除手续费后的金额）
        if (user.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BusinessException(400, "余额不足");
        }
        
        // 冻结余额
        user.setBalance(user.getBalance().subtract(request.getAmount()));
        userMapper.save(user);

        // 清除用户缓存
        userCacheService.evictUser(userId);
        userCacheService.evictUserByUsername(user.getUsername());
        userCacheService.evictUserBalance(userId);

        WithdrawalApplication application = new WithdrawalApplication();
        application.setUserId(userId);
        application.setAmount(request.getAmount());
        application.setFeeAmount(feeAmount);
        application.setActualAmount(actualAmount);
        application.setFeeRate(actualCommissionRate);
        application.setOriginalFeeRate(baseCommissionRate);
        application.setBankAccount(request.getBankAccount());
        application.setBankName(request.getBankName());
        application.setAccountName(request.getAccountName());
        application.setStatus(Constants.WITHDRAWAL_STATUS_COMPLETED);
        withdrawalApplicationMapper.save(application);
        
        // 创建提现支付记录（按实际到账金额），包含手续费信息
        com.dianjing.entity.PaymentRecord withdrawRecord = paymentService.withdraw(userId, request.getAmount(), "bank");
        withdrawRecord.setFeeAmount(feeAmount);
        withdrawRecord.setActualAmount(actualAmount);
        withdrawRecord.setFeeRate(actualCommissionRate);
        paymentService.savePaymentRecord(withdrawRecord);
        
        // 创建平台手续费收入记录
        com.dianjing.entity.PaymentRecord platformFeeRecord = new com.dianjing.entity.PaymentRecord();
        platformFeeRecord.setUserId(1L); // 使用admin用户ID
        platformFeeRecord.setAmount(feeAmount);
        platformFeeRecord.setType(Constants.PAYMENT_TYPE_PLATFORM_FEE);
        platformFeeRecord.setPaymentMethod("system");
        platformFeeRecord.setStatus(Constants.PAYMENT_STATUS_SUCCESS);
        platformFeeRecord.setTransactionNo(java.util.UUID.randomUUID().toString().replace("-", ""));
        paymentService.savePaymentRecord(platformFeeRecord);
        
        return application;
    }

    @Override
    public Page<WithdrawalApplication> getUserApplications(Long userId, Pageable pageable) {
        return withdrawalApplicationMapper.findByUserIdOrderByIdDesc(userId, pageable);
    }

    @Override
    public Page<WithdrawalApplication> getAllApplications(Integer status, Pageable pageable) {
        if (status != null) {
            return withdrawalApplicationMapper.findByStatusOrderByIdDesc(status, pageable);
        }
        return withdrawalApplicationMapper.findAll(
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Order.desc("id"))));
    }

    @Override
    @Transactional
    public void audit(Long adminId, Long id, AuditRequest request) {
        WithdrawalApplication application = withdrawalApplicationMapper.findById(id)
                .orElseThrow(() -> new BusinessException(404, "提现申请不存在"));
        if (application.getStatus() != Constants.WITHDRAWAL_STATUS_PENDING) {
            throw new BusinessException(400, "该申请已审核");
        }

        application.setAuditUserId(adminId);
        application.setAuditTime(LocalDateTime.now());
        application.setAuditRemark(request.getRemark());

        if (request.getStatus() == Constants.WITHDRAWAL_STATUS_APPROVED) {
            application.setStatus(Constants.WITHDRAWAL_STATUS_APPROVED);
            // 审核通过，余额已在申请时扣除，此处标记为已完成
            application.setStatus(Constants.WITHDRAWAL_STATUS_COMPLETED);
        } else if (request.getStatus() == Constants.WITHDRAWAL_STATUS_REJECTED) {
            application.setStatus(Constants.WITHDRAWAL_STATUS_REJECTED);
            // 审核拒绝，退还全额余额（包括手续费）
            User user = userMapper.findById(application.getUserId())
                    .orElseThrow(() -> new BusinessException(404, "用户不存在"));
            user.setBalance(user.getBalance().add(application.getAmount()));
            userMapper.save(user);
            
            // 清除用户缓存
            userCacheService.evictUser(application.getUserId());
            userCacheService.evictUserByUsername(user.getUsername());
            userCacheService.evictUserBalance(application.getUserId());
            
            // 创建退款支付记录
            paymentService.withdraw(application.getUserId(), application.getAmount().negate(), "bank");
        } else {
            throw new BusinessException(400, "无效的审核状态");
        }
        withdrawalApplicationMapper.save(application);
    }
}
