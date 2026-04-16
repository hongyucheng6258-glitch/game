package com.dianjing.controller.admin;

import com.dianjing.common.PageResult;
import com.dianjing.common.Result;
import com.dianjing.entity.Order;
import com.dianjing.service.OrderService;
import com.dianjing.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final OrderService orderService;
    private final UserService userService;

    public AdminOrderController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    private Long getCurrentAdminId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getUserByUsername(username).getId();
    }

    /**
     * 订单列表（分页）
     */
    @GetMapping
    public Result<PageResult<Order>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        PageRequest pageable = PageRequest.of(page - 1, size);
        Page<Order> orderPage = orderService.getAllOrders(status, pageable);
        return Result.success(new PageResult<>(orderPage.getTotalElements(),
                orderPage.getTotalPages(), page, orderPage.getContent()));
    }

    /**
     * 订单详情
     */
    @GetMapping("/{orderNo}")
    public Result<Order> getDetail(@PathVariable String orderNo) {
        return Result.success(orderService.getByOrderNo(orderNo));
    }

    /**
     * 同意退款
     */
    @PostMapping("/{id}/refund/approve")
    public Result<Void> approveRefund(@PathVariable Long id) {
        Long adminId = getCurrentAdminId();
        orderService.approveRefund(adminId, id);
        return Result.success();
    }

    /**
     * 拒绝退款
     */
    @PostMapping("/{id}/refund/reject")
    public Result<Void> rejectRefund(@PathVariable Long id) {
        Long adminId = getCurrentAdminId();
        orderService.rejectRefund(adminId, id);
        return Result.success();
    }
}
