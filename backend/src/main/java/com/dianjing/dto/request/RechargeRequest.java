package com.dianjing.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class RechargeRequest {
    @NotNull(message = "充值金额不能为空")
    @DecimalMin(value = "1.00", message = "充值金额不能低于1元")
    private BigDecimal amount;

    @NotBlank(message = "充值方式不能为空")
    private String paymentMethod;
}
