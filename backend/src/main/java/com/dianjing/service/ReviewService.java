package com.dianjing.service;

import com.dianjing.dto.request.ReviewCreateRequest;
import com.dianjing.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface ReviewService {

    Review create(Long userId, ReviewCreateRequest request);

    Page<Review> getByServiceId(Long serviceId, Pageable pageable);

    Page<Review> getByProviderId(Long providerId, Pageable pageable);

    void reply(Long providerId, Long reviewId, String reply);

    Map<String, Object> getProviderReviewStats(Long providerId);

    Map<String, Object> getServiceReviewStats(Long serviceId);

    Page<Review> getByUserId(Long userId, Pageable pageable);
    
    Page<Review> getAll(Pageable pageable);
    
    Page<Review> getByRating(Integer rating, Pageable pageable);
    
    void delete(Long id);
}
