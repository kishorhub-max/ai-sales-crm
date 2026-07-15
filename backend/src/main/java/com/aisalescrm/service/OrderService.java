package com.aisalescrm.service;

import com.aisalescrm.dto.request.OrderRequest;
import com.aisalescrm.dto.request.OrderStatusUpdateRequest;
import com.aisalescrm.dto.response.OrderResponse;
import com.aisalescrm.dto.response.PageResponse;
import com.aisalescrm.enums.OrderStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface OrderService {

    OrderResponse createOrder(OrderRequest request);

    OrderResponse getOrderById(Long id);

    OrderResponse getOrderByNumber(String orderNumber);

    PageResponse<OrderResponse> getAllOrders(
            OrderStatus status, Long customerId, String search, Pageable pageable);

    OrderResponse updateOrderStatus(Long id, OrderStatusUpdateRequest request);

    void cancelOrder(Long id);

    List<OrderResponse> getOrdersByCustomer(Long customerId);

    Map<String, Object> getOrderStats();
}