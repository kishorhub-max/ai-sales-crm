package com.aisalescrm.mapper;

import com.aisalescrm.dto.response.InvoiceResponse;
import com.aisalescrm.entity.Invoice;
import com.aisalescrm.enums.InvoiceStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class InvoiceMapper {

    public InvoiceResponse toResponse(Invoice inv) {
        if (inv == null) return null;

        LocalDate today = LocalDate.now();
        boolean overdue = inv.getStatus() != InvoiceStatus.PAID
                && inv.getStatus() != InvoiceStatus.CANCELLED
                && inv.getDueDate() != null
                && inv.getDueDate().isBefore(today);

        long daysUntilDue = inv.getDueDate() != null
                ? ChronoUnit.DAYS.between(today, inv.getDueDate())
                : 0;

        InvoiceResponse.InvoiceResponseBuilder b = InvoiceResponse.builder()
                .id(inv.getId())
                .invoiceNumber(inv.getInvoiceNumber())
                .status(inv.getStatus())
                .issueDate(inv.getIssueDate())
                .dueDate(inv.getDueDate())
                .paidDate(inv.getPaidDate())
                .subtotal(inv.getSubtotal())
                .taxPercent(inv.getTaxPercent())
                .taxAmount(inv.getTaxAmount())
                .discountPercent(inv.getDiscountPercent())
                .discountAmount(inv.getDiscountAmount())
                .total(inv.getTotal())
                .paymentMethod(inv.getPaymentMethod())
                .notes(inv.getNotes())
                .overdue(overdue)
                .daysUntilDue(daysUntilDue)
                .createdAt(inv.getCreatedAt())
                .updatedAt(inv.getUpdatedAt());

        if (inv.getCustomer() != null) {
            b.customerId(inv.getCustomer().getId())
                    .customerName(inv.getCustomer().getFullName())
                    .customerCompany(inv.getCustomer().getCompany())
                    .customerEmail(inv.getCustomer().getEmail());
        }

        if (inv.getOrder() != null) {
            b.orderId(inv.getOrder().getId())
                    .orderNumber(inv.getOrder().getOrderNumber());
        }

        return b.build();
    }
}