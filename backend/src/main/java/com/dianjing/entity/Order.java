package com.dianjing.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    @Column(name = "service_id", nullable = false)
    private Long serviceId;

    @Column(name = "order_no", nullable = false, unique = true, length = 32)
    private String orderNo;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private Integer status = 0;

    @Column(name = "game_account_id")
    private Long gameAccountId;

    @Column(columnDefinition = "TEXT")
    private String requirements;

    @Column(name = "is_negotiated_price")
    private Integer isNegotiatedPrice = 0;

    @Column(name = "original_price", precision = 10, scale = 2)
    private BigDecimal originalPrice;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "payment_time")
    private LocalDateTime paymentTime;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "price_negotiation_message_id")
    private Long priceNegotiationMessageId;

    @Column(name = "activity_id")
    private Long activityId;

    @Column(name = "activity_price", precision = 10, scale = 2)
    private BigDecimal activityPrice;

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
