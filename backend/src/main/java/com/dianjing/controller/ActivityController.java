package com.dianjing.controller;

import com.dianjing.common.Result;
import com.dianjing.dto.response.ActivityResponse;
import com.dianjing.service.ActivityService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/activities")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping
    public Result<List<ActivityResponse>> getActiveActivities() {
        return Result.success(activityService.getActiveActivities());
    }

    @GetMapping("/{id}")
    public Result<ActivityResponse> getDetail(@PathVariable Long id) {
        com.dianjing.entity.Activity activity = activityService.getById(id);
        com.dianjing.dto.response.ActivityResponse response = new com.dianjing.dto.response.ActivityResponse();
        response.setId(activity.getId());
        response.setTitle(activity.getTitle());
        response.setDescription(activity.getDescription());
        response.setType(activity.getType());
        response.setDiscountRate(activity.getDiscountRate());
        response.setStartTime(activity.getStartTime());
        response.setEndTime(activity.getEndTime());
        response.setStatus(activity.getStatus());
        response.setImage(activity.getImage());
        return Result.success(response);
    }
}
