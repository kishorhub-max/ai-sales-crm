package com.aisalescrm.ai.service;

import com.aisalescrm.ai.dto.CustomerInsightResponse;
import com.aisalescrm.entity.Customer;
import com.aisalescrm.entity.Order;
import com.aisalescrm.entity.Opportunity;
import com.aisalescrm.exception.ResourceNotFoundException;
import com.aisalescrm.repository.CustomerRepository;
import com.aisalescrm.repository.InvoiceRepository;
import com.aisalescrm.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerInsightService {

    private final CustomerRepository customerRepository;
    private final OrderRepository    orderRepository;
    private final GeminiService      geminiService;
    private final ObjectMapper       objectMapper;

    private static final String SYSTEM_INSTRUCTION = """
        You are an expert CRM AI analyst for a B2B sales team.
        Analyze customer data and return ONLY valid JSON — no markdown, no backticks, no explanation.
        Always return the exact JSON schema requested.
        """;

    @Transactional(readOnly = true)
    public CustomerInsightResponse analyzeCustomer(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", customerId));

        List<Order> orders = orderRepository
                .findByCustomerIdOrderByCreatedAtDesc(customerId);

        // Build CRM context for the AI
        String context = buildCustomerContext(customer, orders);

        String prompt = """
            Analyze this CRM customer data and return a JSON object with exactly these fields:
            {
              "healthScore": <integer 0-100>,
              "churnRisk": <integer 0-100>,
              "purchaseProbability": <integer 0-100>,
              "insights": "<string: 2-3 sentence analysis>",
              "upsellSuggestions": ["<suggestion1>", "<suggestion2>", "<suggestion3>"],
              "recommendedActions": ["<action1>", "<action2>", "<action3>"],
              "summary": "<string: 1 sentence executive summary>"
            }

            Customer Data:
            %s
            """.formatted(context);

        String rawJson = geminiService.sendPrompt(SYSTEM_INSTRUCTION, prompt);
        return parseInsightResponse(customer, rawJson);
    }

    // ── Context Builder ───────────────────────────────────────────────────────

    private String buildCustomerContext(Customer customer, List<Order> orders) {
        int totalOrders    = orders.size();
        double totalSpend  = customer.getTotalPurchaseValue() != null ? customer.getTotalPurchaseValue() : 0.0;
        long daysSinceLast = orders.isEmpty() ? 999 :
                java.time.temporal.ChronoUnit.DAYS.between(
                        orders.get(0).getCreatedAt().toLocalDate(), LocalDate.now());

        // Last 3 order values
        String recentOrders = orders.stream()
                .limit(3)
                .map(o -> "$" + o.getTotal() + " (" + o.getStatus() + ")")
                .reduce((a, b) -> a + ", " + b)
                .orElse("No orders");

        // Open opportunities
        long openOpps = customer.getOpportunities().stream()
                .filter(o -> !o.getStage().name().equals("WON")
                        && !o.getStage().name().equals("LOST"))
                .count();

        return """
            Customer Name:     %s
            Company:           %s
            Industry:          %s
            Active:            %s
            Total Orders:      %d
            Total Spend:       $%.2f
            Days Since Last Order: %d
            Recent Orders:     %s
            Open Opportunities: %d
            Account Created:   %s
            Notes:             %s
            """.formatted(
                customer.getFullName(),
                customer.getCompany() != null ? customer.getCompany() : "N/A",
                customer.getIndustry() != null ? customer.getIndustry() : "N/A",
                customer.isActive(),
                totalOrders,
                totalSpend,
                daysSinceLast,
                recentOrders,
                openOpps,
                customer.getCreatedAt() != null ? customer.getCreatedAt().toLocalDate() : "N/A",
                customer.getNotes() != null ? customer.getNotes() : "None"
        );
    }

    // ── JSON Parser ───────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private CustomerInsightResponse parseInsightResponse(Customer customer, String rawJson) {
        try {
            // Strip markdown fences if AI included them
            String json = cleanJson(rawJson);
            var map = objectMapper.readValue(json, java.util.Map.class);

            int healthScore          = toInt(map.get("healthScore"), 50);
            int churnRisk            = toInt(map.get("churnRisk"), 50);
            int purchaseProbability  = toInt(map.get("purchaseProbability"), 50);

            return CustomerInsightResponse.builder()
                    .customerId(customer.getId())
                    .customerName(customer.getFullName())
                    .healthScore(healthScore)
                    .churnRisk(churnRisk)
                    .purchaseProbability(purchaseProbability)
                    .healthScoreLabel(scoreLabel(healthScore, false))
                    .churnRiskLabel(scoreLabel(churnRisk, true))
                    .purchaseProbabilityLabel(scoreLabel(purchaseProbability, false))
                    .insights(str(map.get("insights")))
                    .upsellSuggestions(toStringList(map.get("upsellSuggestions")))
                    .recommendedActions(toStringList(map.get("recommendedActions")))
                    .summary(str(map.get("summary")))
                    .build();

        } catch (Exception e) {
            log.error("Failed to parse customer insight JSON: {}", e.getMessage());
            return fallbackInsight(customer);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String cleanJson(String text) {
        return text.replaceAll("```json", "")
                .replaceAll("```", "")
                .trim();
    }

    private String scoreLabel(int score, boolean invertedScale) {
        if (invertedScale) {
            if (score >= 70) return "HIGH";
            if (score >= 40) return "MEDIUM";
            return "LOW";
        } else {
            if (score >= 70) return "HIGH";
            if (score >= 40) return "MEDIUM";
            return "LOW";
        }
    }

    private int toInt(Object val, int defaultVal) {
        if (val == null) return defaultVal;
        try { return ((Number) val).intValue(); }
        catch (Exception e) { return defaultVal; }
    }

    private String str(Object val) {
        return val != null ? val.toString() : "";
    }

    @SuppressWarnings("unchecked")
    private List<String> toStringList(Object val) {
        if (val instanceof List<?> list) {
            return list.stream().map(Object::toString).toList();
        }
        return List.of();
    }

    private CustomerInsightResponse fallbackInsight(Customer customer) {
        return CustomerInsightResponse.builder()
                .customerId(customer.getId())
                .customerName(customer.getFullName())
                .healthScore(50)
                .churnRisk(50)
                .purchaseProbability(50)
                .healthScoreLabel("MEDIUM")
                .churnRiskLabel("MEDIUM")
                .purchaseProbabilityLabel("MEDIUM")
                .insights("Unable to generate AI insights at this time.")
                .upsellSuggestions(List.of("Review purchase history manually"))
                .recommendedActions(List.of("Schedule follow-up call"))
                .summary("AI analysis temporarily unavailable.")
                .build();
    }
}