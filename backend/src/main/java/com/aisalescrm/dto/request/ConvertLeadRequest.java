package com.aisalescrm.dto.request;



import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ConvertLeadRequest {

    @NotNull(message = "Lead ID is required")
    private Long leadId;

    // Optional overrides — if null, values are copied from the lead
    private String company;
    private String jobTitle;
    private String industry;
    private String website;
    private String notes;
    private Long assignedToId;
}