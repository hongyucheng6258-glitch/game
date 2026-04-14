package com.dianjing.service;

import com.dianjing.dto.request.ActivityCreateRequest;
import com.dianjing.dto.response.ActivityResponse;
import com.dianjing.entity.Activity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ActivityService {
    Activity create(Long adminId, ActivityCreateRequest request);
    Activity update(Long adminId, Long id, ActivityCreateRequest request);
    void delete(Long adminId, Long id);
    Activity publish(Long adminId, Long id);
    Activity end(Long adminId, Long id);
    Activity getById(Long id);
    ActivityResponse getDetailResponse(Long id);
    Page<Activity> getAll(Pageable pageable);
    List<ActivityResponse> getActiveActivities();
    Map<String, Object> getActivityPriceForService(Long serviceId, BigDecimal servicePrice);
    void updateActivityStatuses();
}
