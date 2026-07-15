package com.aisalescrm.ai.service;

import com.aisalescrm.ai.dto.EmailGeneratorRequest;
import com.aisalescrm.ai.dto.EmailGeneratorResponse;
import com.aisalescrm.entity.Customer;
import com.aisalescrm.entity.Opportunity;
import com.aisalescrm.exception.ResourceNotFoundException;
import com.aisalescrm.repository.CustomerRepository;
import com.aisalescrm.repository.OpportunityRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailGeneratorService {

    private final CustomerRepository    customerRepository;
    private final OpportunityRepository opportunityRepository;
    private final GeminiService         geminiService;
    private final ObjectMapper          objectMapper;

    private static final String SYSTEM_INSTRUCTION = """
        You are an expert B2B sales email copywriter.
        Write professional, personalized, and compelling sales emails.
        Return ONLY valid JSON — no markdown, no backticks, no extra text.
        Make emails concise, warm, and focused on customer value.
        """;

    @Transactional(readOnly = true)
    public EmailGeneratorResponse generateEmail(EmailGeneratorRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", request.getCustomerId()));

        Opportunity opportunity = null;
        if (request.getOpportunityId() != null) {
            opportunity = opportunityRepository.findById(request.getOpportunityId())
                    .orElse(null);
        }

        String context  = buildContext(customer, opportunity, request);
        String prompt   = buildEmailPrompt(request.getEmailType(), context);
        String rawJson  = geminiService.sendPrompt(SYSTEM_INSTRUCTION, prompt);

        return parseEmailResponse(rawJson, request.getEmailType());
    }

    // ── Context Builder ───────────────────────────────────────────────────────

    private String buildContext(Customer customer,
                                Opportunity opportunity,
                                EmailGeneratorRequest request) {
        StringBuilder ctx = new StringBuilder();

        ctx.append("Customer: ").append(customer.getFullName()).append("\n");
        ctx.append("Company:  ").append(
                customer.getCompany() != null ? customer.getCompany() : "N/A").append("\n");
        ctx.append("Industry: ").append(
                customer.getIndustry() != null ? customer.getIndustry() : "N/A").append("\n");
        ctx.append("Total Purchases: $").append(
                customer.getTotalPurchaseValue() != null ? customer.getTotalPurchaseValue() : 0).append("\n");

        if (opportunity != null) {
            ctx.append("\nDeal Name:  ").append(opportunity.getDealName()).append("\n");
            ctx.append("Deal Value: $").append(opportunity.getValue()).append("\n");
            ctx.append("Stage:      ").append(opportunity.getStage()).append("\n");
            ctx.append("Product:    ").append(
                    opportunity.getProduct() != null
                            ? opportunity.getProduct().getName() : "N/A").append("\n");
        }

        if (request.getAdditionalContext() != null && !request.getAdditionalContext().isBlank()) {
            ctx.append("\nAdditional Context: ").append(request.getAdditionalContext()).append("\n");
        }

        return ctx.toString();
    }

    private String buildEmailPrompt(String emailType, String context) {
        String typeInstruction = switch (emailType.toUpperCase()) {
            case "FOLLOW_UP" -> """
                Write a warm follow-up email to re-engage a customer.
                Reference their previous interactions. Ask for a meeting.
                """;
            case "PROPOSAL" -> """
                Write a compelling proposal email presenting our solution.
                Highlight value, ROI, and next steps clearly.
                """;
            case "ENGAGEMENT" -> """
                Write a value-add engagement email sharing industry insights
                or a helpful resource. Build relationship, don't push for sale.
                """;
            case "THANK_YOU" -> """
                Write a sincere thank-you email after a purchase or meeting.
                Express gratitude and open the door for future business.
                """;
            default -> """
                Write a professional sales email appropriate for the business context.
                """;
        };

        return """
            %s

            Return a JSON object with exactly these fields:
            {
              "subject": "<compelling email subject line>",
              "body": "<full email body with proper line breaks using \\n>",
              "suggestedSubjectLines": ["<alt subject 1>", "<alt subject 2>", "<alt subject 3>"]
            }

            Context:
            %s
            """.formatted(typeInstruction, context);
    }

    // ── JSON Parser ───────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private EmailGeneratorResponse parseEmailResponse(String rawJson, String emailType) {
        try {
            String json = rawJson.replaceAll("```json", "").replaceAll("```", "").trim();
            var map = objectMapper.readValue(json, java.util.Map.class);

            List<String> subjects = map.get("suggestedSubjectLines") instanceof List<?> list
                    ? list.stream().map(Object::toString).toList()
                    : List.of();

            return EmailGeneratorResponse.builder()
                    .emailType(emailType)
                    .subject(str(map.get("subject")))
                    .body(str(map.get("body")))
                    .suggestedSubjectLines(subjects)
                    .build();

        } catch (Exception e) {
            log.error("Failed to parse email JSON: {}", e.getMessage());
            return EmailGeneratorResponse.builder()
                    .emailType(emailType)
                    .subject("Follow up from AI Sales CRM")
                    .body("Dear Customer,\n\nThank you for your continued partnership.\n\nBest regards")
                    .suggestedSubjectLines(List.of())
                    .build();
        }
    }

    private String str(Object val) {
        return val != null ? val.toString() : "";
    }
}