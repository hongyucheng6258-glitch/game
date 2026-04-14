package com.dianjing.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PriceNegotiationResponse {
    @NotNull(message = "服务ID不能为空")
    private Long serviceId;

    @NotNull(message = "协商价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    private BigDecimal negotiatedPrice;

    @NotNull(message = "原始请求ID不能为空")
    private Long requestMessageId;

    private String remark;
}
