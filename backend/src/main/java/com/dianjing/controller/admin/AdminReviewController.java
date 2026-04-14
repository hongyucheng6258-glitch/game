package com.dianjing.controller.admin;

import com.dianjing.common.PageResult;
import com.dianjing.common.Result;
import com.dianjing.entity.Review;
import com.dianjing.service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/reviews")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReviewController {

    private final ReviewService reviewService;

    public AdminReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * 评价列表（管理员）
     */
    @GetMapping
    public Result<PageResult<Review>> list(
            @RequestParam(required = false) Long providerId,
            @RequestParam(required = false) Integer rating,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page - 1, size);
        Page<Review> reviewPage;
        
        if (providerId != null) {
            reviewPage = reviewService.getByProviderId(providerId, pageable);
        } else if (rating != null) {
            reviewPage = reviewService.getByRating(rating, pageable);
        } else {
            reviewPage = reviewService.getAll(pageable);
        }
        
        return Result.success(new PageResult<>(reviewPage.getTotalElements(),
                reviewPage.getTotalPages(), page, reviewPage.getContent()));
    }
    
    /**
     * 删除评价
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        reviewService.delete(id);
        return Result.success(null);
    }
}
