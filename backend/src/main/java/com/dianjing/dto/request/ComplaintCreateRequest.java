package com.dianjing.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ComplaintCreateRequest {
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotNull(message = "投诉类型不能为空")
    private Integer type;

    @NotNull(message = "投诉内容不能为空")
    @Size(min = 10, max = 2000, message = "投诉内容长度应在10-2000字符之间")
    private String content;

    private String evidenceImages;
}
