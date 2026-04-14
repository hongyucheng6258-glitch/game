package com.dianjing.mapper;

import com.dianjing.entity.WithdrawalApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface WithdrawalApplicationMapper extends JpaRepository<WithdrawalApplication, Long> {
    Page<WithdrawalApplication> findByUserIdOrderByIdDesc(Long userId, Pageable pageable);
    Page<WithdrawalApplication> findByStatusOrderByIdDesc(Integer status, Pageable pageable);
    
    @Query("SELECT COUNT(w) FROM WithdrawalApplication w WHERE w.userId = :userId AND w.createdAt >= :startTime")
    long countByUserIdAndCreatedAtAfter(@Param("userId") Long userId, @Param("startTime") LocalDateTime startTime);
}
