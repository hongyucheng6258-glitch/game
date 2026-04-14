package com.dianjing.controller;

import com.dianjing.common.PageResult;
import com.dianjing.common.Result;
import com.dianjing.entity.SystemAnnouncement;
import com.dianjing.service.SystemAnnouncementService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/announcements")
public class SystemAnnouncementController {

    private final SystemAnnouncementService announcementService;

    public SystemAnnouncementController(SystemAnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    /**
     * 公开接口 - 已发布公告列表
     */
    @GetMapping
    public Result<PageResult<SystemAnnouncement>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page - 1, size);
        Page<SystemAnnouncement> pageResult = announcementService.getPublished(pageable);
        return Result.success(new PageResult<>(pageResult.getTotalElements(),
                pageResult.getTotalPages(), page, pageResult.getContent()));
    }

    /**
     * 公开接口 - 公告详情
     */
    @GetMapping("/{id}")
    public Result<SystemAnnouncement> getDetail(@PathVariable Long id) {
        return Result.success(announcementService.getById(id));
    }
}
