package com.dianjing.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ReviewCreateRequest {
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低为1")
    @Max(value = 5, message = "评分最高为5")
    private Integer rating;

    private String content;
    private Boolean isAnonymous = false;
}
