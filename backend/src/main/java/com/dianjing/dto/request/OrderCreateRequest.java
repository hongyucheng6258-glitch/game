package com.dianjing.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderCreateRequest {
    @NotNull(message = "服务ID不能为空")
    private Long serviceId;

    private Long gameAccountId;
    private String requirements;
    
    private BigDecimal negotiatedPrice;
    private Long priceNegotiationMessageId;
}
