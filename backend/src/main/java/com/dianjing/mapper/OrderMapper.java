package com.dianjing.mapper;

import com.dianjing.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderMapper extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNo(String orderNo);
    Page<Order> findByUserIdOrderByIdDesc(Long userId, Pageable pageable);
    Page<Order> findByProviderIdOrderByIdDesc(Long providerId, Pageable pageable);
    Page<Order> findByUserIdAndStatusOrderByIdDesc(Long userId, Integer status, Pageable pageable);
    Page<Order> findByProviderIdAndStatusOrderByIdDesc(Long providerId, Integer status, Pageable pageable);
    Page<Order> findAllByOrderByIdDesc(Pageable pageable);
    Page<Order> findAllByStatusOrderByIdDesc(Integer status, Pageable pageable);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.providerId = :providerId AND o.status = :status")
    long countByProviderIdAndStatus(@Param("providerId") Long providerId, @Param("status") Integer status);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.serviceId = :serviceId AND o.status = :status")
    long countByServiceIdAndStatus(@Param("serviceId") Long serviceId, @Param("status") Integer status);

    long countByUserId(Long userId);
    long countByProviderId(Long providerId);
    boolean existsByPriceNegotiationMessageId(Long priceNegotiationMessageId);

    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.createdAt < :beforeTime")
    List<Order> findByStatusAndCreatedAtBefore(@Param("status") Integer status, @Param("beforeTime") LocalDateTime beforeTime);

    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.paymentTime < :beforeTime")
    List<Order> findByStatusAndPaymentTimeBefore(@Param("status") Integer status, @Param("beforeTime") LocalDateTime beforeTime);

    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.startTime < :beforeTime")
    List<Order> findByStatusAndStartTimeBefore(@Param("status") Integer status, @Param("beforeTime") LocalDateTime beforeTime);
}
