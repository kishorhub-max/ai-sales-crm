package com.aisalescrm.dto.response;

import com.aisalescrm.enums.OrderStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Long        id;
    private String      orderNumber;
    private OrderStatus status;

    // Customer — flattened
    private Long   customerId;
    private String customerName;
    private String customerCompany;

    // Assigned rep
    private Long   assignedToId;
    private String assignedToName;

    // Line items
    private List<OrderItemResponse> items;

    // Financials
    private BigDecimal subtotal;
    private BigDecimal discountPercent;
    private BigDecimal discountAmount;
    private BigDecimal taxPercent;
    private BigDecimal taxAmount;
    private BigDecimal total;

    // Shipping
    private String shippingAddress;
    private String shippingCity;
    private String shippingCountry;

    private String notes;

    // Invoice link (if generated)
    private Long   invoiceId;
    private String invoiceNumber;

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}