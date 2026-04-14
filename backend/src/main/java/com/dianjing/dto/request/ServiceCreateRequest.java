package com.dianjing.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ServiceCreateRequest {
    @NotBlank(message = "游戏类型不能为空")
    private String gameType;

    @NotNull(message = "服务类型不能为空")
    private Integer serviceType;

    @NotBlank(message = "服务标题不能为空")
    @Size(max = 100, message = "标题长度不能超过100个字符")
    private String title;

    @NotBlank(message = "服务描述不能为空")
    private String description;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "1.00", message = "价格不能低于1元")
    private BigDecimal price;

    @NotNull(message = "服务时长不能为空")
    @Min(value = 30, message = "服务时长不能少于30分钟")
    private Integer duration;

    private List<Long> tagIds;
}
