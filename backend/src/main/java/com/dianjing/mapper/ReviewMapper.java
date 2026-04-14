package com.dianjing.mapper;

import com.dianjing.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ReviewMapper extends JpaRepository<Review, Long> {
    Page<Review> findByServiceIdOrderByIdDesc(Long serviceId, Pageable pageable);
    Page<Review> findByProviderIdOrderByIdDesc(Long providerId, Pageable pageable);
    List<Review> findByOrderId(Long orderId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.providerId = :providerId")
    Double getAverageRatingByProvider(@Param("providerId") Long providerId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.providerId = :providerId")
    long countByProviderId(@Param("providerId") Long providerId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.serviceId = :serviceId")
    long countByServiceId(@Param("serviceId") Long serviceId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.serviceId = :serviceId")
    Double getAverageRatingByService(@Param("serviceId") Long serviceId);

    Page<Review> findByUserIdOrderByIdDesc(Long userId, Pageable pageable);
    
    Page<Review> findAllByOrderByIdDesc(Pageable pageable);
    
    @Query("SELECT r FROM Review r WHERE r.rating = :rating ORDER BY r.id DESC")
    Page<Review> findByRating(@Param("rating") Integer rating, Pageable pageable);
}
