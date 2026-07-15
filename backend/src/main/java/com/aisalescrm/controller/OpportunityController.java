package com.aisalescrm.controller;

import com.aisalescrm.dto.request.OpportunityRequest;
import com.aisalescrm.dto.request.OpportunityStageUpdateRequest;
import com.aisalescrm.dto.response.OpportunityResponse;
import com.aisalescrm.dto.response.PageResponse;
import com.aisalescrm.dto.response.PipelineStatsResponse;
import com.aisalescrm.enums.OpportunityStage;
import com.aisalescrm.service.OpportunityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/opportunities")
@RequiredArgsConstructor
@Tag(name = "Opportunity Management", description = "Sales pipeline and deal management")
public class OpportunityController {

    private final OpportunityService opportunityService;

    // ── CREATE ───────────────────────────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Create a new opportunity")
    public ResponseEntity<OpportunityResponse> create(
            @Valid @RequestBody OpportunityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(opportunityService.createOpportunity(request));
    }

    // ── READ single ──────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    @Operation(summary = "Get opportunity by ID")
    public ResponseEntity<OpportunityResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(opportunityService.getOpportunityById(id));
    }

    // ── READ list ────────────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "List opportunities with filters and pagination")
    public ResponseEntity<PageResponse<OpportunityResponse>> getAll(
            @RequestParam(required = false) OpportunityStage stage,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long assignedToId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0")           int page,
            @RequestParam(defaultValue = "20")          int size,
            @RequestParam(defaultValue = "createdAt")   String sortBy,
            @RequestParam(defaultValue = "desc")        String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return ResponseEntity.ok(opportunityService.getAllOpportunities(
                stage, customerId, assignedToId, search,
                PageRequest.of(page, size, sort)));
    }

    // ── UPDATE full ──────────────────────────────────────────────────────────

    @PutMapping("/{id}")
    @Operation(summary = "Update an opportunity")
    public ResponseEntity<OpportunityResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody OpportunityRequest request) {
        return ResponseEntity.ok(opportunityService.updateOpportunity(id, request));
    }

    // ── UPDATE stage only ────────────────────────────────────────────────────

    @PatchMapping("/{id}/stage")
    @Operation(summary = "Move opportunity to a new pipeline stage")
    public ResponseEntity<OpportunityResponse> updateStage(
            @PathVariable Long id,
            @Valid @RequestBody OpportunityStageUpdateRequest request) {
        return ResponseEntity.ok(opportunityService.updateStage(id, request));
    }

    // ── DELETE ───────────────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER')")
    @Operation(summary = "Delete an opportunity")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        opportunityService.deleteOpportunity(id);
        return ResponseEntity.noContent().build();
    }

    // ── BY CUSTOMER ──────────────────────────────────────────────────────────

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get all opportunities for a customer")
    public ResponseEntity<List<OpportunityResponse>> byCustomer(
            @PathVariable Long customerId) {
        return ResponseEntity.ok(opportunityService.getOpportunitiesByCustomer(customerId));
    }

    // ── AT RISK ──────────────────────────────────────────────────────────────

    @GetMapping("/at-risk")
    @Operation(summary = "Get overdue open opportunities")
    public ResponseEntity<List<OpportunityResponse>> atRisk() {
        return ResponseEntity.ok(opportunityService.getAtRiskOpportunities());
    }

    // ── CLOSING SOON ─────────────────────────────────────────────────────────

    @GetMapping("/closing-soon")
    @Operation(summary = "Get opportunities closing within N days")
    public ResponseEntity<List<OpportunityResponse>> closingSoon(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(opportunityService.getClosingSoon(days));
    }

    // ── PIPELINE STATS ───────────────────────────────────────────────────────

    @GetMapping("/pipeline")
    @Operation(summary = "Full pipeline stats: stages, revenue, monthly trend")
    public ResponseEntity<PipelineStatsResponse> pipeline() {
        return ResponseEntity.ok(opportunityService.getPipelineStats());
    }
}