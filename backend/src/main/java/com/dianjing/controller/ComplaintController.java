package com.dianjing.controller;

import com.dianjing.common.BusinessException;
import com.dianjing.common.Constants;
import com.dianjing.common.PageResult;
import com.dianjing.common.Result;
import com.dianjing.dto.request.ComplaintCreateRequest;
import com.dianjing.entity.Complaint;
import com.dianjing.entity.User;
import com.dianjing.service.ComplaintService;
import com.dianjing.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/complaints")
public class ComplaintController {

    private final ComplaintService complaintService;
    private final UserService userService;

    public ComplaintController(ComplaintService complaintService, UserService userService) {
        this.complaintService = complaintService;
        this.userService = userService;
    }

    private Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getUserByUsername(username).getId();
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getUserByUsername(username);
    }

    @PostMapping
    public Result<Complaint> create(@Valid @RequestBody ComplaintCreateRequest request) {
        Long userId = getCurrentUserId();
        Complaint complaint = complaintService.create(userId, request);
        return Result.success(complaint);
    }

    @GetMapping("/{id}")
    public Result<Complaint> getById(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        Complaint complaint = complaintService.getById(id);
        if (complaint == null) {
            throw new BusinessException(404, "投诉不存在");
        }
        // 只有投诉方、被投诉方或管理员可以查看
        if (!userId.equals(complaint.getComplainantId())
            && !userId.equals(complaint.getRespondentId())
            && getCurrentUser().getRole() != Constants.ROLE_ADMIN) {
            throw new BusinessException(403, "无权查看此投诉");
        }
        return Result.success(complaint);
    }

    @GetMapping("/my")
    public Result<PageResult<Complaint>> listMyComplaints(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = getCurrentUserId();
        PageRequest pageable = PageRequest.of(page - 1, size);
        Page<Complaint> complaintPage = complaintService.getUserComplaints(userId, pageable);
        return Result.success(new PageResult<>(complaintPage.getTotalElements(),
                complaintPage.getTotalPages(), page, complaintPage.getContent()));
    }

    @GetMapping
    public Result<PageResult<Complaint>> listComplaints(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = getCurrentUserId();
        PageRequest pageable = PageRequest.of(page - 1, size);
        Page<Complaint> complaintPage = complaintService.getUserComplaints(userId, pageable);
        return Result.success(new PageResult<>(complaintPage.getTotalElements(),
                complaintPage.getTotalPages(), page, complaintPage.getContent()));
    }
}
