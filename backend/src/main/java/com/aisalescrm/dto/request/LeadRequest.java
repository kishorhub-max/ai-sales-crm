package com.aisalescrm.dto.request;

import com.aisalescrm.enums.LeadSource;
import com.aisalescrm.enums.LeadStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LeadRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String phone;

    @Size(max = 100, message = "Company name must not exceed 100 characters")
    private String company;

    private String jobTitle;

    @NotNull(message = "Source is required")
    private LeadSource source;

    private LeadStatus status;

    private Long assignedToId;

    @Size(max = 2000, message = "Notes must not exceed 2000 characters")
    private String notes;

    private String website;
    private String industry;
    private Integer employeeCount;
    private Double estimatedValue;
}