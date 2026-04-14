package com.dianjing.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class UserLevelInfoResponse {
    private Integer currentLevel;
    private String currentLevelName;
    private String currentLevelIcon;
    private Integer experience;
    private Integer currentLevelRequiredExp;
    private Integer nextLevelRequiredExp;
    private Integer expToNextLevel;
    private BigDecimal discountRate;
    private BigDecimal withdrawalFeeDiscount;
    private Integer dailyWithdrawalLimit;
    private BigDecimal maxServicePrice;
    private BigDecimal totalExpense;
}
