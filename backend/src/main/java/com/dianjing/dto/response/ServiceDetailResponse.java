package com.dianjing.dto.response;

import com.dianjing.entity.Service;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ServiceDetailResponse {
    private Long id;
    private Long providerId;
    private String providerName;
    private String providerAvatar;
    private String gameType;
    private Integer serviceType;
    private String title;
    private String description;
    private BigDecimal price;
    private Integer duration;
    private Integer status;
    private BigDecimal rating;
    private Integer reviewCount;
    private Integer salesCount;
    private List<String> tags;
    private LocalDateTime createdAt;

    private Long activityId;
    private String activityTitle;
    private BigDecimal activityPrice;
    private BigDecimal activityDiscountRate;
    private Integer activityType;

    public static ServiceDetailResponse fromEntity(Service service) {
        ServiceDetailResponse resp = new ServiceDetailResponse();
        resp.setId(service.getId());
        resp.setProviderId(service.getProviderId());
        resp.setGameType(service.getGameType());
        resp.setServiceType(service.getServiceType());
        resp.setTitle(service.getTitle());
        resp.setDescription(service.getDescription());
        resp.setPrice(service.getPrice());
        resp.setDuration(service.getDuration());
        resp.setStatus(service.getStatus());
        resp.setRating(service.getRating());
        resp.setReviewCount(service.getReviewCount());
        resp.setSalesCount(service.getSalesCount());
        resp.setCreatedAt(service.getCreatedAt());
        return resp;
    }
}
