package com.dianjing.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentRequest {
    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    @NotBlank(message = "支付方式不能为空")
    private String paymentMethod;
}
