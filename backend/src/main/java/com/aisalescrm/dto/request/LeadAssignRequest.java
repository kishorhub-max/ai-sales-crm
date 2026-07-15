package com.aisalescrm.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LeadAssignRequest {

    @NotNull(message = "Assigned user ID is required")
    private Long assignedToId;
}