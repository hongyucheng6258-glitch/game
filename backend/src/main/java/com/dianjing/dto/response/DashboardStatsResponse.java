package com.dianjing.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DashboardStatsResponse {
    private long totalUsers;
    private long totalProviders;
    private long totalOrders;
    private long todayOrders;
    private BigDecimal totalRevenue;
    private BigDecimal todayRevenue;
    private long totalServices;
    private long pendingWithdrawals;
}
