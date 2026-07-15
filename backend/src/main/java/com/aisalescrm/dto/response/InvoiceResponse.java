package com.aisalescrm.dto.response;

import com.aisalescrm.enums.InvoiceStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse {

    private Long          id;
    private String        invoiceNumber;
    private InvoiceStatus status;

    // Customer
    private Long   customerId;
    private String customerName;
    private String customerCompany;
    private String customerEmail;

    // Linked order
    private Long   orderId;
    private String orderNumber;

    // Dates
    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate paidDate;

    // Financials
    private BigDecimal subtotal;
    private BigDecimal taxPercent;
    private BigDecimal taxAmount;
    private BigDecimal discountPercent;
    private BigDecimal discountAmount;
    private BigDecimal total;

    private String paymentMethod;
    private String notes;

    // Flags
    private boolean overdue;
    private long    daysUntilDue;

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}