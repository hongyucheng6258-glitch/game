package com.dianjing.service.impl;

import com.dianjing.common.Constants;
import com.dianjing.dto.response.DashboardStatsResponse;
import com.dianjing.entity.Order;
import com.dianjing.entity.User;
import com.dianjing.mapper.OrderMapper;
import com.dianjing.mapper.PaymentRecordMapper;
import com.dianjing.mapper.ServiceMapper;
import com.dianjing.mapper.UserMapper;
import com.dianjing.mapper.ReviewMapper;
import com.dianjing.mapper.WithdrawalApplicationMapper;
import com.dianjing.service.StatisticsCacheService;
import com.dianjing.service.StatisticsService;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private final UserMapper userMapper;
    private final OrderMapper orderMapper;
    private final ServiceMapper serviceMapper;
    private final PaymentRecordMapper paymentRecordMapper;
    private final ReviewMapper reviewMapper;
    private final WithdrawalApplicationMapper withdrawalApplicationMapper;
    private final StatisticsCacheService statisticsCacheService;

    public StatisticsServiceImpl(UserMapper userMapper, OrderMapper orderMapper,
                                 ServiceMapper serviceMapper, PaymentRecordMapper paymentRecordMapper,
                                 ReviewMapper reviewMapper, WithdrawalApplicationMapper withdrawalApplicationMapper,
                                 StatisticsCacheService statisticsCacheService) {
        this.userMapper = userMapper;
        this.orderMapper = orderMapper;
        this.serviceMapper = serviceMapper;
        this.paymentRecordMapper = paymentRecordMapper;
        this.reviewMapper = reviewMapper;
        this.withdrawalApplicationMapper = withdrawalApplicationMapper;
        this.statisticsCacheService = statisticsCacheService;
    }

    @Override
    public DashboardStatsResponse getPlatformStats() {
        DashboardStatsResponse cachedStats = statisticsCacheService.getCachedPlatformStats();
        if (cachedStats != null) {
            return cachedStats;
        }
        
        DashboardStatsResponse stats = new DashboardStatsResponse();
        stats.setTotalUsers(userMapper.count());
        stats.setTotalProviders(userMapper.countByRole(Constants.ROLE_PROVIDER));
        stats.setTotalOrders(orderMapper.count());
        stats.setTotalServices(serviceMapper.count());

        // 今日订单数
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        long todayOrders = orderMapper.findAll().stream()
                .filter(o -> o.getCreatedAt() != null &&
                        o.getCreatedAt().isAfter(todayStart) && o.getCreatedAt().isBefore(todayEnd))
                .count();
        stats.setTodayOrders(todayOrders);

        // 总收入（平台手续费）
        BigDecimal totalRevenue = calculatePlatformRevenue();
        stats.setTotalRevenue(totalRevenue);

        // 今日收入
        BigDecimal todayRevenue = calculatePlatformRevenue(todayStart, todayEnd);
        stats.setTodayRevenue(todayRevenue);

        // 待处理提现
        long pendingWithdrawals = withdrawalApplicationMapper.findByStatusOrderByIdDesc(
                Constants.WITHDRAWAL_STATUS_PENDING,
                org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE)
        ).getTotalElements();
        stats.setPendingWithdrawals(pendingWithdrawals);

        statisticsCacheService.cachePlatformStats(stats);
        return stats;
    }

    @Override
    public List<Map<String, Object>> getProviderRanking(int limit) {
        List<Map<String, Object>> cachedRanking = statisticsCacheService.getCachedProviderRanking();
        if (cachedRanking != null) {
            return cachedRanking.stream().limit(limit).toList();
        }

        List<User> providers = userMapper.findAll().stream()
                .filter(u -> u.getRole() == Constants.ROLE_PROVIDER)
                .toList();

        List<Map<String, Object>> ranking = new ArrayList<>();
        for (User provider : providers) {
            Map<String, Object> providerStats = new HashMap<>();
            providerStats.put("providerId", provider.getId());
            providerStats.put("username", provider.getUsername());
            providerStats.put("avatar", provider.getAvatar());

            long totalOrders = orderMapper.countByProviderId(provider.getId());
            long completedOrders = orderMapper.countByProviderIdAndStatus(provider.getId(), Constants.ORDER_COMPLETED);
            providerStats.put("totalOrders", totalOrders);
            providerStats.put("completedOrders", completedOrders);

            // 计算评分
            List<Order> providerOrders = orderMapper.findByProviderIdOrderByIdDesc(provider.getId(),
                    PageRequest.of(0, Integer.MAX_VALUE)).getContent();
            BigDecimal totalAmount = providerOrders.stream()
                    .filter(o -> o.getStatus() == Constants.ORDER_COMPLETED)
                    .map(order -> order.getTotalAmount().multiply(BigDecimal.valueOf(1 - Constants.PLATFORM_FEE_RATE)))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            providerStats.put("totalRevenue", totalAmount);

            ranking.add(providerStats);
        }

        // 按完成订单数降序排序
        ranking.sort((a, b) -> Long.compare((Long) b.get("completedOrders"), (Long) a.get("completedOrders")));

        statisticsCacheService.cacheProviderRanking(ranking);
        return ranking.stream().limit(limit).toList();
    }

    @Override
    public Map<String, Object> getProviderStatistics(Long providerId) {
        Map<String, Object> stats = new HashMap<>();

        long totalOrders = orderMapper.countByProviderId(providerId);
        long completedOrders = orderMapper.countByProviderIdAndStatus(providerId, Constants.ORDER_COMPLETED);
        long pendingService = orderMapper.countByProviderIdAndStatus(providerId, Constants.ORDER_PENDING_SERVICE);
        long inService = orderMapper.countByProviderIdAndStatus(providerId, Constants.ORDER_IN_SERVICE);
        long pendingReview = orderMapper.countByProviderIdAndStatus(providerId, Constants.ORDER_PENDING_REVIEW);
        long cancelledOrders = orderMapper.countByProviderIdAndStatus(providerId, Constants.ORDER_CANCELLED);

        stats.put("totalOrders", totalOrders);
        stats.put("completedOrders", completedOrders);
        stats.put("pendingService", pendingService);
        stats.put("inService", inService);
        stats.put("pendingReview", pendingReview);
        stats.put("cancelledOrders", cancelledOrders);

        // 总收入
        List<Order> providerOrders = orderMapper.findByProviderIdOrderByIdDesc(providerId,
                PageRequest.of(0, Integer.MAX_VALUE)).getContent();
        BigDecimal totalRevenue = providerOrders.stream()
                .filter(o -> o.getStatus() == Constants.ORDER_COMPLETED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 实际收入（扣除手续费后）
        BigDecimal actualIncome = totalRevenue.multiply(
                BigDecimal.ONE.subtract(BigDecimal.valueOf(Constants.PLATFORM_FEE_RATE)))
                .setScale(2, RoundingMode.HALF_UP);
        stats.put("actualIncome", actualIncome);

        // 服务统计
        long totalServices = serviceMapper.countByProviderId(providerId);
        long onlineServices = serviceMapper.countByProviderIdAndStatus(providerId, Constants.SERVICE_ONLINE);

        // 前端期望的字段
        stats.put("totalServices", totalServices);
        stats.put("activeServices", onlineServices);
        stats.put("totalOrders", totalOrders);
        stats.put("totalRevenue", totalRevenue.doubleValue());
        
        // 平均评分和总评价数
        Double avgRating = reviewMapper.getAverageRatingByProvider(providerId);
        long totalReviews = reviewMapper.countByProviderId(providerId);
        stats.put("avgRating", avgRating != null ? avgRating : 0.0);
        stats.put("totalReviews", totalReviews);

        System.out.println("Provider stats for provider " + providerId + ": " + stats);
        return stats;
    }

    private BigDecimal calculatePlatformRevenue() {
        return orderMapper.findAll().stream()
                .filter(o -> o.getStatus() == Constants.ORDER_COMPLETED)
                .map(o -> o.getTotalAmount().multiply(BigDecimal.valueOf(Constants.PLATFORM_FEE_RATE)))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculatePlatformRevenue(LocalDateTime start, LocalDateTime end) {
        return orderMapper.findAll().stream()
                .filter(o -> o.getStatus() == Constants.ORDER_COMPLETED &&
                        o.getPaymentTime() != null &&
                        o.getPaymentTime().isAfter(start) && o.getPaymentTime().isBefore(end))
                .map(o -> o.getTotalAmount().multiply(BigDecimal.valueOf(Constants.PLATFORM_FEE_RATE)))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public List<Map<String, Object>> getRatingRanking(int limit) {
        List<Map<String, Object>> cachedRanking = statisticsCacheService.getCachedRatingRanking();
        if (cachedRanking != null) {
            return cachedRanking.stream().limit(limit).toList();
        }

        List<User> providers = userMapper.findAll().stream()
                .filter(u -> u.getRole() == Constants.ROLE_PROVIDER)
                .toList();

        List<Map<String, Object>> ranking = new ArrayList<>();
        for (User provider : providers) {
            Double avgRating = reviewMapper.getAverageRatingByProvider(provider.getId());
            long reviewCount = reviewMapper.countByProviderId(provider.getId());
            
            if (reviewCount > 0) {
                Map<String, Object> providerStats = new HashMap<>();
                providerStats.put("id", provider.getId());
                providerStats.put("providerName", provider.getUsername());
                providerStats.put("providerAvatar", provider.getAvatar());
                providerStats.put("rating", avgRating != null ? BigDecimal.valueOf(avgRating).setScale(1, RoundingMode.HALF_UP) : BigDecimal.ZERO);
                providerStats.put("reviewCount", reviewCount);
                
                List<com.dianjing.entity.Service> services = serviceMapper.findByProviderId(provider.getId());
                if (!services.isEmpty()) {
                    providerStats.put("gameType", services.get(0).getGameType());
                }
                
                ranking.add(providerStats);
            }
        }

        ranking.sort((a, b) -> {
            int ratingCompare = ((BigDecimal) b.get("rating")).compareTo((BigDecimal) a.get("rating"));
            if (ratingCompare != 0) return ratingCompare;
            return Long.compare((Long) b.get("reviewCount"), (Long) a.get("reviewCount"));
        });

        statisticsCacheService.cacheRatingRanking(ranking);
        return ranking.stream().limit(limit).toList();
    }

    @Override
    public List<Map<String, Object>> getSalesRanking(int limit) {
        List<Map<String, Object>> cachedRanking = statisticsCacheService.getCachedSalesRanking();
        if (cachedRanking != null) {
            return cachedRanking.stream().limit(limit).toList();
        }

        List<User> providers = userMapper.findAll().stream()
                .filter(u -> u.getRole() == Constants.ROLE_PROVIDER)
                .toList();

        List<Map<String, Object>> ranking = new ArrayList<>();
        for (User provider : providers) {
            long completedOrders = orderMapper.countByProviderIdAndStatus(provider.getId(), Constants.ORDER_COMPLETED);
            
            if (completedOrders > 0) {
                Map<String, Object> providerStats = new HashMap<>();
                providerStats.put("id", provider.getId());
                providerStats.put("providerName", provider.getUsername());
                providerStats.put("providerAvatar", provider.getAvatar());
                providerStats.put("salesCount", completedOrders);
                
                List<com.dianjing.entity.Service> services = serviceMapper.findByProviderId(provider.getId());
                if (!services.isEmpty()) {
                    providerStats.put("gameType", services.get(0).getGameType());
                }
                
                ranking.add(providerStats);
            }
        }

        ranking.sort((a, b) -> Long.compare((Long) b.get("salesCount"), (Long) a.get("salesCount")));

        statisticsCacheService.cacheSalesRanking(ranking);
        return ranking.stream().limit(limit).toList();
    }

    @Override
    public List<Map<String, Object>> getPopularRanking(int limit) {
        List<Map<String, Object>> cachedRanking = statisticsCacheService.getCachedPopularRanking();
        if (cachedRanking != null) {
            return cachedRanking.stream().limit(limit).toList();
        }

        List<User> providers = userMapper.findAll().stream()
                .filter(u -> u.getRole() == Constants.ROLE_PROVIDER)
                .toList();

        List<Map<String, Object>> ranking = new ArrayList<>();
        for (User provider : providers) {
            List<com.dianjing.entity.Service> services = serviceMapper.findByProviderId(provider.getId());
            int totalSales = services.stream()
                    .mapToInt(s -> s.getSalesCount() != null ? s.getSalesCount() : 0)
                    .sum();
            
            Double avgRating = reviewMapper.getAverageRatingByProvider(provider.getId());
            
            if (totalSales > 0) {
                Map<String, Object> providerStats = new HashMap<>();
                providerStats.put("id", provider.getId());
                providerStats.put("providerName", provider.getUsername());
                providerStats.put("providerAvatar", provider.getAvatar());
                
                double popularityScore = totalSales * 0.6 + (avgRating != null ? avgRating * 10 : 0) * 0.4;
                providerStats.put("popularity", (int) Math.round(popularityScore));
                
                if (!services.isEmpty()) {
                    providerStats.put("gameType", services.get(0).getGameType());
                }
                
                ranking.add(providerStats);
            }
        }

        ranking.sort((a, b) -> Integer.compare((Integer) b.get("popularity"), (Integer) a.get("popularity")));

        statisticsCacheService.cachePopularRanking(ranking);
        return ranking.stream().limit(limit).toList();
    }

    @Override
    public List<Map<String, Object>> getRevenueRanking(int limit) {
        List<User> providers = userMapper.findAll().stream()
                .filter(u -> u.getRole() == Constants.ROLE_PROVIDER)
                .toList();

        List<Map<String, Object>> ranking = new ArrayList<>();
        for (User provider : providers) {
            List<Order> providerOrders = orderMapper.findByProviderIdOrderByIdDesc(provider.getId(),
                    PageRequest.of(0, Integer.MAX_VALUE)).getContent();
            BigDecimal totalRevenue = providerOrders.stream()
                    .filter(o -> o.getStatus() == Constants.ORDER_COMPLETED)
                    .map(order -> order.getTotalAmount().multiply(BigDecimal.valueOf(1 - Constants.PLATFORM_FEE_RATE)))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
                Map<String, Object> providerStats = new HashMap<>();
                providerStats.put("id", provider.getId());
                providerStats.put("providerName", provider.getUsername());
                providerStats.put("providerAvatar", provider.getAvatar());
                providerStats.put("totalRevenue", totalRevenue);

                List<com.dianjing.entity.Service> services = serviceMapper.findByProviderId(provider.getId());
                if (!services.isEmpty()) {
                    providerStats.put("gameType", services.get(0).getGameType());
                }

                ranking.add(providerStats);
            }
        }

        ranking.sort((a, b) -> ((BigDecimal) b.get("totalRevenue")).compareTo((BigDecimal) a.get("totalRevenue")));

        return ranking.stream().limit(limit).toList();
    }

    @Override
    public List<Map<String, Object>> getOrderTrend(int days) {
        List<Map<String, Object>> trend = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
            
            long orderCount = orderMapper.findAll().stream()
                    .filter(o -> o.getCreatedAt() != null &&
                            o.getCreatedAt().isAfter(startOfDay) &&
                            o.getCreatedAt().isBefore(endOfDay))
                    .count();
            
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.toString());
            dayData.put("count", orderCount);
            trend.add(dayData);
        }
        
        return trend;
    }

    @Override
    public List<Map<String, Object>> getRevenueTrend(int days) {
        List<Map<String, Object>> trend = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
            
            BigDecimal dayRevenue = calculatePlatformRevenue(startOfDay, endOfDay);
            
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.toString());
            dayData.put("amount", dayRevenue);
            trend.add(dayData);
        }
        
        return trend;
    }
}
