package com.dianjing.controller;

import com.dianjing.common.Constants;
import com.dianjing.common.Result;
import com.dianjing.entity.Order;
import com.dianjing.entity.PaymentRecord;
import com.dianjing.entity.User;
import com.dianjing.mapper.OrderMapper;
import com.dianjing.mapper.PaymentRecordMapper;
import com.dianjing.mapper.UserMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    private final UserMapper userMapper;
    private final OrderMapper orderMapper;
    private final PaymentRecordMapper paymentRecordMapper;

    public TestController(UserMapper userMapper, OrderMapper orderMapper, PaymentRecordMapper paymentRecordMapper) {
        this.userMapper = userMapper;
        this.orderMapper = orderMapper;
        this.paymentRecordMapper = paymentRecordMapper;
    }

    @GetMapping("/create-test-refund")
    public Result<String> createTestRefund() {
        User user = userMapper.findById(2L).orElse(null);
        if (user == null) {
            return Result.error(404, "测试用户不存在");
        }

        Order order = orderMapper.findById(1L).orElse(null);
        if (order == null) {
            return Result.error(404, "测试订单不存在");
        }

        PaymentRecord refundRecord = new PaymentRecord();
        refundRecord.setUserId(user.getId());
        refundRecord.setOrderId(order.getId());
        refundRecord.setAmount(new BigDecimal("50.00"));
        refundRecord.setType(Constants.PAYMENT_TYPE_REFUND);
        refundRecord.setPaymentMethod("system");
        refundRecord.setStatus(Constants.PAYMENT_STATUS_SUCCESS);
        refundRecord.setTransactionNo(UUID.randomUUID().toString().replace("-", ""));
        paymentRecordMapper.save(refundRecord);

        user.setBalance(user.getBalance().add(new BigDecimal("50.00")));
        userMapper.save(user);

        return Result.success("测试退款记录创建成功！用户ID: " + user.getId() + ", 退款金额: 50.00元");
    }
}
