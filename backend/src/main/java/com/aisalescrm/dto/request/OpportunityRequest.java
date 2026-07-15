package com.aisalescrm.dto.request;

import com.aisalescrm.enums.OpportunityStage;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class OpportunityRequest {

    @NotBlank(message = "Deal name is required")
    @Size(max = 150, message = "Deal name must not exceed 150 characters")
    private String dealName;

    @NotNull(message = "Value is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Value must be greater than 0")
    private BigDecimal value;

    private OpportunityStage stage;

    @Min(value = 0, message = "Probability must be 0-100")
    @Max(value = 100, message = "Probability must be 0-100")
    private Integer probability;

    private LocalDate expectedCloseDate;

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    private Long assignedToId;
    private Long productId;

    @Size(max = 3000)
    private String description;

    @Size(max = 1000)
    private String lostReason;
}