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
public class OpportunityAnalysisResponse {

    private Long opportunityId;

    private String dealName;

    private Integer winProbability;

    private String riskLevel;

    private List<String> risks;

    private List<String> strengths;

    private String nextRecommendedAction;

    private List<String> suggestedActions;

    private String competitiveAnalysis;

    private String summary;
}