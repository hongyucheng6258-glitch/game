package com.dianjing.service.impl;

import com.dianjing.common.BusinessException;
import com.dianjing.common.Constants;
import com.dianjing.dto.request.AnnouncementCreateRequest;
import com.dianjing.entity.SystemAnnouncement;
import com.dianjing.mapper.SystemAnnouncementMapper;
import com.dianjing.service.SystemAnnouncementService;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;

@Service
public class SystemAnnouncementServiceImpl implements SystemAnnouncementService {

    private final SystemAnnouncementMapper systemAnnouncementMapper;

    public SystemAnnouncementServiceImpl(SystemAnnouncementMapper systemAnnouncementMapper) {
        this.systemAnnouncementMapper = systemAnnouncementMapper;
    }

    @Override
    public SystemAnnouncement create(Long adminId, AnnouncementCreateRequest request) {
        SystemAnnouncement announcement = new SystemAnnouncement();
        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        announcement.setIsTop(request.getIsTop() != null ? request.getIsTop() : false);
        announcement.setIsPublished(Constants.ANNOUNCEMENT_UNPUBLISHED);
        announcement.setCreatedBy(adminId);
        return systemAnnouncementMapper.save(announcement);
    }

    @Override
    public void publish(Long adminId, Long id) {
        SystemAnnouncement announcement = getById(id);
        if (announcement.getIsPublished() == Constants.ANNOUNCEMENT_PUBLISHED) {
            throw new BusinessException(400, "公告已发布");
        }
        announcement.setIsPublished(Constants.ANNOUNCEMENT_PUBLISHED);
        announcement.setPublishTime(LocalDateTime.now());
        systemAnnouncementMapper.save(announcement);
    }

    @Override
    public Page<SystemAnnouncement> getPublished(Pageable pageable) {
        return systemAnnouncementMapper.findByIsPublishedOrderByPublishTimeDesc(
                Constants.ANNOUNCEMENT_PUBLISHED, pageable);
    }

    @Override
    public Page<SystemAnnouncement> getAll(Pageable pageable) {
        return systemAnnouncementMapper.findAllByOrderByIdDesc(pageable);
    }

    @Override
    public SystemAnnouncement getById(Long id) {
        return systemAnnouncementMapper.findById(id)
                .orElseThrow(() -> new BusinessException(404, "公告不存在"));
    }

    @Override
    public SystemAnnouncement update(Long adminId, Long id, AnnouncementCreateRequest request) {
        SystemAnnouncement announcement = getById(id);
        if (announcement.getIsPublished() == Constants.ANNOUNCEMENT_PUBLISHED) {
            throw new BusinessException(400, "已发布的公告不能修改");
        }
        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        announcement.setIsTop(request.getIsTop());
        return systemAnnouncementMapper.save(announcement);
    }

    @Override
    public void delete(Long adminId, Long id) {
        SystemAnnouncement announcement = getById(id);
        if (announcement.getIsPublished() == Constants.ANNOUNCEMENT_PUBLISHED) {
            throw new BusinessException(400, "已发布的公告不能删除");
        }
        systemAnnouncementMapper.delete(announcement);
    }

    @Override
    public void toggleTop(Long adminId, Long id, boolean top) {
        SystemAnnouncement announcement = getById(id);
        announcement.setIsTop(top);
        systemAnnouncementMapper.save(announcement);
    }
}
