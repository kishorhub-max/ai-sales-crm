package com.aisalescrm.dto.response;

import com.aisalescrm.enums.LeadSource;
import com.aisalescrm.enums.LeadStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String company;
    private String jobTitle;
    private LeadSource source;
    private LeadStatus status;
    private String notes;
    private String website;
    private String industry;
    private Integer employeeCount;
    private Double estimatedValue;

    // Assigned user — flattened
    private Long assignedToId;
    private String assignedToName;
    private String assignedToEmail;

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
}