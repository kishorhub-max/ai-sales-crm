package com.aisalescrm.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineStatsResponse {

    // Per-stage breakdown
    private List<StageStats> stages;

    // Totals
    private BigDecimal totalPipelineValue;
    private BigDecimal weightedPipelineValue;
    private BigDecimal wonRevenue;
    private long       totalOpportunities;
    private long       openOpportunities;
    private long       wonCount;
    private long       lostCount;

    // Monthly trend [{month:"2024-01", revenue:50000}, ...]
    private List<Map<String, Object>> monthlyRevenue;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StageStats {
        private String     stage;
        private long       count;
        private BigDecimal value;
    }
}