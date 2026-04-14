package com.dianjing.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderDetailResponse {
    private Long id;
    private String orderNo;
    private Long userId;
    private String userName;
    private Long providerId;
    private String providerName;
    private Long serviceId;
    private String serviceTitle;
    private BigDecimal totalAmount;
    private Integer status;
    private String requirements;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime paymentTime;
    private String paymentMethod;
    private LocalDateTime createdAt;
}
