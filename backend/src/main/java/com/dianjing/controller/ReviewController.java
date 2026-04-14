package com.dianjing.controller;

import com.dianjing.common.PageResult;
import com.dianjing.common.Result;
import com.dianjing.dto.request.ReviewCreateRequest;
import com.dianjing.entity.Review;
import com.dianjing.service.ReviewService;
import com.dianjing.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final UserService userService;

    public ReviewController(ReviewService reviewService, UserService userService) {
        this.reviewService = reviewService;
        this.userService = userService;
    }

    private Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getUserByUsername(username).getId();
    }

    /**
     * 创建评价
     */
    @PostMapping
    public Result<Review> create(@Valid @RequestBody ReviewCreateRequest request) {
        Long userId = getCurrentUserId();
        Review review = reviewService.create(userId, request);
        return Result.success(review);
    }

    /**
     * 公开接口 - 查看服务的评价列表
     */
    @GetMapping("/service/{serviceId}")
    public Result<PageResult<Review>> listByService(
            @PathVariable Long serviceId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page - 1, size);
        Page<Review> reviewPage = reviewService.getByServiceId(serviceId, pageable);
        return Result.success(new PageResult<>(reviewPage.getTotalElements(),
                reviewPage.getTotalPages(), page, reviewPage.getContent()));
    }

    /**
     * 查看服务商的评价列表
     */
    @GetMapping("/provider/{providerId}")
    public Result<PageResult<Review>> listByProvider(
            @PathVariable Long providerId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page - 1, size);
        Page<Review> reviewPage = reviewService.getByProviderId(providerId, pageable);
        return Result.success(new PageResult<>(reviewPage.getTotalElements(),
                reviewPage.getTotalPages(), page, reviewPage.getContent()));
    }

    /**
     * 服务商 - 回复评价
     */
    @PostMapping("/{id}/reply")
    public Result<Void> reply(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Long userId = getCurrentUserId();
        String replyContent = body.get("reply");
        reviewService.reply(userId, id, replyContent);
        return Result.success();
    }

    /**
     * 公开接口 - 服务商评价统计
     */
    @GetMapping("/stats/{providerId}")
    public Result<Map<String, Object>> getProviderStats(@PathVariable Long providerId) {
        return Result.success(reviewService.getProviderReviewStats(providerId));
    }

    /**
     * 公开接口 - 服务评价统计
     */
    @GetMapping("/service-stats/{serviceId}")
    public Result<Map<String, Object>> getServiceStats(@PathVariable Long serviceId) {
        return Result.success(reviewService.getServiceReviewStats(serviceId));
    }

    /**
     * 用户 - 我的评价列表
     */
    @GetMapping("/my")
    public Result<PageResult<Review>> listMyReviews(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = getCurrentUserId();
        PageRequest pageable = PageRequest.of(page - 1, size);
        Page<Review> reviewPage = reviewService.getByUserId(userId, pageable);
        return Result.success(new PageResult<>(reviewPage.getTotalElements(),
                reviewPage.getTotalPages(), page, reviewPage.getContent()));
    }
}
