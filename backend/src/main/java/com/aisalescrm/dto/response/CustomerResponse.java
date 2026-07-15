package com.aisalescrm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {

    private Long id;

    // Profile
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phone;
    private String mobile;

    // Company
    private String company;
    private String jobTitle;
    private String industry;
    private String website;

    // Address
    private String street;
    private String city;
    private String state;
    private String country;
    private String zipCode;

    // Meta
    private String notes;
    private boolean active;

    // Purchase summary
    private Double totalPurchaseValue;
    private Integer purchaseCount;

    // Assigned rep
    private Long   assignedToId;
    private String assignedToName;
    private String assignedToEmail;

    // Converted from lead
    private Long convertedFromLeadId;

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
}

