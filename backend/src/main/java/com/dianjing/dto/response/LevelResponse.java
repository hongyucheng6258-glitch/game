package com.dianjing.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class LevelResponse {
    private Long id;
    private Integer level;
    private String name;
    private Integer requiredExp;
    private BigDecimal discountRate;
    private BigDecimal withdrawalFeeDiscount;
    private Integer dailyWithdrawalLimit;
    private BigDecimal maxServicePrice;
    private String icon;
    private String description;
}
