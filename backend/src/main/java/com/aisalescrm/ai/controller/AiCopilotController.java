package com.aisalescrm.ai.controller;

import com.aisalescrm.ai.dto.*;
import com.aisalescrm.ai.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@Tag(name = "AI Sales Copilot", description = "AI-powered insights, analysis, chat, and email generation")
public class AiCopilotController {

    private final CustomerInsightService    customerInsightService;
    private final OpportunityAnalysisService opportunityAnalysisService;
    private final SalesChatService          salesChatService;
    private final EmailGeneratorService     emailGeneratorService;
    private final SalesSummaryService       salesSummaryService;

    // ── Feature 1: Customer Insights ─────────────────────────────────────────

    @PostMapping("/customer-insights")
    @Operation(
            summary = "AI Customer Insights",
            description = "Generate health score, churn risk, purchase probability, and upsell suggestions"
    )
    public ResponseEntity<CustomerInsightResponse> customerInsights(
            @Valid @RequestBody CustomerInsightRequest request) {
        return ResponseEntity.ok(
                customerInsightService.analyzeCustomer(request.getCustomerId()));
    }

    // Convenience GET version
    @GetMapping("/customer-insights/{customerId}")
    @Operation(summary = "AI Customer Insights by customer ID")
    public ResponseEntity<CustomerInsightResponse> customerInsightsById(
            @PathVariable Long customerId) {
        return ResponseEntity.ok(customerInsightService.analyzeCustomer(customerId));
    }

    // ── Feature 2: Opportunity Analysis ──────────────────────────────────────

    @PostMapping("/opportunity-analysis")
    @Operation(
            summary = "AI Opportunity Analysis",
            description = "Win probability, risk assessment, strengths, and next recommended actions"
    )
    public ResponseEntity<OpportunityAnalysisResponse> opportunityAnalysis(
            @Valid @RequestBody OpportunityAnalysisRequest request) {
        return ResponseEntity.ok(
                opportunityAnalysisService.analyzeOpportunity(request.getOpportunityId()));
    }

    @GetMapping("/opportunity-analysis/{opportunityId}")
    @Operation(summary = "AI Opportunity Analysis by ID")
    public ResponseEntity<OpportunityAnalysisResponse> opportunityAnalysisById(
            @PathVariable Long opportunityId) {
        return ResponseEntity.ok(
                opportunityAnalysisService.analyzeOpportunity(opportunityId));
    }

    // ── Feature 3: Sales Assistant Chat ──────────────────────────────────────

    @PostMapping("/chat")
    @Operation(
            summary = "AI Sales Assistant Chat",
            description = "Multi-turn chat with live CRM context. Ask about deals, customers, priorities."
    )
    public ResponseEntity<ChatResponse> chat(
            @Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(salesChatService.chat(request));
    }

    // ── Feature 4: Email Generator ────────────────────────────────────────────

    @PostMapping("/generate-email")
    @Operation(
            summary = "AI Email Generator",
            description = "Generate personalized sales emails: FOLLOW_UP | PROPOSAL | ENGAGEMENT | THANK_YOU"
    )
    public ResponseEntity<EmailGeneratorResponse> generateEmail(
            @Valid @RequestBody EmailGeneratorRequest request) {
        return ResponseEntity.ok(emailGeneratorService.generateEmail(request));
    }

    // ── Feature 5: Daily Sales Summary ───────────────────────────────────────

    @GetMapping("/daily-summary")
    @Operation(
            summary = "AI Daily Sales Summary",
            description = "AI-generated daily briefing: priorities, at-risk deals, follow-ups, forecast"
    )
    public ResponseEntity<SalesSummaryResponse> dailySummary() {
        return ResponseEntity.ok(salesSummaryService.generateDailySummary());
    }
}