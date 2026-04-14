package com.dianjing.mapper;

import com.dianjing.entity.SystemAnnouncement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemAnnouncementMapper extends JpaRepository<SystemAnnouncement, Long> {
    Page<SystemAnnouncement> findByIsPublishedOrderByPublishTimeDesc(Integer isPublished, Pageable pageable);
    Page<SystemAnnouncement> findAllByOrderByIdDesc(Pageable pageable);
}
