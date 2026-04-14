package com.dianjing.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "game_account")
public class GameAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "game_type", nullable = false, length = 20)
    private String gameType;

    @Column(name = "account_name", nullable = false, length = 50)
    private String accountName;

    @Column(name = "account_password", length = 100)
    private String accountPassword;

    @Column(name = "account_level", length = 20)
    private String accountLevel;

    @Column(name = "region", length = 50)
    private String region;

    @Column(name = "account_description", columnDefinition = "TEXT")
    private String accountDescription;

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
