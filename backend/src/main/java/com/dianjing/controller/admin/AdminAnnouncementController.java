package com.dianjing.controller.admin;

import com.dianjing.common.PageResult;
import com.dianjing.common.Result;
import com.dianjing.dto.request.AnnouncementCreateRequest;
import com.dianjing.entity.SystemAnnouncement;
import com.dianjing.service.SystemAnnouncementService;
import com.dianjing.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/announcements")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAnnouncementController {

    private final SystemAnnouncementService announcementService;
    private final UserService userService;

    public AdminAnnouncementController(SystemAnnouncementService announcementService, UserService userService) {
        this.announcementService = announcementService;
        this.userService = userService;
    }

    private Long getCurrentAdminId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getUserByUsername(username).getId();
    }

    /**
     * 公告列表（全部，含未发布）
     */
    @GetMapping
    public Result<PageResult<SystemAnnouncement>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page - 1, size);
        Page<SystemAnnouncement> pageResult = announcementService.getAll(pageable);
        return Result.success(new PageResult<>(pageResult.getTotalElements(),
                pageResult.getTotalPages(), page, pageResult.getContent()));
    }

    /**
     * 创建公告
     */
    @PostMapping
    public Result<SystemAnnouncement> create(@Valid @RequestBody AnnouncementCreateRequest request) {
        Long adminId = getCurrentAdminId();
        SystemAnnouncement announcement = announcementService.create(adminId, request);
        return Result.success(announcement);
    }

    /**
     * 发布公告
     */
    @PostMapping("/{id}/publish")
    public Result<Void> publish(@PathVariable Long id) {
        Long adminId = getCurrentAdminId();
        announcementService.publish(adminId, id);
        return Result.success();
    }

    /**
     * 公告详情
     */
    @GetMapping("/{id}")
    public Result<SystemAnnouncement> getDetail(@PathVariable Long id) {
        return Result.success(announcementService.getById(id));
    }

    /**
     * 更新公告
     */
    @PutMapping("/{id}")
    public Result<SystemAnnouncement> update(@PathVariable Long id, @Valid @RequestBody AnnouncementCreateRequest request) {
        Long adminId = getCurrentAdminId();
        SystemAnnouncement announcement = announcementService.update(adminId, id, request);
        return Result.success(announcement);
    }

    /**
     * 删除公告
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long adminId = getCurrentAdminId();
        announcementService.delete(adminId, id);
        return Result.success();
    }

    /**
     * 置顶/取消置顶公告
     */
    @PostMapping("/{id}/top")
    public Result<Void> toggleTop(@PathVariable Long id, @RequestParam boolean top) {
        Long adminId = getCurrentAdminId();
        announcementService.toggleTop(adminId, id, top);
        return Result.success();
    }
}
