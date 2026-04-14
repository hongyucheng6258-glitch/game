package com.dianjing.controller.admin;

import com.dianjing.common.PageResult;
import com.dianjing.common.Result;
import com.dianjing.dto.request.ActivityCreateRequest;
import com.dianjing.dto.response.ActivityResponse;
import com.dianjing.entity.Activity;
import com.dianjing.service.ActivityService;
import com.dianjing.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/activities")
@PreAuthorize("hasRole('ADMIN')")
public class AdminActivityController {

    private final ActivityService activityService;
    private final UserService userService;

    public AdminActivityController(ActivityService activityService, UserService userService) {
        this.activityService = activityService;
        this.userService = userService;
    }

    private Long getCurrentAdminId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getUserByUsername(username).getId();
    }

    @GetMapping
    public Result<PageResult<Activity>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page - 1, size);
        Page<Activity> pageResult = activityService.getAll(pageable);
        return Result.success(new PageResult<>(pageResult.getTotalElements(),
                pageResult.getTotalPages(), page, pageResult.getContent()));
    }

    @PostMapping
    public Result<Activity> create(@Valid @RequestBody ActivityCreateRequest request) {
        Long adminId = getCurrentAdminId();
        Activity activity = activityService.create(adminId, request);
        return Result.success(activity);
    }

    @GetMapping("/{id}")
    public Result<ActivityResponse> getDetail(@PathVariable Long id) {
        ActivityResponse response = activityService.getDetailResponse(id);
        return Result.success(response);
    }

    @PutMapping("/{id}")
    public Result<Activity> update(@PathVariable Long id, @Valid @RequestBody ActivityCreateRequest request) {
        Long adminId = getCurrentAdminId();
        Activity activity = activityService.update(adminId, id, request);
        return Result.success(activity);
    }

    @PostMapping("/{id}/publish")
    public Result<Activity> publish(@PathVariable Long id) {
        Long adminId = getCurrentAdminId();
        return Result.success(activityService.publish(adminId, id));
    }

    @PostMapping("/{id}/end")
    public Result<Activity> end(@PathVariable Long id) {
        Long adminId = getCurrentAdminId();
        return Result.success(activityService.end(adminId, id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long adminId = getCurrentAdminId();
        activityService.delete(adminId, id);
        return Result.success();
    }

    @PostMapping("/update-statuses")
    public Result<Void> updateStatuses() {
        activityService.updateActivityStatuses();
        return Result.success();
    }
}
