package com.dianjing.service;

import com.dianjing.dto.request.AnnouncementCreateRequest;
import com.dianjing.entity.SystemAnnouncement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SystemAnnouncementService {

    SystemAnnouncement create(Long adminId, AnnouncementCreateRequest request);

    SystemAnnouncement update(Long adminId, Long id, AnnouncementCreateRequest request);

    void delete(Long adminId, Long id);

    void publish(Long adminId, Long id);

    Page<SystemAnnouncement> getPublished(Pageable pageable);

    Page<SystemAnnouncement> getAll(Pageable pageable);

    SystemAnnouncement getById(Long id);

    void toggleTop(Long adminId, Long id, boolean top);
}
