package com.aisalescrm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class InvoicePaymentRequest {

    @NotNull(message = "Payment date is required")
    private LocalDate paidDate;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;
}