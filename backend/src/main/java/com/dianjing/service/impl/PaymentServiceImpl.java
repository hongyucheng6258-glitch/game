package com.dianjing.service.impl;

import com.dianjing.common.BusinessException;
import com.dianjing.common.Constants;
import com.dianjing.dto.request.PaymentRequest;
import com.dianjing.dto.request.RechargeRequest;
import com.dianjing.entity.Order;
import com.dianjing.entity.PaymentRecord;
import com.dianjing.entity.User;
import com.dianjing.mapper.OrderMapper;
import com.dianjing.mapper.PaymentRecordMapper;
import com.dianjing.mapper.UserMapper;
import com.dianjing.service.PaymentService;
import com.dianjing.service.UserCacheService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRecordMapper paymentRecordMapper;
    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final UserCacheService userCacheService;

    public PaymentServiceImpl(PaymentRecordMapper paymentRecordMapper, OrderMapper orderMapper,
                              UserMapper userMapper, UserCacheService userCacheService) {
        this.paymentRecordMapper = paymentRecordMapper;
        this.orderMapper = orderMapper;
        this.userMapper = userMapper;
        this.userCacheService = userCacheService;
    }

    @Override
    @Transactional
    public PaymentRecord payOrder(Long userId, PaymentRequest request) {
        Order order = orderMapper.findByOrderNo(request.getOrderNo())
                .orElseThrow(() -> new BusinessException(404, "订单不存在"));
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作此订单");
        }
        if (order.getStatus() != Constants.ORDER_PENDING_PAYMENT) {
            throw new BusinessException(400, "订单状态不允许支付");
        }

        // 保存支付方式到订单
        order.setPaymentMethod(request.getPaymentMethod());

        PaymentRecord record = new PaymentRecord();
        record.setUserId(userId);
        record.setOrderId(order.getId());
        record.setType(Constants.PAYMENT_TYPE_PAY);
        record.setPaymentMethod(request.getPaymentMethod());
        record.setTransactionNo(UUID.randomUUID().toString().replace("-", ""));

        // 只有余额支付才立即扣除余额
        if ("balance".equals(request.getPaymentMethod())) {
            User user = userMapper.findById(userId)
                    .orElseThrow(() -> new BusinessException(404, "用户不存在"));
            if (user.getBalance().compareTo(order.getTotalAmount()) < 0) {
                throw new BusinessException(400, "余额不足，请先充值");
            }

            // 扣除余额
            user.setBalance(user.getBalance().subtract(order.getTotalAmount()));
            userMapper.save(user);

            record.setAmount(order.getTotalAmount().negate());
            record.setStatus(Constants.PAYMENT_STATUS_SUCCESS);
        } else {
            // 其他支付方式（如支付宝、微信）创建成功的支付记录（模拟支付成功）
            record.setAmount(order.getTotalAmount().negate());
            record.setStatus(Constants.PAYMENT_STATUS_SUCCESS);
        }

        // 所有支付方式都更新订单状态为待服务
        order.setStatus(Constants.ORDER_PENDING_SERVICE);
        order.setPaymentTime(java.time.LocalDateTime.now());
        orderMapper.save(order);
        
        // 清除用户缓存
        if ("balance".equals(request.getPaymentMethod())) {
            User user = userMapper.findById(userId)
                    .orElseThrow(() -> new BusinessException(404, "用户不存在"));
            userCacheService.evictUser(userId);
            userCacheService.evictUserByUsername(user.getUsername());
            userCacheService.evictUserBalance(userId);
        }

        return paymentRecordMapper.save(record);
    }

    @Override
    @Transactional
    public PaymentRecord recharge(Long userId, RechargeRequest request) {
        // 模拟充值成功
        User user = userMapper.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        user.setBalance(user.getBalance().add(request.getAmount()));
        userMapper.save(user);

        PaymentRecord record = new PaymentRecord();
        record.setUserId(userId);
        record.setAmount(request.getAmount());
        record.setType(Constants.PAYMENT_TYPE_RECHARGE);
        record.setPaymentMethod(request.getPaymentMethod());
        record.setStatus(Constants.PAYMENT_STATUS_SUCCESS);
        record.setTransactionNo(UUID.randomUUID().toString().replace("-", ""));
        
        // 清除用户缓存
        userCacheService.evictUser(userId);
        userCacheService.evictUserByUsername(user.getUsername());
        userCacheService.evictUserBalance(userId);
        
        return paymentRecordMapper.save(record);
    }

    @Override
    public Page<PaymentRecord> getRecords(Long userId, Integer type, Pageable pageable) {
        if (type != null) {
            return paymentRecordMapper.findByUserIdAndTypeOrderByIdDesc(userId, type, pageable);
        }
        return paymentRecordMapper.findByUserIdOrderByIdDesc(userId, pageable);
    }

    @Override
    public Map<String, Object> getStatistics(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        BigDecimal totalSpent = paymentRecordMapper.getTotalSpentByUser(userId);
        BigDecimal totalRecharged = paymentRecordMapper.getTotalRechargedByUser(userId);
        stats.put("totalSpent", totalSpent != null ? totalSpent : BigDecimal.ZERO);
        stats.put("totalRecharged", totalRecharged != null ? totalRecharged : BigDecimal.ZERO);
        User user = userMapper.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        stats.put("balance", user.getBalance());
        return stats;
    }

    @Override
    public PaymentRecord withdraw(Long userId, BigDecimal amount, String paymentMethod) {
        PaymentRecord record = new PaymentRecord();
        record.setUserId(userId);
        record.setAmount(amount.negate()); // 提现金额为负数
        record.setType(Constants.PAYMENT_TYPE_WITHDRAWAL);
        record.setPaymentMethod(paymentMethod);
        record.setStatus(Constants.PAYMENT_STATUS_SUCCESS);
        record.setTransactionNo(UUID.randomUUID().toString().replace("-", ""));
        return paymentRecordMapper.save(record);
    }

    @Override
    public PaymentRecord savePaymentRecord(PaymentRecord paymentRecord) {
        return paymentRecordMapper.save(paymentRecord);
    }

    @Override
    public Map<String, Object> getAdminStatistics() {
        Map<String, Object> stats = new HashMap<>();
        BigDecimal totalSpent = paymentRecordMapper.getTotalSpentAll();
        BigDecimal totalRecharged = paymentRecordMapper.getTotalRechargedAll();
        BigDecimal totalWithdraw = paymentRecordMapper.getTotalWithdrawAll();
        BigDecimal totalPlatformFee = paymentRecordMapper.getTotalPlatformFeeAll();
        
        // 总消费显示为绝对值（因为消费记录是负数）
        BigDecimal displayTotalSpent = (totalSpent != null ? totalSpent : BigDecimal.ZERO).abs();
        
        stats.put("totalRevenue", displayTotalSpent);
        stats.put("totalRecharge", totalRecharged != null ? totalRecharged : BigDecimal.ZERO);
        stats.put("totalWithdraw", totalWithdraw != null ? totalWithdraw.abs() : BigDecimal.ZERO);
        
        // 平台净收入 = 实际平台抽成记录总和
        stats.put("netIncome", totalPlatformFee != null ? totalPlatformFee : BigDecimal.ZERO);
        
        return stats;
    }
}
