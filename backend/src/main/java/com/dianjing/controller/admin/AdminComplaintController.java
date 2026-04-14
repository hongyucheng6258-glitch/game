package com.dianjing.controller.admin;

import com.dianjing.common.PageResult;
import com.dianjing.common.Result;
import com.dianjing.dto.request.ArbitrationRequest;
import com.dianjing.entity.Complaint;
import com.dianjing.service.ComplaintService;
import com.dianjing.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/complaints")
@PreAuthorize("hasRole('ADMIN')")
public class AdminComplaintController {

    private final ComplaintService complaintService;
    private final UserService userService;

    public AdminComplaintController(ComplaintService complaintService, UserService userService) {
        this.complaintService = complaintService;
        this.userService = userService;
    }

    private Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getUserByUsername(username).getId();
    }

    @GetMapping
    public Result<PageResult<Complaint>> list(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page - 1, size);
        Page<Complaint> complaintPage = complaintService.getAllComplaints(status, pageable);
        return Result.success(new PageResult<>(complaintPage.getTotalElements(),
                complaintPage.getTotalPages(), page, complaintPage.getContent()));
    }

    @GetMapping("/{id}")
    public Result<Complaint> getById(@PathVariable Long id) {
        Complaint complaint = complaintService.getById(id);
        return Result.success(complaint);
    }

    @PutMapping("/arbitrate")
    public Result<Complaint> arbitrate(@Valid @RequestBody ArbitrationRequest request) {
        Long arbitratorId = getCurrentUserId();
        Complaint complaint = complaintService.arbitrate(arbitratorId, request);
        return Result.success(complaint);
    }
}
