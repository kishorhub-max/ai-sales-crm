package com.aisalescrm.dto.request;

import com.aisalescrm.enums.OpportunityStage;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OpportunityStageUpdateRequest {

    @NotNull(message = "Stage is required")
    private OpportunityStage stage;

    @Min(0) @Max(100)
    private Integer probability;

    private String lostReason;
}