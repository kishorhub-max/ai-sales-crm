package com.aisalescrm.dto.request;



import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

    @Data
    public class OrderRequest {

        @NotNull(message = "Customer ID is required")
        private Long customerId;

        private Long assignedToId;

        @NotEmpty(message = "Order must have at least one item")
        @Valid
        private List<OrderItemRequest> items;

        @DecimalMin(value = "0.0", message = "Discount cannot be negative")
        @DecimalMax(value = "100.0", message = "Discount cannot exceed 100%")
        private BigDecimal discountPercent;

        @DecimalMin(value = "0.0", message = "Tax cannot be negative")
        private BigDecimal taxPercent;

        private String notes;

        // Shipping info
        private String shippingAddress;
        private String shippingCity;
        private String shippingCountry;
    }