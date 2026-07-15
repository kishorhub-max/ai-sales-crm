package com.aisalescrm.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerInsightResponse {

    private Long customerId;
    private String customerName;

    private Integer healthScore;
    private Integer churnRisk;
    private Integer purchaseProbability;

    private String healthScoreLabel;
    private String churnRiskLabel;
    private String purchaseProbabilityLabel;

    private String insights;
    private List<String> upsellSuggestions;
    private List<String> recommendedActions;
    private String summary;
}