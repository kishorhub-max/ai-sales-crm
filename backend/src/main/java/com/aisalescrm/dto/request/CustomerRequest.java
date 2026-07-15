package com.aisalescrm.dto.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CustomerRequest {

    // ── Profile ──────────────────────────────────────────────────────────────

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String phone;
    private String mobile;

    // ── Company ──────────────────────────────────────────────────────────────

    private String company;
    private String jobTitle;
    private String industry;
    private String website;

    // ── Address ──────────────────────────────────────────────────────────────

    private String street;
    private String city;
    private String state;
    private String country;
    private String zipCode;

    // ── Meta ─────────────────────────────────────────────────────────────────

    @Size(max = 3000, message = "Notes must not exceed 3000 characters")
    private String notes;

    private Long assignedToId;
    private boolean active = true;
}