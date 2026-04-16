package com.dianjing.scheduled;

import com.dianjing.common.Constants;
import com.dianjing.entity.Order;
import com.dianjing.entity.PaymentRecord;
import com.dianjing.entity.User;
import com.dianjing.mapper.OrderMapper;
import com.dianjing.mapper.PaymentRecordMapper;
import com.dianjing.mapper.ServiceMapper;
import com.dianjing.mapper.UserMapper;
import com.dianjing.service.MessageService;
import com.dianjing.service.SystemSettingService;
import com.dianjing.service.UserCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class OrderTimeoutScheduledTask {

    private static final Logger log = LoggerFactory.getLogger(OrderTimeoutScheduledTask.class);

    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final ServiceMapper serviceMapper;
    private final PaymentRecordMapper paymentRecordMapper;
    private final SystemSettingService systemSettingService;
    private final MessageService messageService;
    private final UserCacheService userCacheService;

    public OrderTimeoutScheduledTask(OrderMapper orderMapper, UserMapper userMapper,
                                       ServiceMapper serviceMapper, PaymentRecordMapper paymentRecordMapper,
                                       SystemSettingService systemSettingService,
                                       MessageService messageService,
                                       UserCacheService userCacheService) {
        this.orderMapper = orderMapper;
        this.userMapper = userMapper;
        this.serviceMapper = serviceMapper;
        this.paymentRecordMapper = paymentRecordMapper;
        this.systemSettingService = systemSettingService;
        this.messageService = messageService;
        this.userCacheService = userCacheService;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void processTimeouts() {
        log.debug("开始处理订单超时...");
        
        processPaymentTimeout();
        processPendingServiceTimeout();
        processServiceTimeout();
        
        log.debug("订单超时处理完成");
    }

    private void processPaymentTimeout() {
        Integer timeoutMinutes = systemSettingService.getInt("orderPaymentTimeoutMinutes", 30);
        LocalDateTime beforeTime = LocalDateTime.now().minusMinutes(timeoutMinutes);
        
        List<Order> orders = orderMapper.findByStatusAndCreatedAtBefore(
                Constants.ORDER_PENDING_PAYMENT, beforeTime);
        
        for (Order order : orders) {
            log.info("订单 {} 支付超时，自动取消", order.getOrderNo());
            order.setStatus(Constants.ORDER_CANCELLED);
            orderMapper.save(order);
            
            messageService.sendSystemMessage(order.getUserId(),
                    "您的订单 " + order.getOrderNo() + " 因支付超时已自动取消。", order.getId(), "order");
        }
    }

    private void processPendingServiceTimeout() {
        Integer timeoutMinutes = systemSettingService.getInt("orderPendingServiceTimeoutMinutes", 60);
        LocalDateTime beforeTime = LocalDateTime.now().minusMinutes(timeoutMinutes);
        
        List<Order> orders = orderMapper.findByStatusAndPaymentTimeBefore(
                Constants.ORDER_PENDING_SERVICE, beforeTime);
        
        for (Order order : orders) {
            log.info("订单 {} 等待服务超时，自动取消并退款", order.getOrderNo());
            cancelAndRefundOrder(order, "等待服务超时");
        }
    }

    private void processServiceTimeout() {
        Integer timeoutHours = systemSettingService.getInt("serviceTimeoutHours", 24);
        LocalDateTime beforeTime = LocalDateTime.now().minusHours(timeoutHours);
        
        List<Order> orders = orderMapper.findByStatusAndStartTimeBefore(
                Constants.ORDER_IN_SERVICE, beforeTime);
        
        for (Order order : orders) {
            log.info("订单 {} 服务超时，自动完成", order.getOrderNo());
            autoCompleteService(order);
        }
    }

    private void cancelAndRefundOrder(Order order, String reason) {
        order.setStatus(Constants.ORDER_CANCELLED);
        orderMapper.save(order);

        // 只有余额支付才退还用户余额
        if ("balance".equals(order.getPaymentMethod())) {
            User user = userMapper.findById(order.getUserId()).orElse(null);
            if (user != null) {
                user.setBalance(user.getBalance().add(order.getTotalAmount()));
                userMapper.save(user);

                // 清除用户缓存
                userCacheService.evictUser(order.getUserId());
                userCacheService.evictUserBalance(order.getUserId());
            }
        }

        // 创建用户退款记录
        PaymentRecord refundRecord = new PaymentRecord();
        refundRecord.setUserId(order.getUserId());
        refundRecord.setOrderId(order.getId());
        refundRecord.setAmount(order.getTotalAmount());
        refundRecord.setType(Constants.PAYMENT_TYPE_REFUND);
        refundRecord.setPaymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod() : "system");
        refundRecord.setStatus(Constants.PAYMENT_STATUS_SUCCESS);
        refundRecord.setTransactionNo(UUID.randomUUID().toString().replace("-", ""));
        paymentRecordMapper.save(refundRecord);

        messageService.sendSystemMessage(order.getUserId(),
                "您的订单 " + order.getOrderNo() + " 因" + reason + "已自动取消，款项已退还。", order.getId(), "order");
        messageService.sendSystemMessage(order.getProviderId(),
                "订单 " + order.getOrderNo() + " 因" + reason + "已自动取消。", order.getId(), "order");
    }

    private void autoCompleteService(Order order) {
        Boolean penaltyEnabled = systemSettingService.getBoolean("providerPenaltyEnabled", true);
        
        order.setStatus(Constants.ORDER_PENDING_REVIEW);
        order.setEndTime(LocalDateTime.now());
        orderMapper.save(order);

        serviceMapper.findById(order.getServiceId()).ifPresent(service -> {
            service.setSalesCount(service.getSalesCount() + 1);
            serviceMapper.save(service);
        });

        BigDecimal platformFee = order.getTotalAmount().multiply(
                BigDecimal.valueOf(Constants.PLATFORM_FEE_RATE));
        BigDecimal income = order.getTotalAmount().subtract(platformFee);
        
        User provider = userMapper.findById(order.getProviderId()).orElse(null);
        if (provider != null) {
            provider.setBalance(provider.getBalance().add(income));
            userMapper.save(provider);

            PaymentRecord record = new PaymentRecord();
            record.setUserId(order.getProviderId());
            record.setOrderId(order.getId());
            record.setAmount(income);
            record.setType(Constants.PAYMENT_TYPE_INCOME);
            record.setPaymentMethod("system");
            record.setStatus(Constants.PAYMENT_STATUS_SUCCESS);
            record.setTransactionNo(UUID.randomUUID().toString().replace("-", ""));
            paymentRecordMapper.save(record);
            
            PaymentRecord platformFeeRecord = new PaymentRecord();
            platformFeeRecord.setUserId(1L);
            platformFeeRecord.setOrderId(order.getId());
            platformFeeRecord.setAmount(platformFee);
            platformFeeRecord.setType(Constants.PAYMENT_TYPE_PLATFORM_FEE);
            platformFeeRecord.setPaymentMethod("system");
            platformFeeRecord.setStatus(Constants.PAYMENT_STATUS_SUCCESS);
            platformFeeRecord.setTransactionNo(UUID.randomUUID().toString().replace("-", ""));
            paymentRecordMapper.save(platformFeeRecord);
        }

        if (penaltyEnabled) {
            applyProviderPenalty(order.getProviderId());
        }

        messageService.sendSystemMessage(order.getUserId(),
                "您的订单 " + order.getOrderNo() + " 因服务超时已自动完成，请确认并评价。", order.getId(), "order");
        messageService.sendSystemMessage(order.getProviderId(),
                "您的订单 " + order.getOrderNo() + " 因超时已自动完成，已受到相应惩罚。", order.getId(), "order");
    }

    private void applyProviderPenalty(Long providerId) {
        Integer penaltyScore = systemSettingService.getInt("providerPenaltyScore", 5);
        Integer penaltyAmount = systemSettingService.getInt("providerPenaltyAmount", 10);

        User provider = userMapper.findById(providerId).orElse(null);
        if (provider != null) {
            // 防止 creditScore 为 null
            Integer currentScore = provider.getCreditScore() != null ? provider.getCreditScore() : 100;
            Integer newScore = Math.max(0, currentScore - penaltyScore);
            provider.setCreditScore(newScore);
            
            if (penaltyAmount > 0) {
                BigDecimal amount = new BigDecimal(penaltyAmount);
                // 防止 balance 为 null
                BigDecimal currentBalance = provider.getBalance() != null ? provider.getBalance() : BigDecimal.ZERO;
                provider.setBalance(currentBalance.subtract(amount));
                
                // 创建罚金支付记录
                PaymentRecord penaltyRecord = new PaymentRecord();
                penaltyRecord.setUserId(providerId);
                penaltyRecord.setAmount(amount.negate()); // 罚金为负数
                penaltyRecord.setType(Constants.PAYMENT_TYPE_PENALTY);
                penaltyRecord.setPaymentMethod("system");
                penaltyRecord.setStatus(Constants.PAYMENT_STATUS_SUCCESS);
                penaltyRecord.setTransactionNo(UUID.randomUUID().toString().replace("-", ""));
                paymentRecordMapper.save(penaltyRecord);
            }
            
            userMapper.save(provider);
            
            // 清除用户缓存，确保余额显示正确
            userCacheService.evictUser(providerId);
            userCacheService.evictUserBalance(providerId);
            
            log.info("服务者 {} 受到惩罚：信誉分 -{}，罚金 -{}", providerId, penaltyScore, penaltyAmount);
        }
    }
}
