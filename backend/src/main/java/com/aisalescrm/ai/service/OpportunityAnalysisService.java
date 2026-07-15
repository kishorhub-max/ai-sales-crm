package com.aisalescrm.ai.service;

import com.aisalescrm.ai.dto.OpportunityAnalysisResponse;
import com.aisalescrm.entity.Opportunity;
import com.aisalescrm.exception.ResourceNotFoundException;
import com.aisalescrm.repository.OpportunityRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpportunityAnalysisService {

    private final OpportunityRepository opportunityRepository;
    private final GeminiService         geminiService;
    private final ObjectMapper          objectMapper;

    private static final String SYSTEM_INSTRUCTION = """
        You are an expert B2B sales strategist AI.
        Analyze sales opportunity data and return ONLY valid JSON — no markdown, no backticks.
        Be specific and actionable in your recommendations.
        """;

    @Transactional(readOnly = true)
    public OpportunityAnalysisResponse analyzeOpportunity(Long opportunityId) {
        Opportunity opp = opportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity", opportunityId));

        String context = buildOpportunityContext(opp);

        String prompt = """
            Analyze this B2B sales opportunity and return a JSON object with exactly these fields:
            {
              "winProbability": <integer 0-100>,
              "riskLevel": "<LOW|MEDIUM|HIGH|CRITICAL>",
              "risks": ["<risk1>", "<risk2>", "<risk3>"],
              "strengths": ["<strength1>", "<strength2>", "<strength3>"],
              "nextRecommendedAction": "<single most important action to take now>",
              "suggestedActions": ["<action1>", "<action2>", "<action3>"],
              "competitiveAnalysis": "<brief competitive positioning insight>",
              "summary": "<1 sentence executive summary>"
            }

            Opportunity Data:
            %s
            """.formatted(context);

        String rawJson = geminiService.sendPrompt(SYSTEM_INSTRUCTION, prompt);
        return parseAnalysisResponse(opp, rawJson);
    }

    // ── Context Builder ───────────────────────────────────────────────────────

    private String buildOpportunityContext(Opportunity opp) {
        long daysInStage = opp.getCreatedAt() != null
                ? ChronoUnit.DAYS.between(opp.getCreatedAt().toLocalDate(), LocalDate.now())
                : 0;

        long daysToClose = opp.getExpectedCloseDate() != null
                ? ChronoUnit.DAYS.between(LocalDate.now(), opp.getExpectedCloseDate())
                : -1;

        String customerInfo = opp.getCustomer() != null
                ? "%s (%s) - Industry: %s, Total Spend: $%.2f".formatted(
                opp.getCustomer().getFullName(),
                opp.getCustomer().getCompany() != null ? opp.getCustomer().getCompany() : "N/A",
                opp.getCustomer().getIndustry() != null ? opp.getCustomer().getIndustry() : "N/A",
                opp.getCustomer().getTotalPurchaseValue() != null
                ? opp.getCustomer().getTotalPurchaseValue() : 0.0)
                : "N/A";

        return """
            Deal Name:           %s
            Value:               $%s
            Stage:               %s
            Current Probability: %d%%
            Days in Pipeline:    %d
            Days Until Close:    %s
            Customer:            %s
            Product:             %s
            Description:         %s
            Lost Reason:         %s
            """.formatted(
                opp.getDealName(),
                opp.getValue(),
                opp.getStage(),
                opp.getProbability(),
                daysInStage,
                daysToClose >= 0 ? daysToClose + " days" : "Overdue / Not set",
                customerInfo,
                opp.getProduct() != null ? opp.getProduct().getName() : "N/A",
                opp.getDescription() != null ? opp.getDescription() : "None",
                opp.getLostReason() != null ? opp.getLostReason() : "N/A"
        );
    }

    // ── JSON Parser ───────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private OpportunityAnalysisResponse parseAnalysisResponse(Opportunity opp, String rawJson) {
        try {
            String json = rawJson.replaceAll("```json", "").replaceAll("```", "").trim();
            var map = objectMapper.readValue(json, java.util.Map.class);

            return OpportunityAnalysisResponse.builder()
                    .opportunityId(opp.getId())
                    .dealName(opp.getDealName())
                    .winProbability(toInt(map.get("winProbability"), opp.getProbability()))
                    .riskLevel(str(map.get("riskLevel"), "MEDIUM"))
                    .risks(toStringList(map.get("risks")))
                    .strengths(toStringList(map.get("strengths")))
                    .nextRecommendedAction(str(map.get("nextRecommendedAction"), "Review deal status"))
                    .suggestedActions(toStringList(map.get("suggestedActions")))
                    .competitiveAnalysis(str(map.get("competitiveAnalysis"), ""))
                    .summary(str(map.get("summary"), ""))
                    .build();

        } catch (Exception e) {
            log.error("Failed to parse opportunity analysis JSON: {}", e.getMessage());
            return fallbackAnalysis(opp);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private int toInt(Object val, int def) {
        if (val == null) return def;
        try { return ((Number) val).intValue(); }
        catch (Exception e) { return def; }
    }

    private String str(Object val, String def) {
        return val != null ? val.toString() : def;
    }

    @SuppressWarnings("unchecked")
    private List<String> toStringList(Object val) {
        if (val instanceof List<?> list) return list.stream().map(Object::toString).toList();
        return List.of();
    }

    private OpportunityAnalysisResponse fallbackAnalysis(Opportunity opp) {
        return OpportunityAnalysisResponse.builder()
                .opportunityId(opp.getId())
                .dealName(opp.getDealName())
                .winProbability(opp.getProbability())
                .riskLevel("MEDIUM")
                .risks(List.of("Unable to assess risks at this time"))
                .strengths(List.of("Deal is in active pipeline"))
                .nextRecommendedAction("Review opportunity details and schedule customer call")
                .suggestedActions(List.of("Follow up with customer", "Update deal notes"))
                .summary("AI analysis temporarily unavailable.")
                .build();
    }
}