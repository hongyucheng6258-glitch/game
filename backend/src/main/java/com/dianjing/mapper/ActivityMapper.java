package com.dianjing.mapper;

import com.dianjing.entity.Activity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ActivityMapper extends JpaRepository<Activity, Long> {
    Page<Activity> findAllByOrderByIdDesc(Pageable pageable);

    @Query("SELECT a FROM Activity a WHERE a.status = 1 AND a.startTime <= :now AND a.endTime >= :now ORDER BY a.id DESC")
    List<Activity> findActiveActivities(@Param("now") LocalDateTime now);

    @Query("SELECT a FROM Activity a WHERE a.status = 1 AND a.startTime <= :now AND a.endTime >= :now AND a.type = 0 ORDER BY a.id DESC")
    List<Activity> findActiveGlobalActivities(@Param("now") LocalDateTime now);

    @Query("SELECT a FROM Activity a WHERE a.status = 1 AND a.type IN (1, 2) AND a.startTime <= :now AND a.endTime >= :now ORDER BY a.id DESC")
    List<Activity> findActiveServiceActivities(@Param("now") LocalDateTime now);
}
