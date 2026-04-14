package com.dianjing.controller.admin;

import com.dianjing.common.Constants;
import com.dianjing.common.PageResult;
import com.dianjing.common.Result;
import com.dianjing.entity.Service;
import com.dianjing.service.ServiceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/services")
@PreAuthorize("hasRole('ADMIN')")
public class AdminServiceController {

    private final ServiceService serviceService;

    public AdminServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    /**
     * 服务列表（分页）- 管理员可以查看所有状态的服务
     */
    @GetMapping
    public Result<PageResult<Service>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String gameType,
            @RequestParam(required = false) Integer serviceType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) java.math.BigDecimal minPrice,
            @RequestParam(required = false) java.math.BigDecimal maxPrice,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Order.desc("id")));
        
        Page<Service> servicePage = serviceService.search(keyword, gameType, serviceType, status, minPrice, maxPrice, sort, null, true, pageable);
        
        return Result.success(new PageResult<>(servicePage.getTotalElements(),
                servicePage.getTotalPages(), page, servicePage.getContent()));
    }

    /**
     * 服务详情
     */
    @GetMapping("/{id}")
    public Result<Service> getDetail(@PathVariable Long id) {
        return Result.success(serviceService.getById(id));
    }

    /**
     * 审核通过服务
     */
    @PutMapping("/{id}/approve")
    public Result<Void> approveService(@PathVariable Long id) {
        serviceService.approveService(id);
        return Result.success();
    }

    /**
     * 审核拒绝服务
     */
    @PutMapping("/{id}/reject")
    public Result<Void> rejectService(@PathVariable Long id) {
        serviceService.rejectService(id);
        return Result.success();
    }

    /**
     * 上下架服务
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id) {
        Service service = serviceService.getById(id);
        serviceService.toggleStatus(service.getProviderId(), id);
        return Result.success();
    }

    /**
     * 删除服务
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Service service = serviceService.getById(id);
        serviceService.delete(service.getProviderId(), id);
        return Result.success();
    }
}
