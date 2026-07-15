package com.aisalescrm.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class InvoiceRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    // If linked to an order, values are copied from it
    private Long orderId;

    @NotNull(message = "Issue date is required")
    private LocalDate issueDate;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    // Manual financials (used when not linked to an order)
    private BigDecimal subtotal;

    @DecimalMin(value = "0.0", message = "Tax percent cannot be negative")
    @DecimalMax(value = "100.0", message = "Tax percent cannot exceed 100")
    private BigDecimal taxPercent;

    @DecimalMin(value = "0.0", message = "Discount percent cannot be negative")
    @DecimalMax(value = "100.0", message = "Discount cannot exceed 100")
    private BigDecimal discountPercent;

    @Size(max = 2000)
    private String notes;
}