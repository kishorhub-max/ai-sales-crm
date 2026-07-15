
package com.aisalescrm.ai.service;

import com.aisalescrm.ai.dto.SalesSummaryResponse;
import com.aisalescrm.enums.LeadStatus;
import com.aisalescrm.enums.OpportunityStage;
import com.aisalescrm.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalesSummaryService {

    private final LeadRepository        leadRepository;
    private final CustomerRepository    customerRepository;
    private final OpportunityRepository opportunityRepository;
    private final InvoiceRepository     invoiceRepository;
    private final GeminiService         geminiService;
    private final ObjectMapper          objectMapper;

    private static final String SYSTEM_INSTRUCTION = """
        You are a sales intelligence AI for a B2B CRM platform.
        Generate actionable daily sales briefings.
        Return ONLY valid JSON — no markdown, no backticks.
        Be specific, prioritized, and focused on revenue impact.
        """;

    @Transactional(readOnly = true)
    public SalesSummaryResponse generateDailySummary() {
        String context = buildSummaryContext();

        String prompt = """
            Based on this CRM data, generate a daily sales team briefing.
            Return a JSON object with exactly these fields:
            {
              "overallSummary": "<2-3 sentence executive overview of pipeline health>",
              "topPriorities": ["<priority1>", "<priority2>", "<priority3>"],
              "atRiskDeals": ["<deal description1>", "<deal description2>"],
              "followUpRequired": ["<customer/lead1>", "<customer/lead2>"],
              "recommendations": ["<recommendation1>", "<recommendation2>", "<recommendation3>"],
              "forecastInsight": "<1 sentence revenue forecast insight>"
            }

            CRM Data:
            %s
            """.formatted(context);

        String rawJson = geminiService.sendPrompt(SYSTEM_INSTRUCTION, prompt);
        return parseSummaryResponse(rawJson);
    }

    // ── Context Builder ───────────────────────────────────────────────────────

    private String buildSummaryContext() {
        long totalLeads       = leadRepository.count();
        long newLeads         = leadRepository.countByStatus(LeadStatus.NEW);
        long convertedLeads   = leadRepository.countByStatus(LeadStatus.CONVERTED);

        long activeCustomers  = customerRepository.countByActiveTrue();

        long openOpps         = opportunityRepository.count()
                - opportunityRepository.countByStage(OpportunityStage.WON)
                - opportunityRepository.countByStage(OpportunityStage.LOST);
        long wonOpps          = opportunityRepository.countByStage(OpportunityStage.WON);
        long lostOpps         = opportunityRepository.countByStage(OpportunityStage.LOST);
        BigDecimal wonRev     = opportunityRepository.sumWonRevenue();
        BigDecimal weighted   = opportunityRepository.sumWeightedPipeline();

        List<?> atRisk        = opportunityRepository.findAtRiskOpportunities(LocalDate.now());
        List<?> closingSoon   = opportunityRepository.findClosingSoon(
                LocalDate.now(), LocalDate.now().plusDays(14));

        long overdueInvoices  = invoiceRepository.findOverdueInvoices(LocalDate.now()).size();
        BigDecimal outstanding = invoiceRepository.sumOutstandingRevenue();

        // At-risk deal names
        String atRiskNames = opportunityRepository
                .findAtRiskOpportunities(LocalDate.now())
                .stream()
                .map(o -> o.getDealName() + " ($" + o.getValue() + ")")
                .limit(5)
                .reduce((a, b) -> a + "; " + b)
                .orElse("None");

        // Closing soon names
        String closingNames = opportunityRepository
                .findClosingSoon(LocalDate.now(), LocalDate.now().plusDays(14))
                .stream()
                .map(o -> o.getDealName() + " ($" + o.getValue() + ", " + o.getStage() + ")")
                .limit(5)
                .reduce((a, b) -> a + "; " + b)
                .orElse("None");

        return """
            Date: %s

            LEADS
            Total: %d | New: %d | Converted: %d

            CUSTOMERS
            Active: %d

            OPPORTUNITIES
            Open: %d | Won: %d | Lost: %d
            Won Revenue: $%s
            Weighted Pipeline: $%s
            At-Risk Deals (%d): %s
            Closing This Week (%d): %s

            INVOICES
            Overdue: %d
            Outstanding Revenue: $%s
            """.formatted(
                LocalDate.now(),
                totalLeads, newLeads, convertedLeads,
                activeCustomers,
                openOpps, wonOpps, lostOpps,
                wonRev    != null ? wonRev.toPlainString()    : "0",
                weighted  != null ? weighted.toPlainString()  : "0",
                atRisk.size(), atRiskNames,
                closingSoon.size(), closingNames,
                overdueInvoices,
                outstanding != null ? outstanding.toPlainString() : "0"
        );
    }

    // ── JSON Parser ───────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private SalesSummaryResponse parseSummaryResponse(String rawJson) {
        try {
            String json = rawJson.replaceAll("```json", "").replaceAll("```", "").trim();
            var map = objectMapper.readValue(json, java.util.Map.class);

            return SalesSummaryResponse.builder()
                    .overallSummary(str(map.get("overallSummary")))
                    .topPriorities(toList(map.get("topPriorities")))
                    .atRiskDeals(toList(map.get("atRiskDeals")))
                    .followUpRequired(toList(map.get("followUpRequired")))
                    .recommendations(toList(map.get("recommendations")))
                    .forecastInsight(str(map.get("forecastInsight")))
                    .build();

        } catch (Exception e) {
            log.error("Failed to parse summary JSON: {}", e.getMessage());
            return SalesSummaryResponse.builder()
                    .overallSummary("AI summary temporarily unavailable.")
                    .topPriorities(List.of("Review at-risk deals", "Follow up on overdue invoices"))
                    .atRiskDeals(List.of())
                    .followUpRequired(List.of())
                    .recommendations(List.of("Check pipeline health manually"))
                    .forecastInsight("Unable to generate forecast at this time.")
                    .build();
        }
    }

    private String str(Object val) { return val != null ? val.toString() : ""; }

    @SuppressWarnings("unchecked")
    private List<String> toList(Object val) {
        if (val instanceof List<?> list) return list.stream().map(Object::toString).toList();
        return List.of();
    }
}