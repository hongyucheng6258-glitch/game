package com.dianjing.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ActivityResponse {
    private Long id;
    private String title;
    private String description;
    private Integer type;
    private BigDecimal discountRate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private String image;
    private List<ActivityServiceItem> services;

    @Data
    public static class ActivityServiceItem {
        private Long serviceId;
        private String serviceTitle;
        private BigDecimal servicePrice;
        private BigDecimal specialPrice;
    }
}
