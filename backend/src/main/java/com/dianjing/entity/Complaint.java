package com.dianjing.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "complaints")
public class Complaint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "complainant_id", nullable = false)
    private Long complainantId;

    @Column(name = "respondent_id", nullable = false)
    private Long respondentId;

    @Column(nullable = false)
    private Integer type;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "evidence_images", columnDefinition = "TEXT")
    private String evidenceImages;

    @Column(nullable = false)
    private Integer status = 0;

    @Column(name = "arbitration_result", columnDefinition = "TEXT")
    private String arbitrationResult;

    @Column(name = "arbitrator_id")
    private Long arbitratorId;

    @Column(name = "arbitrated_at")
    private LocalDateTime arbitratedAt;

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
