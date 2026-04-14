package com.dianjing.service.impl;

import com.dianjing.common.BusinessException;
import com.dianjing.common.Constants;
import com.dianjing.dto.request.OrderCreateRequest;
import com.dianjing.entity.Order;
import com.dianjing.entity.PaymentRecord;
import com.dianjing.entity.User;
import com.dianjing.mapper.OrderMapper;
import com.dianjing.mapper.PaymentRecordMapper;
import com.dianjing.mapper.ServiceMapper;
import com.dianjing.mapper.UserMapper;
import com.dianjing.service.LevelService;
import com.dianjing.service.MessageService;
import com.dianjing.service.OrderService;
import com.dianjing.service.ActivityService;
import com.dianjing.service.StatisticsCacheService;
import com.dianjing.util.OrderNoGenerator;
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
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final ServiceMapper serviceMapper;
    private final UserMapper userMapper;
    private final MessageService messageService;
    private final PaymentRecordMapper paymentRecordMapper;
    private final LevelService levelService;
    private final ActivityService activityService;
    private final StatisticsCacheService statisticsCacheService;

    public OrderServiceImpl(OrderMapper orderMapper, ServiceMapper serviceMapper,
                            UserMapper userMapper, MessageService messageService, PaymentRecordMapper paymentRecordMapper,
                            LevelService levelService, ActivityService activityService, StatisticsCacheService statisticsCacheService) {
        this.orderMapper = orderMapper;
        this.serviceMapper = serviceMapper;
        this.userMapper = userMapper;
        this.messageService = messageService;
        this.paymentRecordMapper = paymentRecordMapper;
        this.levelService = levelService;
        this.activityService = activityService;
        this.statisticsCacheService = statisticsCacheService;
    }

    @Override
    @Transactional
    public Order create(Long userId, OrderCreateRequest request) {
        com.dianjing.entity.Service service = serviceMapper.findById(request.getServiceId())
                .orElseThrow(() -> new BusinessException(404, "服务不存在"));
        if (service.getStatus() != Constants.SERVICE_ONLINE) {
            throw new BusinessException(400, "该服务已下线");
        }
        if (service.getProviderId().equals(userId)) {
            throw new BusinessException(400, "不能下单自己的服务");
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setProviderId(service.getProviderId());
        order.setServiceId(request.getServiceId());
        order.setOrderNo(OrderNoGenerator.generate());
        
        BigDecimal originalPrice = service.getPrice();
        
        // 查询活动优惠价
        Map<String, Object> activityInfo = activityService.getActivityPriceForService(service.getId(), originalPrice);
        BigDecimal activityPrice = activityInfo.containsKey("activityPrice") 
                ? (BigDecimal) activityInfo.get("activityPrice") : null;
        
        // 应用等级折扣（基于活动优惠价或原价）
        var levelInfo = levelService.getUserLevelInfo(userId);
        BigDecimal discountRate = levelInfo.getDiscountRate();
        
        // 支持协商价格
        if (request.getNegotiatedPrice() != null) {
            BigDecimal baseForDiscount = request.getNegotiatedPrice();
            BigDecimal negotiatedDiscounted = baseForDiscount.multiply(discountRate).setScale(2, java.math.RoundingMode.HALF_UP);
            order.setTotalAmount(negotiatedDiscounted);
            order.setOriginalPrice(request.getNegotiatedPrice());
            order.setIsNegotiatedPrice(1);
            if (activityPrice != null) {
                order.setActivityId((Long) activityInfo.get("activityId"));
                order.setActivityPrice(activityPrice);
            }
        } else {
            BigDecimal baseForDiscount = activityPrice != null ? activityPrice : originalPrice;
            BigDecimal discountedPrice = baseForDiscount.multiply(discountRate).setScale(2, java.math.RoundingMode.HALF_UP);
            order.setTotalAmount(discountedPrice);
            order.setOriginalPrice(originalPrice);
            order.setIsNegotiatedPrice(0);
            if (activityPrice != null) {
                order.setActivityId((Long) activityInfo.get("activityId"));
                order.setActivityPrice(activityPrice);
            }
        }
        
        order.setStatus(Constants.ORDER_PENDING_PAYMENT);
        order.setGameAccountId(request.getGameAccountId());
        order.setRequirements(request.getRequirements());
        order.setPriceNegotiationMessageId(request.getPriceNegotiationMessageId());
        return orderMapper.save(order);
    }

    @Override
    @Transactional
    public Order pay(Long userId, String orderNo, String paymentMethod) {
        Order order = getByOrderNo(orderNo);
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作此订单");
        }
        if (order.getStatus() != Constants.ORDER_PENDING_PAYMENT) {
            throw new BusinessException(400, "订单状态不允许支付");
        }

        // 保存支付方式到订单
        order.setPaymentMethod(paymentMethod);

        // 只有余额支付才立即扣除余额
        if ("balance".equals(paymentMethod)) {
            User user = userMapper.findById(userId)
                    .orElseThrow(() -> new BusinessException(404, "用户不存在"));
            if (user.getBalance().compareTo(order.getTotalAmount()) < 0) {
                throw new BusinessException(400, "余额不足，请先充值");
            }

            // 扣除用户余额（已应用折扣后的金额）
            user.setBalance(user.getBalance().subtract(order.getTotalAmount()));
            userMapper.save(user);

            // 创建用户消费记录
            PaymentRecord paymentRecord = new PaymentRecord();
            paymentRecord.setUserId(userId);
            paymentRecord.setOrderId(order.getId());
            paymentRecord.setAmount(order.getTotalAmount().negate());
            paymentRecord.setType(Constants.PAYMENT_TYPE_PAY);
            paymentRecord.setPaymentMethod(paymentMethod);
            paymentRecord.setStatus(Constants.PAYMENT_STATUS_SUCCESS);
            paymentRecord.setTransactionNo(UUID.randomUUID().toString().replace("-", ""));
            paymentRecordMapper.save(paymentRecord);
            
            // 记录原价和折扣信息
            if (order.getOriginalPrice() != null && order.getOriginalPrice().compareTo(order.getTotalAmount()) > 0) {
                paymentRecord.setOriginalAmount(order.getOriginalPrice());
                paymentRecord.setDiscountAmount(order.getOriginalPrice().subtract(order.getTotalAmount()));
                paymentRecordMapper.save(paymentRecord);
            }
        } else {
            // 其他支付方式（如支付宝、微信）创建成功的支付记录（模拟支付成功）
            PaymentRecord paymentRecord = new PaymentRecord();
            paymentRecord.setUserId(userId);
            paymentRecord.setOrderId(order.getId());
            paymentRecord.setAmount(order.getTotalAmount().negate());
            paymentRecord.setType(Constants.PAYMENT_TYPE_PAY);
            paymentRecord.setPaymentMethod(paymentMethod);
            paymentRecord.setStatus(Constants.PAYMENT_STATUS_SUCCESS);
            paymentRecord.setTransactionNo(UUID.randomUUID().toString().replace("-", ""));
            paymentRecordMapper.save(paymentRecord);
            
            // 记录原价和折扣信息
            if (order.getOriginalPrice() != null && order.getOriginalPrice().compareTo(order.getTotalAmount()) > 0) {
                paymentRecord.setOriginalAmount(order.getOriginalPrice());
                paymentRecord.setDiscountAmount(order.getOriginalPrice().subtract(order.getTotalAmount()));
                paymentRecordMapper.save(paymentRecord);
            }
        }

        // 更新用户总消费（按原价计算）
        User user = userMapper.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        BigDecimal totalExpense = user.getTotalExpense() != null ? user.getTotalExpense() : BigDecimal.ZERO;
        user.setTotalExpense(totalExpense.add(order.getOriginalPrice() != null ? order.getOriginalPrice() : order.getTotalAmount()));
        userMapper.save(user);
        
        // 支付成功时给用户增加经验值（按订单金额计算：1元=1经验）
        int userExp = (order.getOriginalPrice() != null ? order.getOriginalPrice() : order.getTotalAmount()).intValue();
        levelService.addExperience(userId, userExp);
        
        // 所有支付方式都更新订单状态为待服务
        order.setStatus(Constants.ORDER_PENDING_SERVICE);
        order.setPaymentTime(LocalDateTime.now());
        orderMapper.save(order);

        // 通知服务提供者
        messageService.sendSystemMessage(order.getProviderId(),
                "您有新的订单，订单号：" + orderNo + "，请及时处理。", order.getId(), "order");

        return order;
    }

    @Override
    @Transactional
    public void cancel(Long userId, String orderNo) {
        Order order = getByOrderNo(orderNo);
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作此订单");
        }
        if (order.getStatus() != Constants.ORDER_PENDING_PAYMENT &&
            order.getStatus() != Constants.ORDER_PENDING_SERVICE) {
            throw new BusinessException(400, "当前订单状态不允许取消");
        }

        // 如果已支付，退还余额
        if (order.getStatus() == Constants.ORDER_PENDING_SERVICE) {
            User user = userMapper.findById(userId)
                    .orElseThrow(() -> new BusinessException(404, "用户不存在"));
            user.setBalance(user.getBalance().add(order.getTotalAmount()));
            userMapper.save(user);
        }

        order.setStatus(Constants.ORDER_CANCELLED);
        orderMapper.save(order);

        messageService.sendSystemMessage(order.getProviderId(),
                "订单 " + orderNo + " 已被用户取消。", order.getId(), "order");
    }

    @Override
    @Transactional
    public void startService(Long providerId, String orderNo) {
        Order order = getByOrderNo(orderNo);
        if (!order.getProviderId().equals(providerId)) {
            throw new BusinessException(403, "无权操作此订单");
        }
        if (order.getStatus() != Constants.ORDER_PENDING_SERVICE) {
            throw new BusinessException(400, "订单状态不允许开始服务");
        }
        order.setStatus(Constants.ORDER_IN_SERVICE);
        order.setStartTime(LocalDateTime.now());
        orderMapper.save(order);

        messageService.sendSystemMessage(order.getUserId(),
                "您的订单 " + orderNo + " 已开始服务。", order.getId(), "order");
    }

    @Override
    @Transactional
    public void completeService(Long providerId, String orderNo) {
        Order order = getByOrderNo(orderNo);
        if (!order.getProviderId().equals(providerId)) {
            throw new BusinessException(403, "无权操作此订单");
        }
        if (order.getStatus() != Constants.ORDER_IN_SERVICE) {
            throw new BusinessException(400, "订单状态不允许完成服务");
        }
        order.setStatus(Constants.ORDER_PENDING_REVIEW);
        order.setEndTime(LocalDateTime.now());
        orderMapper.save(order);

        // 更新服务销量
        serviceMapper.findById(order.getServiceId()).ifPresent(service -> {
            service.setSalesCount(service.getSalesCount() + 1);
            serviceMapper.save(service);
        });

        // 计算平台手续费
        BigDecimal platformFee = order.getTotalAmount().multiply(
                BigDecimal.valueOf(Constants.PLATFORM_FEE_RATE));
        // 服务提供者收入（扣除平台手续费）
        BigDecimal income = order.getTotalAmount().subtract(platformFee);
        
        User provider = userMapper.findById(providerId)
                .orElseThrow(() -> new BusinessException(404, "服务提供者不存在"));
        provider.setBalance(provider.getBalance().add(income));
        userMapper.save(provider);

        // 创建支付记录（服务提供者收入）
        PaymentRecord record = new PaymentRecord();
        record.setUserId(providerId);
        record.setOrderId(order.getId());
        record.setAmount(income);
        record.setType(Constants.PAYMENT_TYPE_INCOME);
        record.setPaymentMethod("system");
        record.setStatus(Constants.PAYMENT_STATUS_SUCCESS);
        record.setTransactionNo(UUID.randomUUID().toString().replace("-", ""));
        paymentRecordMapper.save(record);
        
        // 创建平台手续费收入记录
        PaymentRecord platformFeeRecord = new PaymentRecord();
        platformFeeRecord.setUserId(1L);
        platformFeeRecord.setOrderId(order.getId());
        platformFeeRecord.setAmount(platformFee);
        platformFeeRecord.setType(Constants.PAYMENT_TYPE_PLATFORM_FEE);
        platformFeeRecord.setPaymentMethod("system");
        platformFeeRecord.setStatus(Constants.PAYMENT_STATUS_SUCCESS);
        platformFeeRecord.setTransactionNo(UUID.randomUUID().toString().replace("-", ""));
        paymentRecordMapper.save(platformFeeRecord);
        
        // 给服务商增加经验值（按收入计算：1元=2经验）
        int providerExp = income.intValue() * 2;
        levelService.addExperience(order.getProviderId(), providerExp);
        
        messageService.sendSystemMessage(order.getUserId(),
                "您的订单 " + orderNo + " 服务已完成，请确认并评价。", order.getId(), "order");
        
        messageService.sendSystemMessage(order.getProviderId(),
                "订单 " + orderNo + " 已完成，获得经验值+" + providerExp, order.getId(), "order");
        
        // 更新排行榜缓存
        statisticsCacheService.evictAllStatisticsCaches();
    }

    @Override
    @Transactional
    public void confirm(Long userId, String orderNo) {
        Order order = getByOrderNo(orderNo);
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作此订单");
        }
        if (order.getStatus() != Constants.ORDER_PENDING_REVIEW) {
            throw new BusinessException(400, "订单状态不允许确认");
        }
        order.setStatus(Constants.ORDER_COMPLETED);
        orderMapper.save(order);
    }

    @Override
    public Order getByOrderNo(String orderNo) {
        return orderMapper.findByOrderNo(orderNo)
                .orElseThrow(() -> new BusinessException(404, "订单不存在"));
    }

    @Override
    public Order getById(Long id) {
        return orderMapper.findById(id)
                .orElseThrow(() -> new BusinessException(404, "订单不存在"));
    }

    @Override
    public Page<Order> getUserOrders(Long userId, Integer status, Pageable pageable) {
        if (status != null) {
            return orderMapper.findByUserIdAndStatusOrderByIdDesc(userId, status, pageable);
        }
        return orderMapper.findByUserIdOrderByIdDesc(userId, pageable);
    }

    @Override
    public Page<Order> getProviderOrders(Long providerId, Integer status, Pageable pageable) {
        if (status != null) {
            return orderMapper.findByProviderIdAndStatusOrderByIdDesc(providerId, status, pageable);
        }
        return orderMapper.findByProviderIdOrderByIdDesc(providerId, pageable);
    }

    @Override
    public Page<Order> getAllOrders(Integer status, Pageable pageable) {
        if (status != null) {
            return orderMapper.findAllByStatusOrderByIdDesc(status, pageable);
        }
        return orderMapper.findAllByOrderByIdDesc(pageable);
    }

    @Override
    public Map<String, Object> getUserOrderStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders", orderMapper.countByUserId(userId));
        stats.put("pendingPayment", orderMapper.countByProviderIdAndStatus(userId, Constants.ORDER_PENDING_PAYMENT));
        stats.put("pendingService", orderMapper.countByProviderIdAndStatus(userId, Constants.ORDER_PENDING_SERVICE));
        stats.put("inService", orderMapper.countByProviderIdAndStatus(userId, Constants.ORDER_IN_SERVICE));
        stats.put("pendingReview", orderMapper.countByProviderIdAndStatus(userId, Constants.ORDER_PENDING_REVIEW));
        stats.put("completed", orderMapper.countByProviderIdAndStatus(userId, Constants.ORDER_COMPLETED));
        stats.put("cancelled", orderMapper.countByProviderIdAndStatus(userId, Constants.ORDER_CANCELLED));
        return stats;
    }

    @Override
    public Map<String, Object> getProviderOrderStats(Long providerId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders", orderMapper.countByProviderId(providerId));
        stats.put("pendingService", orderMapper.countByProviderIdAndStatus(providerId, Constants.ORDER_PENDING_SERVICE));
        stats.put("inService", orderMapper.countByProviderIdAndStatus(providerId, Constants.ORDER_IN_SERVICE));
        stats.put("pendingReview", orderMapper.countByProviderIdAndStatus(providerId, Constants.ORDER_PENDING_REVIEW));
        stats.put("completed", orderMapper.countByProviderIdAndStatus(providerId, Constants.ORDER_COMPLETED));
        stats.put("cancelled", orderMapper.countByProviderIdAndStatus(providerId, Constants.ORDER_CANCELLED));
        return stats;
    }
}
