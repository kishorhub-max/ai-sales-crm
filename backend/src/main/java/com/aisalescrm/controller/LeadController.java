package com.aisalescrm.controller;

import com.aisalescrm.dto.request.LeadAssignRequest;
import com.aisalescrm.dto.request.LeadRequest;
import com.aisalescrm.dto.response.LeadResponse;
import com.aisalescrm.dto.response.PageResponse;
import com.aisalescrm.enums.LeadSource;
import com.aisalescrm.enums.LeadStatus;
import com.aisalescrm.service.LeadService;
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
import java.util.Map;

@RestController
@RequestMapping("/leads")
@RequiredArgsConstructor
@Tag(name = "Lead Management", description = "Create, search, update, assign and delete leads")
public class LeadController {

    private final LeadService leadService;

    // ── CREATE ───────────────────────────────────────────────────────────────

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new lead")
    public ResponseEntity<LeadResponse> createLead(
            @Valid @RequestBody LeadRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(leadService.createLead(request));
    }

    // ── READ (single) ────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    @Operation(summary = "Get lead by ID")
    public ResponseEntity<LeadResponse> getLeadById(@PathVariable Long id) {
        return ResponseEntity.ok(leadService.getLeadById(id));
    }

    // ── READ (list with filters + pagination) ────────────────────────────────

    @GetMapping
    @Operation(summary = "List leads with optional filters, search, and pagination")
    public ResponseEntity<PageResponse<LeadResponse>> getAllLeads(
            @Parameter(description = "Filter by status")
            @RequestParam(required = false) LeadStatus status,

            @Parameter(description = "Filter by source")
            @RequestParam(required = false) LeadSource source,

            @Parameter(description = "Filter by assigned user ID")
            @RequestParam(required = false) Long assignedToId,

            @Parameter(description = "Search by name, email, or company")
            @RequestParam(required = false) String search,

            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(
                leadService.getAllLeads(status, source, assignedToId, search, pageable)
        );
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────

    @PutMapping("/{id}")
    @Operation(summary = "Update a lead")
    public ResponseEntity<LeadResponse> updateLead(
            @PathVariable Long id,
            @Valid @RequestBody LeadRequest request) {
        return ResponseEntity.ok(leadService.updateLead(id, request));
    }

    // ── ASSIGN ───────────────────────────────────────────────────────────────

    @PatchMapping("/{id}/assign")
    @Operation(summary = "Assign a lead to a sales rep")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER')")
    public ResponseEntity<LeadResponse> assignLead(
            @PathVariable Long id,
            @Valid @RequestBody LeadAssignRequest request) {
        return ResponseEntity.ok(leadService.assignLead(id, request));
    }

    // ── STATUS CHANGE ────────────────────────────────────────────────────────

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update lead status")
    public ResponseEntity<LeadResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam LeadStatus status) {
        return ResponseEntity.ok(leadService.updateStatus(id, status));
    }

    // ── DELETE ───────────────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a lead")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER')")
    public ResponseEntity<Void> deleteLead(@PathVariable Long id) {
        leadService.deleteLead(id);
        return ResponseEntity.noContent().build();
    }

    // ── LEADS BY USER ────────────────────────────────────────────────────────

    @GetMapping("/assigned/{userId}")
    @Operation(summary = "Get all leads assigned to a specific user")
    public ResponseEntity<List<LeadResponse>> getLeadsByUser(
            @PathVariable Long userId) {
        return ResponseEntity.ok(leadService.getLeadsByAssignedUser(userId));
    }

    // ── STATS ────────────────────────────────────────────────────────────────

    @GetMapping("/stats/by-status")
    @Operation(summary = "Count leads grouped by status")
    public ResponseEntity<Map<String, Long>> getStatusCounts() {
        return ResponseEntity.ok(leadService.getLeadStatusCounts());
    }

    @GetMapping("/stats/by-source")
    @Operation(summary = "Count leads grouped by source")
    public ResponseEntity<Map<String, Long>> getSourceCounts() {
        return ResponseEntity.ok(leadService.getLeadSourceCounts());
    }
}