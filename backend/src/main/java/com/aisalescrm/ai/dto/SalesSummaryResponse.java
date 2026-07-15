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
public class SalesSummaryResponse {

    private String overallSummary;

    private List<String> topPriorities;

    private List<String> atRiskDeals;

    private List<String> followUpRequired;

    private List<String> recommendations;

    private String forecastInsight;
}