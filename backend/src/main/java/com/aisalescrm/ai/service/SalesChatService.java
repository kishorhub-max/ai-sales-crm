package com.aisalescrm.ai.service;

import com.aisalescrm.ai.dto.ChatMessageDto;
import com.aisalescrm.ai.dto.ChatRequest;
import com.aisalescrm.ai.dto.ChatResponse;
import com.aisalescrm.enums.LeadStatus;
import com.aisalescrm.enums.OpportunityStage;
import com.aisalescrm.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalesChatService {

    private final LeadRepository        leadRepository;
    private final CustomerRepository    customerRepository;
    private final OpportunityRepository opportunityRepository;
    private final OrderRepository       orderRepository;
    private final InvoiceRepository     invoiceRepository;
    private final GeminiService         geminiService;

    // ── Chat ─────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ChatResponse chat(ChatRequest request) {
        String systemInstruction = buildSystemInstruction();
        List<Map<String, Object>> history = convertHistory(request.getHistory());

        String reply = geminiService.sendChat(
                systemInstruction, history, request.getMessage());

        return ChatResponse.builder()
                .reply(reply)
                .model("gemini-3.1-flash-lite")
                .tokensUsed(0L)
                .build();
    }

    // ── System instruction with live CRM snapshot ─────────────────────────────

    private String buildSystemInstruction() {
        // Gather live CRM stats
        long   totalLeads        = leadRepository.count();
        long   newLeads          = leadRepository.countByStatus(LeadStatus.NEW);
        long   qualifiedLeads    = leadRepository.countByStatus(LeadStatus.QUALIFIED);

        long   activeCustomers   = customerRepository.countByActiveTrue();

        long   openOpps          = opportunityRepository.count()
                - opportunityRepository.countByStage(OpportunityStage.WON)
                - opportunityRepository.countByStage(OpportunityStage.LOST);
        long   wonOpps           = opportunityRepository.countByStage(OpportunityStage.WON);
        BigDecimal wonRevenue    = opportunityRepository.sumWonRevenue();
        BigDecimal weightedPipeline = opportunityRepository.sumWeightedPipeline();

        List<?> atRisk           = opportunityRepository.findAtRiskOpportunities(LocalDate.now());
        List<?> closingSoon      = opportunityRepository.findClosingSoon(
                LocalDate.now(), LocalDate.now().plusDays(30));

        long   overdueInvoices   = invoiceRepository.findOverdueInvoices(LocalDate.now()).size();
        BigDecimal paidRevenue   = invoiceRepository.sumPaidRevenue();

        // Recent leads (names only for context)
        String recentLeadNames = leadRepository
                .findRecentLeads(PageRequest.of(0, 5))
                .stream()
                .map(l -> l.getName() + " (" + l.getStatus() + ")")
                .reduce((a, b) -> a + ", " + b)
                .orElse("None");

        return """
            You are an intelligent AI Sales Copilot for a CRM system.
            You help sales reps make smart decisions, prioritize their work, and close more deals.
            Be concise, specific, and actionable. Use bullet points when listing items.
            Always reference the live CRM data provided below.

            ═══ LIVE CRM SNAPSHOT ═══

            LEADS
            • Total:      %d
            • New:        %d
            • Qualified:  %d
            • Recent:     %s

            CUSTOMERS
            • Active:     %d

            OPPORTUNITIES
            • Open:       %d
            • Won:        %d
            • Won Revenue:$%s
            • Pipeline:   $%s (weighted)
            • At-Risk:    %d overdue deals
            • Closing Soon (30d): %d

            INVOICES
            • Paid Revenue: $%s
            • Overdue:    %d

            ═══════════════════════════

            Answer questions about the CRM data above.
            Common questions:
            - "Which customers need follow-up?" → look at recent activity, overdue invoices
            - "Which deals are at risk?" → at-risk opportunities
            - "Show top opportunities" → open opps with high value/probability
            - "Summarize customer activity" → reference customer & order stats
            """.formatted(
                totalLeads, newLeads, qualifiedLeads, recentLeadNames,
                activeCustomers,
                openOpps, wonOpps,
                wonRevenue    != null ? wonRevenue.toPlainString()        : "0",
                weightedPipeline != null ? weightedPipeline.toPlainString() : "0",
                atRisk.size(), closingSoon.size(),
                paidRevenue   != null ? paidRevenue.toPlainString()       : "0",
                overdueInvoices
        );
    }

    // ── Convert chat history to Gemini format ─────────────────────────────────

    private List<Map<String, Object>> convertHistory(List<ChatMessageDto> history) {
        if (history == null || history.isEmpty()) return new ArrayList<>();

        return history.stream()
                .map(msg -> (Map<String, Object>) Map.of(
                        "role",  msg.getRole(),
                        "parts", List.of(Map.of("text", msg.getContent()))
                ))
                .toList();
    }
}