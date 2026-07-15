package com.aisalescrm.dto.request;

import com.aisalescrm.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private OrderStatus status;
}