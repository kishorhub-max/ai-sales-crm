package com.aisalescrm.service.impl;

import com.aisalescrm.dto.request.OrderItemRequest;
import com.aisalescrm.dto.request.OrderRequest;
import com.aisalescrm.dto.request.OrderStatusUpdateRequest;
import com.aisalescrm.dto.response.OrderResponse;
import com.aisalescrm.dto.response.PageResponse;
import com.aisalescrm.entity.*;
import com.aisalescrm.enums.OrderStatus;
import com.aisalescrm.exception.BusinessException;
import com.aisalescrm.exception.ResourceNotFoundException;
import com.aisalescrm.mapper.OrderMapper;
import com.aisalescrm.repository.*;
import com.aisalescrm.service.CustomerService;
import com.aisalescrm.service.OrderService;
import com.aisalescrm.util.OrderNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository      orderRepository;
    private final CustomerRepository   customerRepository;
    private final UserRepository       userRepository;
    private final ProductRepository    productRepository;
    private final OrderMapper          orderMapper;
    private final OrderNumberGenerator numberGenerator;
    private final CustomerService      customerService;

    // ── Create ───────────────────────────────────────────────────────────────

    @Override
    public OrderResponse createOrder(OrderRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", request.getCustomerId()));

        Order order = Order.builder()
                .orderNumber(numberGenerator.generateOrderNumber())
                .customer(customer)
                .status(OrderStatus.PENDING)
                .discountPercent(nullSafe(request.getDiscountPercent()))
                .taxPercent(nullSafe(request.getTaxPercent()))
                .notes(request.getNotes())
                .shippingAddress(request.getShippingAddress())
                .shippingCity(request.getShippingCity())
                .shippingCountry(request.getShippingCountry())
                .items(new ArrayList<>())
                .build();

        if (request.getAssignedToId() != null) {
            order.setAssignedTo(userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", request.getAssignedToId())));
        }

        // ── Build line items & compute subtotal ──────────────────────────────
        BigDecimal subtotal = BigDecimal.ZERO;

        for (OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", itemReq.getProductId()));

            BigDecimal unitPrice = itemReq.getUnitPrice() != null
                    ? itemReq.getUnitPrice()
                    : product.getPrice();

            BigDecimal discountPct = nullSafe(itemReq.getDiscountPercent());
            BigDecimal lineGross   = unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            BigDecimal lineDisc    = lineGross.multiply(discountPct)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal lineTotal   = lineGross.subtract(lineDisc).setScale(2, RoundingMode.HALF_UP);

            OrderItem item = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(unitPrice)
                    .discountPercent(discountPct)
                    .totalPrice(lineTotal)
                    .build();

            order.getItems().add(item);
            subtotal = subtotal.add(lineTotal);
        }

        // ── Apply order-level discount & tax ─────────────────────────────────
        BigDecimal discountPct    = nullSafe(request.getDiscountPercent());
        BigDecimal discountAmount = subtotal.multiply(discountPct)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal afterDiscount  = subtotal.subtract(discountAmount);

        BigDecimal taxPct    = nullSafe(request.getTaxPercent());
        BigDecimal taxAmount = afterDiscount.multiply(taxPct)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal total     = afterDiscount.add(taxAmount).setScale(2, RoundingMode.HALF_UP);

        order.setSubtotal(subtotal);
        order.setDiscountAmount(discountAmount);
        order.setTaxAmount(taxAmount);
        order.setTotal(total);

        Order saved = orderRepository.save(order);

        // Update customer purchase summary
        customerService.updatePurchaseSummary(customer.getId(), total.doubleValue());

        log.info("Order created: {} total={}", saved.getOrderNumber(), saved.getTotal());
        return orderMapper.toResponse(saved);
    }

    // ── Read ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        return orderMapper.toResponse(findOrder(id));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByNumber(String orderNumber) {
        return orderMapper.toResponse(
                orderRepository.findByOrderNumber(orderNumber)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Order not found with number: " + orderNumber)));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getAllOrders(OrderStatus status,
                                                    Long customerId,
                                                    String search,
                                                    Pageable pageable) {
//        String cleanSearch = (search != null && search.isBlank()) ? null : search;
        Page<Order> page = orderRepository.findAllWithFilters(
                status, customerId, pageable);
        return PageResponse.of(page.map(orderMapper::toResponse));
    }

    // ── Status Update ─────────────────────────────────────────────────────────

    @Override
    public OrderResponse updateOrderStatus(Long id, OrderStatusUpdateRequest request) {
        Order order = findOrder(id);

        // Guard: cannot un-cancel
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException("Cannot change status of a cancelled order");
        }

        order.setStatus(request.getStatus());
        Order saved = orderRepository.save(order);
        log.info("Order {} status → {}", saved.getOrderNumber(), saved.getStatus());
        return orderMapper.toResponse(saved);
    }

    // ── Cancel ────────────────────────────────────────────────────────────────

    @Override
    public void cancelOrder(Long id) {
        Order order = findOrder(id);
        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new BusinessException("Cannot cancel a delivered order. Create a refund instead.");
        }
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        log.info("Order {} cancelled", order.getOrderNumber());
    }

    // ── By Customer ───────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream().map(orderMapper::toResponse).collect(Collectors.toList());
    }

    // ── Stats ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getOrderStats() {
        Map<String, Object> stats = new HashMap<>();

        BigDecimal totalRevenue = orderRepository.sumTotalRevenue();
        stats.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        stats.put("totalOrders", orderRepository.count());

        // Per-status counts
        Map<String, Map<String, Object>> byStatus = new HashMap<>();
        for (Object[] row : orderRepository.getOrderStatsByStatus()) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("count",   row[1]);
            entry.put("revenue", row[2] != null ? row[2] : BigDecimal.ZERO);
            byStatus.put(row[0].toString(), entry);
        }
        stats.put("byStatus", byStatus);

        // Monthly trend
        List<Map<String, Object>> monthly = new ArrayList<>();
        for (Object[] row : orderRepository.getMonthlyRevenue()) {
            Map<String, Object> point = new HashMap<>();
            point.put("month",      row[0].toString());
            point.put("orderCount", row[1]);
            point.put("revenue",    row[2] != null ? row[2] : BigDecimal.ZERO);
            monthly.add(point);
        }
        stats.put("monthlyRevenue", monthly);

        return stats;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Order findOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }

    private BigDecimal nullSafe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}