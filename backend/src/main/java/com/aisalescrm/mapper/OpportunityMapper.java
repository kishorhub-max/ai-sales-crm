package com.aisalescrm.mapper;

import com.aisalescrm.dto.request.OpportunityRequest;
import com.aisalescrm.dto.response.OpportunityResponse;
import com.aisalescrm.entity.Opportunity;
import com.aisalescrm.enums.OpportunityStage;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class OpportunityMapper {

    public OpportunityResponse toResponse(Opportunity o) {
        if (o == null) return null;

        // Weighted value = value × probability / 100
        BigDecimal weighted = BigDecimal.ZERO;
        if (o.getValue() != null && o.getProbability() != null) {
            weighted = o.getValue()
                    .multiply(BigDecimal.valueOf(o.getProbability()))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        OpportunityResponse.OpportunityResponseBuilder b = OpportunityResponse.builder()
                .id(o.getId())
                .dealName(o.getDealName())
                .value(o.getValue())
                .stage(o.getStage())
                .probability(o.getProbability())
                .expectedCloseDate(o.getExpectedCloseDate())
                .description(o.getDescription())
                .lostReason(o.getLostReason())
                .weightedValue(weighted)
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt());

        if (o.getCustomer() != null) {
            b.customerId(o.getCustomer().getId())
                    .customerName(o.getCustomer().getFullName())
                    .customerCompany(o.getCustomer().getCompany());
        }
        if (o.getAssignedTo() != null) {
            b.assignedToId(o.getAssignedTo().getId())
                    .assignedToName(o.getAssignedTo().getFullName());
        }
        if (o.getProduct() != null) {
            b.productId(o.getProduct().getId())
                    .productName(o.getProduct().getName());
        }

        return b.build();
    }

    public Opportunity toEntity(OpportunityRequest req) {
        return Opportunity.builder()
                .dealName(req.getDealName())
                .value(req.getValue())
                .stage(req.getStage() != null ? req.getStage() : OpportunityStage.NEW)
                .probability(req.getProbability() != null ? req.getProbability() : defaultProbability(req.getStage()))
                .expectedCloseDate(req.getExpectedCloseDate())
                .description(req.getDescription())
                .lostReason(req.getLostReason())
                .build();
    }

    public void updateEntity(Opportunity o, OpportunityRequest req) {
        o.setDealName(req.getDealName());
        o.setValue(req.getValue());
        if (req.getStage() != null)       o.setStage(req.getStage());
        if (req.getProbability() != null) o.setProbability(req.getProbability());
        else o.setProbability(defaultProbability(o.getStage()));
        o.setExpectedCloseDate(req.getExpectedCloseDate());
        o.setDescription(req.getDescription());
        o.setLostReason(req.getLostReason());
    }

    // Default probability by stage if not explicitly provided
    private int defaultProbability(OpportunityStage stage) {
        if (stage == null) return 10;
        return switch (stage) {
            case NEW         -> 10;
            case QUALIFIED   -> 25;
            case PROPOSAL    -> 50;
            case NEGOTIATION -> 75;
            case WON         -> 100;
            case LOST        -> 0;
        };
    }
}