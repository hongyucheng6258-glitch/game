
package com.dianjing.controller;

import com.dianjing.common.PageResult;
import com.dianjing.common.Result;
import com.dianjing.dto.request.OrderCreateRequest;
import com.dianjing.dto.response.OrderDetailResponse;
import com.dianjing.entity.Order;
import com.dianjing.entity.User;
import com.dianjing.mapper.OrderMapper;
import com.dianjing.service.OrderService;
import com.dianjing.service.ServiceService;
import com.dianjing.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;
    private final ServiceService serviceService;
    private final OrderMapper orderMapper;

    public OrderController(OrderService orderService, UserService userService, ServiceService serviceService, OrderMapper orderMapper) {
        this.orderService = orderService;
        this.userService = userService;
        this.serviceService = serviceService;
        this.orderMapper = orderMapper;
    }

    private Long getCurrentUserId() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            if ("anonymousUser".equals(username)) {
                throw new com.dianjing.common.BusinessException(401, "用户未登录");
            }
            return userService.getUserByUsername(username).getId();
        } catch (Exception e) {
            throw new com.dianjing.common.BusinessException(401, "用户未登录");
        }
    }

    @PostMapping
    public Result<Order> create(@Valid @RequestBody OrderCreateRequest request) {
        Long userId = getCurrentUserId();
        Order order = orderService.create(userId, request);
        return Result.success(order);
    }

    @GetMapping("/{orderNo}")
    public Result<OrderDetailResponse> getDetail(@PathVariable String orderNo) {
        Order order;
        try {
            // 先尝试作为ID查询
            Long id = Long.parseLong(orderNo);
            order = orderService.getById(id);
        } catch (NumberFormatException e) {
            // 不是数字，作为订单号查询
            order = orderService.getByOrderNo(orderNo);
        }
        return Result.success(convertToResponse(order));
    }

    @GetMapping("/check-ordered/{messageId}")
    public Result<Boolean> checkOrdered(@PathVariable Long messageId) {
        boolean hasOrdered = orderMapper.existsByPriceNegotiationMessageId(messageId);
        return Result.success(hasOrdered);
    }

    private OrderDetailResponse convertToResponse(Order order) {
        OrderDetailResponse response = new OrderDetailResponse();
        response.setId(order.getId());
        response.setOrderNo(order.getOrderNo());
        response.setUserId(order.getUserId());
        response.setProviderId(order.getProviderId());
        response.setServiceId(order.getServiceId());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());
        response.setRequirements(order.getRequirements());
        response.setStartTime(order.getStartTime());
        response.setEndTime(order.getEndTime());
        response.setPaymentTime(order.getPaymentTime());
        response.setPaymentMethod(order.getPaymentMethod());
        response.setCreatedAt(order.getCreatedAt());
        
        // 填充用户信息
        try {
            User user = userService.getUserById(order.getUserId());
            response.setUserName(user.getUsername());
        } catch (Exception e) {
            // ignore
        }
        
        // 填充服务提供者信息
        try {
            User provider = userService.getUserById(order.getProviderId());
            response.setProviderName(provider.getUsername());
        } catch (Exception e) {
            // ignore
        }
        
        // 填充服务信息
        try {
            com.dianjing.entity.Service service = serviceService.getById(order.getServiceId());
            response.setServiceTitle(service.getTitle());
        } catch (Exception e) {
            // ignore
        }
        
        return response;
    }

    @GetMapping("/my")
    public Result<PageResult<OrderDetailResponse>> listMyOrders(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = getCurrentUserId();
        PageRequest pageable = PageRequest.of(page - 1, size);
        Page<Order> orderPage = orderService.getUserOrders(userId, status, pageable);
        List<OrderDetailResponse> responses = orderPage.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return Result.success(new PageResult<>(orderPage.getTotalElements(),
                orderPage.getTotalPages(), page, responses));
    }

    @GetMapping("/received")
    public Result<PageResult<OrderDetailResponse>> listReceivedOrders(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = getCurrentUserId();
        PageRequest pageable = PageRequest.of(page - 1, size);
        Page<Order> orderPage = orderService.getProviderOrders(userId, status, pageable);
        List<OrderDetailResponse> responses = orderPage.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return Result.success(new PageResult<>(orderPage.getTotalElements(),
                orderPage.getTotalPages(), page, responses));
    }

    @GetMapping("/my/stats")
    public Result<Map<String, Object>> getUserOrderStats() {
        Long userId = getCurrentUserId();
        return Result.success(orderService.getUserOrderStats(userId));
    }

    @GetMapping("/provider/stats")
    public Result<Map<String, Object>> getProviderOrderStats() {
        Long userId = getCurrentUserId();
        return Result.success(orderService.getProviderOrderStats(userId));
    }

    @PutMapping("/{orderNo}/pay")
    public Result<Order> payOrderByPut(@PathVariable String orderNo, @RequestParam(defaultValue = "balance") String paymentMethod) {
        return payOrder(orderNo, paymentMethod);
    }

    @PostMapping("/{orderNo}/pay")
    public Result<Order> payOrder(@PathVariable String orderNo, @RequestParam(defaultValue = "balance") String paymentMethod) {
        Long userId = getCurrentUserId();
        Order order = orderService.pay(userId, orderNo, paymentMethod);
        return Result.success(order);
    }

    @PutMapping("/{orderNo}/cancel")
    public Result<Void> cancelOrderByPut(@PathVariable String orderNo) {
        return cancelOrder(orderNo);
    }

    @PostMapping("/{orderNo}/cancel")
    public Result<Void> cancelOrder(@PathVariable String orderNo) {
        Long userId = getCurrentUserId();
        orderService.cancel(userId, orderNo);
        return Result.success();
    }

    @PutMapping("/{orderNo}/start")
    public Result<Void> startServiceByPut(@PathVariable String orderNo) {
        return startService(orderNo);
    }

    @PostMapping("/{orderNo}/start")
    public Result<Void> startService(@PathVariable String orderNo) {
        Long userId = getCurrentUserId();
        orderService.startService(userId, orderNo);
        return Result.success();
    }

    @PutMapping("/{orderNo}/complete")
    public Result<Void> completeServiceByPut(@PathVariable String orderNo) {
        return completeService(orderNo);
    }

    @PostMapping("/{orderNo}/complete")
    public Result<Void> completeService(@PathVariable String orderNo) {
        Long userId = getCurrentUserId();
        orderService.completeService(userId, orderNo);
        return Result.success();
    }

    @PutMapping("/{orderNo}/confirm")
    public Result<Void> confirmOrderByPut(@PathVariable String orderNo) {
        return confirmOrder(orderNo);
    }

    @PostMapping("/{orderNo}/confirm")
    public Result<Void> confirmOrder(@PathVariable String orderNo) {
        Long userId = getCurrentUserId();
        orderService.confirm(userId, orderNo);
        return Result.success();
    }

    @GetMapping
    public Result<com.dianjing.common.PageResult<Order>> listOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = getCurrentUserId();
        org.springframework.data.domain.PageRequest pageable = org.springframework.data.domain.PageRequest.of(page - 1, size);
        org.springframework.data.domain.Page<Order> orderPage = orderService.getUserOrders(userId, null, pageable);
        return Result.success(new com.dianjing.common.PageResult<>(orderPage.getTotalElements(),
                orderPage.getTotalPages(), page, orderPage.getContent()));
    }
}
