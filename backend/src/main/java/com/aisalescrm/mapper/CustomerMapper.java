package com.aisalescrm.mapper;

import com.aisalescrm.dto.request.CustomerRequest;
import com.aisalescrm.dto.response.CustomerResponse;
import com.aisalescrm.entity.Customer;
import com.aisalescrm.entity.User;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    // ── Entity → Response ────────────────────────────────────────────────────

    public CustomerResponse toResponse(Customer c) {
        if (c == null) return null;

        CustomerResponse.CustomerResponseBuilder b = CustomerResponse.builder()
                .id(c.getId())
                .firstName(c.getFirstName())
                .lastName(c.getLastName())
                .fullName(c.getFullName())
                .email(c.getEmail())
                .phone(c.getPhone())
                .mobile(c.getMobile())
                .company(c.getCompany())
                .jobTitle(c.getJobTitle())
                .industry(c.getIndustry())
                .website(c.getWebsite())
                .street(c.getStreet())
                .city(c.getCity())
                .state(c.getState())
                .country(c.getCountry())
                .zipCode(c.getZipCode())
                .notes(c.getNotes())
                .active(c.isActive())
                .totalPurchaseValue(c.getTotalPurchaseValue())
                .purchaseCount(c.getPurchaseCount())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .createdBy(c.getCreatedBy());

        if (c.getAssignedTo() != null) {
            User u = c.getAssignedTo();
            b.assignedToId(u.getId())
                    .assignedToName(u.getFullName())
                    .assignedToEmail(u.getEmail());
        }

        if (c.getConvertedFromLead() != null) {
            b.convertedFromLeadId(c.getConvertedFromLead().getId());
        }

        return b.build();
    }

    // ── Request → Entity (create) ────────────────────────────────────────────

    public Customer toEntity(CustomerRequest req) {
        return Customer.builder()
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .mobile(req.getMobile())
                .company(req.getCompany())
                .jobTitle(req.getJobTitle())
                .industry(req.getIndustry())
                .website(req.getWebsite())
                .street(req.getStreet())
                .city(req.getCity())
                .state(req.getState())
                .country(req.getCountry())
                .zipCode(req.getZipCode())
                .notes(req.getNotes())
                .active(req.isActive())
                .totalPurchaseValue(0.0)
                .purchaseCount(0)
                .build();
    }

    // ── Request → Entity (update) ────────────────────────────────────────────

    public void updateEntity(Customer c, CustomerRequest req) {
        c.setFirstName(req.getFirstName());
        c.setLastName(req.getLastName());
        c.setEmail(req.getEmail());
        c.setPhone(req.getPhone());
        c.setMobile(req.getMobile());
        c.setCompany(req.getCompany());
        c.setJobTitle(req.getJobTitle());
        c.setIndustry(req.getIndustry());
        c.setWebsite(req.getWebsite());
        c.setStreet(req.getStreet());
        c.setCity(req.getCity());
        c.setState(req.getState());
        c.setCountry(req.getCountry());
        c.setZipCode(req.getZipCode());
        c.setNotes(req.getNotes());
        c.setActive(req.isActive());
    }
}