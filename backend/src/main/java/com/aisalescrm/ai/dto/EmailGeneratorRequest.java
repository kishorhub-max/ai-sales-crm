package com.aisalescrm.ai.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailGeneratorRequest {

    @NotNull
    private Long customerId;

    private Long opportunityId;

    @NotNull
    private String emailType;

    private String additionalContext;
}