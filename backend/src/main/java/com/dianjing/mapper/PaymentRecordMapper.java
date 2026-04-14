package com.dianjing.mapper;

import com.dianjing.entity.PaymentRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;

public interface PaymentRecordMapper extends JpaRepository<PaymentRecord, Long> {
    Page<PaymentRecord> findByUserIdOrderByIdDesc(Long userId, Pageable pageable);
    Page<PaymentRecord> findByUserIdAndTypeOrderByIdDesc(Long userId, Integer type, Pageable pageable);
    Page<PaymentRecord> findByType(Integer type, Pageable pageable);
    Page<PaymentRecord> findByStatus(Integer status, Pageable pageable);
    Page<PaymentRecord> findByTypeAndStatus(Integer type, Integer status, Pageable pageable);
    Page<PaymentRecord> findByTypeOrderByIdDesc(Integer type, Pageable pageable);
    Page<PaymentRecord> findByStatusOrderByIdDesc(Integer status, Pageable pageable);
    Page<PaymentRecord> findByTypeAndStatusOrderByIdDesc(Integer type, Integer status, Pageable pageable);
    
    @Query("SELECT p FROM PaymentRecord p WHERE p.userId = :userId OR p.orderId = :orderId ORDER BY p.id DESC")
    Page<PaymentRecord> findByUserIdOrOrderId(@Param("userId") Long userId, @Param("orderId") Long orderId, Pageable pageable);

    @Query("SELECT SUM(p.amount) FROM PaymentRecord p WHERE p.userId = :userId AND p.type = 0 AND p.status = 1")
    BigDecimal getTotalSpentByUser(@Param("userId") Long userId);

    @Query("SELECT SUM(p.amount) FROM PaymentRecord p WHERE p.userId = :userId AND p.type = 1 AND p.status = 1")
    BigDecimal getTotalRechargedByUser(@Param("userId") Long userId);

    @Query("SELECT SUM(p.amount) FROM PaymentRecord p WHERE p.type = 0 AND p.status = 1")
    BigDecimal getTotalSpentAll();

    @Query("SELECT SUM(p.amount) FROM PaymentRecord p WHERE p.type = 1 AND p.status = 1")
    BigDecimal getTotalRechargedAll();

    @Query("SELECT SUM(p.amount) FROM PaymentRecord p WHERE p.type = 2 AND p.status = 1")
    BigDecimal getTotalWithdrawAll();
    
    @Query("SELECT SUM(p.amount) FROM PaymentRecord p WHERE p.type = 5 AND p.status = 1")
    BigDecimal getTotalPlatformFeeAll();
}
