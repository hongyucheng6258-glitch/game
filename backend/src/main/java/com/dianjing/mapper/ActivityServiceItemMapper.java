package com.dianjing.mapper;

import com.dianjing.entity.ActivityServiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityServiceItemMapper extends JpaRepository<ActivityServiceItem, Long> {
    List<ActivityServiceItem> findByActivityId(Long activityId);

    List<ActivityServiceItem> findByServiceId(Long serviceId);

    void deleteByActivityId(Long activityId);
}
