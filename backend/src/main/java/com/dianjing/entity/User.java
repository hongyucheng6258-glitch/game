package com.dianjing.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(unique = true, length = 20)
    private String phone;

    @Column(unique = true, length = 50)
    private String email;

    @Column(length = 255)
    private String avatar;

    @Column(length = 20)
    private String realName;

    @Column(unique = true, length = 20)
    private String idCard;

    @Column(nullable = false)
    private Integer role = 0;

    @Column(nullable = false)
    private Integer status = 0;

    @Column(precision = 10, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "credit_score")
    private Integer creditScore = 100;

    @Column(name = "current_level")
    private Integer currentLevel = 1;

    @Column(name = "experience")
    private Integer experience = 0;

    @Column(name = "total_expense", precision = 12, scale = 2)
    private BigDecimal totalExpense = BigDecimal.ZERO;

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
