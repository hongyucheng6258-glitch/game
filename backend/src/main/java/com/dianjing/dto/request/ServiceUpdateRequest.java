package com.dianjing.dto.request;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ServiceUpdateRequest {
    private String gameType;
    private Integer serviceType;
    private String title;
    private String description;
    private BigDecimal price;
    private Integer duration;
    private List<Long> tagIds;
}
