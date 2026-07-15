package com.aisalescrm.mapper;

import com.aisalescrm.dto.request.LeadRequest;
import com.aisalescrm.dto.response.LeadResponse;
import com.aisalescrm.entity.Lead;
import com.aisalescrm.entity.User;
import org.springframework.stereotype.Component;

@Component
public class LeadMapper {

    // ── Entity → Response ────────────────────────────────────────────────────

    public LeadResponse toResponse(Lead lead) {
        if (lead == null) return null;

        LeadResponse.LeadResponseBuilder builder = LeadResponse.builder()
                .id(lead.getId())
                .name(lead.getName())
                .email(lead.getEmail())
                .phone(lead.getPhone())
                .company(lead.getCompany())
                .jobTitle(lead.getJobTitle())
                .source(lead.getSource())
                .status(lead.getStatus())
                .notes(lead.getNotes())
                .website(lead.getWebsite())
                .industry(lead.getIndustry())
                .employeeCount(lead.getEmployeeCount())
                .estimatedValue(lead.getEstimatedValue())
                .createdAt(lead.getCreatedAt())
                .updatedAt(lead.getUpdatedAt())
                .createdBy(lead.getCreatedBy());

        User assignedTo = lead.getAssignedTo();
        if (assignedTo != null) {
            builder.assignedToId(assignedTo.getId())
                    .assignedToName(assignedTo.getFullName())
                    .assignedToEmail(assignedTo.getEmail());
        }

        return builder.build();
    }

    // ── Request → Entity (create) ────────────────────────────────────────────

    public Lead toEntity(LeadRequest request) {
        return Lead.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .company(request.getCompany())
                .jobTitle(request.getJobTitle())
                .source(request.getSource())
                .status(request.getStatus() != null
                        ? request.getStatus()
                        : com.aisalescrm.enums.LeadStatus.NEW)
                .notes(request.getNotes())
                .website(request.getWebsite())
                .industry(request.getIndustry())
                .employeeCount(request.getEmployeeCount())
                .estimatedValue(request.getEstimatedValue())
                .build();
    }

    // ── Request → Entity (update — patch existing fields) ────────────────────

    public void updateEntity(Lead lead, LeadRequest request) {
        lead.setName(request.getName());
        lead.setEmail(request.getEmail());
        lead.setPhone(request.getPhone());
        lead.setCompany(request.getCompany());
        lead.setJobTitle(request.getJobTitle());
        lead.setSource(request.getSource());
        if (request.getStatus() != null) {
            lead.setStatus(request.getStatus());
        }
        lead.setNotes(request.getNotes());
        lead.setWebsite(request.getWebsite());
        lead.setIndustry(request.getIndustry());
        lead.setEmployeeCount(request.getEmployeeCount());
        lead.setEstimatedValue(request.getEstimatedValue());
    }
}