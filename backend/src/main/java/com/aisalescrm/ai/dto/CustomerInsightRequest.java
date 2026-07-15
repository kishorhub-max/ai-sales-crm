package com.aisalescrm.ai.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CustomerInsightRequest {

    @NotNull
    private Long customerId;
}