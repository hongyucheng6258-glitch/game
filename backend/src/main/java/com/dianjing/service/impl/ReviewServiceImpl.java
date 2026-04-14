package com.dianjing.service.impl;

import com.dianjing.common.BusinessException;
import com.dianjing.common.Constants;
import com.dianjing.dto.request.ReviewCreateRequest;
import com.dianjing.entity.Order;
import com.dianjing.entity.Review;
import com.dianjing.entity.User;
import com.dianjing.mapper.OrderMapper;
import com.dianjing.mapper.ReviewMapper;
import com.dianjing.mapper.ServiceMapper;
import com.dianjing.service.ReviewService;
import com.dianjing.service.UserService;
import com.dianjing.service.StatisticsCacheService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewMapper reviewMapper;
    private final OrderMapper orderMapper;
    private final ServiceMapper serviceMapper;
    private final UserService userService;
    private final StatisticsCacheService statisticsCacheService;

    public ReviewServiceImpl(ReviewMapper reviewMapper, OrderMapper orderMapper,
                             ServiceMapper serviceMapper, UserService userService, StatisticsCacheService statisticsCacheService) {
        this.reviewMapper = reviewMapper;
        this.orderMapper = orderMapper;
        this.serviceMapper = serviceMapper;
        this.userService = userService;
        this.statisticsCacheService = statisticsCacheService;
    }

    @Override
    @Transactional
    public Review create(Long userId, ReviewCreateRequest request) {
        Order order = orderMapper.findById(request.getOrderId())
                .orElseThrow(() -> new BusinessException(404, "订单不存在"));
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权评价此订单");
        }
        if (order.getStatus() != Constants.ORDER_PENDING_REVIEW &&
            order.getStatus() != Constants.ORDER_COMPLETED) {
            throw new BusinessException(400, "订单状态不允许评价");
        }

        // 检查是否已评价
        List<Review> existingReviews = reviewMapper.findByOrderId(order.getId());
        if (!existingReviews.isEmpty()) {
            throw new BusinessException(400, "该订单已评价");
        }

        Review review = new Review();
        review.setOrderId(order.getId());
        review.setUserId(userId);
        review.setProviderId(order.getProviderId());
        review.setServiceId(order.getServiceId());
        review.setRating(request.getRating());
        review.setContent(request.getContent());
        review.setIsAnonymous(request.getIsAnonymous() != null ? request.getIsAnonymous() : false);
        review = reviewMapper.save(review);

        // 更新服务评分
        updateServiceRating(order.getServiceId());

        // 更新订单状态
        if (order.getStatus() == Constants.ORDER_PENDING_REVIEW) {
            order.setStatus(Constants.ORDER_COMPLETED);
            orderMapper.save(order);
        }

        // 更新排行榜缓存
        statisticsCacheService.evictAllStatisticsCaches();
        
        return review;
    }

    @Override
    public Page<Review> getByServiceId(Long serviceId, Pageable pageable) {
        Page<Review> reviewPage = reviewMapper.findByServiceIdOrderByIdDesc(serviceId, pageable);
        return reviewPage.map(review -> {
            try {
                User user = userService.getUserById(review.getUserId());
                // 使用反射为 Review 对象添加额外字段
                java.lang.reflect.Field userNameField = Review.class.getDeclaredField("userName");
                userNameField.setAccessible(true);
                userNameField.set(review, user.getUsername());
                
                java.lang.reflect.Field userAvatarField = Review.class.getDeclaredField("userAvatar");
                userAvatarField.setAccessible(true);
                userAvatarField.set(review, user.getAvatar());
            } catch (Exception e) {
                // 忽略异常，保持原有数据
            }
            return review;
        });
    }

    @Override
    public Page<Review> getByProviderId(Long providerId, Pageable pageable) {
        Page<Review> reviewPage = reviewMapper.findByProviderIdOrderByIdDesc(providerId, pageable);
        return reviewPage.map(review -> {
            try {
                User user = userService.getUserById(review.getUserId());
                // 使用反射为 Review 对象添加额外字段
                java.lang.reflect.Field userNameField = Review.class.getDeclaredField("userName");
                userNameField.setAccessible(true);
                userNameField.set(review, user.getUsername());
                
                java.lang.reflect.Field userAvatarField = Review.class.getDeclaredField("userAvatar");
                userAvatarField.setAccessible(true);
                userAvatarField.set(review, user.getAvatar());
            } catch (Exception e) {
                // 忽略异常，保持原有数据
            }
            return review;
        });
    }

    @Override
    @Transactional
    public void reply(Long providerId, Long reviewId, String reply) {
        Review review = reviewMapper.findById(reviewId)
                .orElseThrow(() -> new BusinessException(404, "评价不存在"));
        if (!review.getProviderId().equals(providerId)) {
            throw new BusinessException(403, "无权回复此评价");
        }
        if (review.getReply() != null && !review.getReply().isEmpty()) {
            throw new BusinessException(400, "已回复，不能重复回复");
        }
        review.setReply(reply);
        reviewMapper.save(review);
    }

    @Override
    public Map<String, Object> getProviderReviewStats(Long providerId) {
        Map<String, Object> stats = new HashMap<>();
        Double avgRating = reviewMapper.getAverageRatingByProvider(providerId);
        long totalCount = reviewMapper.countByProviderId(providerId);
        stats.put("averageRating", avgRating != null ? BigDecimal.valueOf(avgRating).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        stats.put("totalReviews", totalCount);
        return stats;
    }

    @Override
    public Map<String, Object> getServiceReviewStats(Long serviceId) {
        Map<String, Object> stats = new HashMap<>();
        long totalCount = reviewMapper.countByServiceId(serviceId);
        stats.put("totalReviews", totalCount);
        serviceMapper.findById(serviceId).ifPresent(service -> {
            stats.put("averageRating", service.getRating());
            stats.put("reviewCount", service.getReviewCount());
        });
        return stats;
    }

    @Override
    public Page<Review> getByUserId(Long userId, Pageable pageable) {
        return reviewMapper.findByUserIdOrderByIdDesc(userId, pageable);
    }
    
    @Override
    public Page<Review> getAll(Pageable pageable) {
        Page<Review> reviewPage = reviewMapper.findAllByOrderByIdDesc(pageable);
        return reviewPage.map(review -> {
            try {
                User user = userService.getUserById(review.getUserId());
                // 使用反射为 Review 对象添加额外字段
                java.lang.reflect.Field userNameField = Review.class.getDeclaredField("userName");
                userNameField.setAccessible(true);
                userNameField.set(review, user.getUsername());
                
                java.lang.reflect.Field userAvatarField = Review.class.getDeclaredField("userAvatar");
                userAvatarField.setAccessible(true);
                userAvatarField.set(review, user.getAvatar());
            } catch (Exception e) {
                // 忽略异常，保持原有数据
            }
            return review;
        });
    }
    
    @Override
    public Page<Review> getByRating(Integer rating, Pageable pageable) {
        Page<Review> reviewPage = reviewMapper.findByRating(rating, pageable);
        return reviewPage.map(review -> {
            try {
                User user = userService.getUserById(review.getUserId());
                // 使用反射为 Review 对象添加额外字段
                java.lang.reflect.Field userNameField = Review.class.getDeclaredField("userName");
                userNameField.setAccessible(true);
                userNameField.set(review, user.getUsername());
                
                java.lang.reflect.Field userAvatarField = Review.class.getDeclaredField("userAvatar");
                userAvatarField.setAccessible(true);
                userAvatarField.set(review, user.getAvatar());
            } catch (Exception e) {
                // 忽略异常，保持原有数据
            }
            return review;
        });
    }
    
    @Override
    public void delete(Long id) {
        reviewMapper.deleteById(id);
    }

    private void updateServiceRating(Long serviceId) {
        serviceMapper.findById(serviceId).ifPresent(service -> {
            List<Review> reviews = reviewMapper.findByServiceIdOrderByIdDesc(serviceId,
                    org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE)).getContent();
            if (!reviews.isEmpty()) {
                double avg = reviews.stream()
                        .mapToInt(Review::getRating)
                        .average()
                        .orElse(0.0);
                service.setRating(BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP));
                service.setReviewCount(reviews.size());
                serviceMapper.save(service);
            }
        });
    }
}
