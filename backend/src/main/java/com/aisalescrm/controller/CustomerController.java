package com.aisalescrm.controller;

import com.aisalescrm.dto.request.ConvertLeadRequest;
import com.aisalescrm.dto.request.CustomerRequest;
import com.aisalescrm.dto.response.CustomerResponse;
import com.aisalescrm.dto.response.CustomerStatsResponse;
import com.aisalescrm.dto.response.PageResponse;
import com.aisalescrm.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
@Tag(name = "Customer Management", description = "Manage customers, profiles, purchase history, and lead conversion")
public class CustomerController {

    private final CustomerService customerService;

    // ── CREATE ───────────────────────────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Create a new customer")
    public ResponseEntity<CustomerResponse> createCustomer(
            @Valid @RequestBody CustomerRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(customerService.createCustomer(request));
    }

    // ── READ (single) ────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    // ── READ (list with filters + pagination) ────────────────────────────────

    @GetMapping
    @Operation(summary = "List customers with optional filters, search, and pagination")
    public ResponseEntity<PageResponse<CustomerResponse>> getAllCustomers(
            @Parameter(description = "Filter by active status")
            @RequestParam(required = false) Boolean active,

            @Parameter(description = "Filter by assigned user ID")
            @RequestParam(required = false) Long assignedToId,

            @Parameter(description = "Search by name, email, company, or phone")
            @RequestParam(required = false) String search,

            @RequestParam(defaultValue = "0")          int page,
            @RequestParam(defaultValue = "20")         int size,
            @RequestParam(defaultValue = "createdAt")  String sortBy,
            @RequestParam(defaultValue = "desc")       String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(
                customerService.getAllCustomers(active, assignedToId, search, pageable));
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────

    @PutMapping("/{id}")
    @Operation(summary = "Update customer details")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(customerService.updateCustomer(id, request));
    }

    // ── CONVERT LEAD → CUSTOMER ──────────────────────────────────────────────

    @PostMapping("/convert-lead")
    @Operation(summary = "Convert an existing lead into a customer")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER','SALES_REPRESENTATIVE')")
    public ResponseEntity<CustomerResponse> convertLead(
            @Valid @RequestBody ConvertLeadRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(customerService.convertLeadToCustomer(request));
    }

    // ── DELETE (soft) ────────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deactivate (soft-delete) a customer")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER')")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    // ── TOP CUSTOMERS ────────────────────────────────────────────────────────

    @GetMapping("/top")
    @Operation(summary = "Get top customers by total purchase value")
    public ResponseEntity<List<CustomerResponse>> getTopCustomers(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(customerService.getTopCustomers(limit));
    }

    // ── RECENT CUSTOMERS ─────────────────────────────────────────────────────

    @GetMapping("/recent")
    @Operation(summary = "Get recently added customers")
    public ResponseEntity<List<CustomerResponse>> getRecentCustomers(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(customerService.getRecentCustomers(limit));
    }

    // ── STATS ────────────────────────────────────────────────────────────────

    @GetMapping("/stats")
    @Operation(summary = "Get customer statistics (totals, revenue, industry breakdown)")
    public ResponseEntity<CustomerStatsResponse> getStats() {
        return ResponseEntity.ok(customerService.getCustomerStats());
    }
}