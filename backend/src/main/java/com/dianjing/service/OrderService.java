package com.dianjing.service;

import com.dianjing.dto.request.OrderCreateRequest;
import com.dianjing.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface OrderService {

    Order create(Long userId, OrderCreateRequest request);

    Order pay(Long userId, String orderNo, String paymentMethod);

    void cancel(Long userId, String orderNo);

    void startService(Long providerId, String orderNo);

    void completeService(Long providerId, String orderNo);

    void confirm(Long userId, String orderNo);

    Order getByOrderNo(String orderNo);

    Order getById(Long id);

    Page<Order> getUserOrders(Long userId, Integer status, Pageable pageable);

    Page<Order> getProviderOrders(Long providerId, Integer status, Pageable pageable);

    Page<Order> getAllOrders(Integer status, Pageable pageable);

    Map<String, Object> getUserOrderStats(Long userId);

    Map<String, Object> getProviderOrderStats(Long providerId);

    void applyRefund(Long userId, String orderNo);

    void approveRefund(Long adminId, Long orderId);

    void rejectRefund(Long adminId, Long orderId);
}
