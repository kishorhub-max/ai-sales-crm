package com.aisalescrm.mapper;

import com.aisalescrm.dto.response.OrderItemResponse;
import com.aisalescrm.dto.response.OrderResponse;
import com.aisalescrm.entity.Order;
import com.aisalescrm.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order o) {
        if (o == null) return null;

        OrderResponse.OrderResponseBuilder b = OrderResponse.builder()
                .id(o.getId())
                .orderNumber(o.getOrderNumber())
                .status(o.getStatus())
                .subtotal(o.getSubtotal())
                .discountPercent(o.getDiscountPercent())
                .discountAmount(o.getDiscountAmount())
                .taxPercent(o.getTaxPercent())
                .taxAmount(o.getTaxAmount())
                .total(o.getTotal())
                .shippingAddress(o.getShippingAddress())
                .shippingCity(o.getShippingCity())
                .shippingCountry(o.getShippingCountry())
                .notes(o.getNotes())
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt());

        if (o.getCustomer() != null) {
            b.customerId(o.getCustomer().getId())
                    .customerName(o.getCustomer().getFullName())
                    .customerCompany(o.getCustomer().getCompany());
        }

        if (o.getAssignedTo() != null) {
            b.assignedToId(o.getAssignedTo().getId())
                    .assignedToName(o.getAssignedTo().getFullName());
        }

        if (o.getItems() != null) {
            b.items(o.getItems().stream()
                    .map(this::toItemResponse)
                    .collect(Collectors.toList()));
        }

        return b.build();
    }

    public OrderItemResponse toItemResponse(OrderItem item) {
        BigDecimal discountAmount = BigDecimal.ZERO;

        if (item.getDiscountPercent() != null
                && item.getDiscountPercent().compareTo(BigDecimal.ZERO) > 0) {
            discountAmount = item.getUnitPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()))
                    .multiply(item.getDiscountPercent())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                .productName(item.getProduct() != null ? item.getProduct().getName() : null)
                .productSku(item.getProduct() != null ? item.getProduct().getSku() : null)
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .discountPercent(item.getDiscountPercent())
                .discountAmount(discountAmount)
                .totalPrice(item.getTotalPrice())
                .build();
    }
}