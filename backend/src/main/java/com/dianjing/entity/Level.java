package com.dianjing.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_level")
public class Level {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer level;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private Integer requiredExp;

    @Column(name = "discount_rate", precision = 5, scale = 2)
    private BigDecimal discountRate = BigDecimal.ONE;

    @Column(name = "withdrawal_fee_discount", precision = 5, scale = 2)
    private BigDecimal withdrawalFeeDiscount = BigDecimal.ONE;

    @Column(name = "daily_withdrawal_limit")
    private Integer dailyWithdrawalLimit;

    @Column(name = "max_service_price", precision = 10, scale = 2)
    private BigDecimal maxServicePrice;

    @Column(length = 255)
    private String icon;

    @Column(length = 500)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
