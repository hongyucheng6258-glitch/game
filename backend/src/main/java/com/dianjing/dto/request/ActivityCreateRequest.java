package com.dianjing.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ActivityCreateRequest {
    @NotBlank(message = "活动标题不能为空")
    private String title;

    private String description;

    @NotNull(message = "活动类型不能为空")
    private Integer type;

    private BigDecimal discountRate;

    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    private String image;

    private List<ActivityServiceItem> services;

    @Data
    public static class ActivityServiceItem {
        @NotNull(message = "服务ID不能为空")
        private Long serviceId;

        private BigDecimal specialPrice;
    }
}
