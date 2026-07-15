package com.aisalescrm.controller;

import com.aisalescrm.dto.request.InvoicePaymentRequest;
import com.aisalescrm.dto.request.InvoiceRequest;
import com.aisalescrm.dto.response.InvoiceResponse;
import com.aisalescrm.dto.response.PageResponse;
import com.aisalescrm.enums.InvoiceStatus;
import com.aisalescrm.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/invoices")
@RequiredArgsConstructor
@Tag(name = "Invoice Management", description = "Generate, send, pay, and download invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    // ── CREATE (manual) ───────────────────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Create a manual invoice")
    public ResponseEntity<InvoiceResponse> create(
            @Valid @RequestBody InvoiceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(invoiceService.createInvoice(request));
    }

    // ── GENERATE FROM ORDER ───────────────────────────────────────────────────

    @PostMapping("/generate-from-order/{orderId}")
    @Operation(summary = "Auto-generate an invoice from an existing order")
    public ResponseEntity<InvoiceResponse> generateFromOrder(
            @PathVariable Long orderId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(invoiceService.generateFromOrder(orderId));
    }

    // ── READ single ───────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    @Operation(summary = "Get invoice by ID")
    public ResponseEntity<InvoiceResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.getInvoiceById(id));
    }

    // ── READ list ─────────────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "List invoices with filters and pagination")
    public ResponseEntity<PageResponse<InvoiceResponse>> getAll(
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0")         int page,
            @RequestParam(defaultValue = "20")        int size,
            @RequestParam(defaultValue = "issueDate") String sortBy,
            @RequestParam(defaultValue = "desc")      String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return ResponseEntity.ok(invoiceService.getAllInvoices(
                status, customerId, search, PageRequest.of(page, size, sort)));
    }

    // ── STATUS TRANSITIONS ────────────────────────────────────────────────────

    @PatchMapping("/{id}/send")
    @Operation(summary = "Mark invoice as SENT")
    public ResponseEntity<InvoiceResponse> markAsSent(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.markAsSent(id));
    }

    @PatchMapping("/{id}/pay")
    @Operation(summary = "Mark invoice as PAID")
    public ResponseEntity<InvoiceResponse> markAsPaid(
            @PathVariable Long id,
            @Valid @RequestBody InvoicePaymentRequest request) {
        return ResponseEntity.ok(invoiceService.markAsPaid(id, request));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel an invoice")
    public ResponseEntity<InvoiceResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.cancelInvoice(id));
    }

    // ── OVERDUE ───────────────────────────────────────────────────────────────

    @GetMapping("/overdue")
    @Operation(summary = "Get all overdue invoices")
    public ResponseEntity<List<InvoiceResponse>> overdue() {
        return ResponseEntity.ok(invoiceService.getOverdueInvoices());
    }

    // ── BY CUSTOMER ───────────────────────────────────────────────────────────

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get all invoices for a customer")
    public ResponseEntity<List<InvoiceResponse>> byCustomer(
            @PathVariable Long customerId) {
        return ResponseEntity.ok(invoiceService.getInvoicesByCustomer(customerId));
    }

    // ── PDF DOWNLOAD ──────────────────────────────────────────────────────────

    @GetMapping("/{id}/pdf")
    @Operation(summary = "Download invoice as PDF")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        byte[] pdf = invoiceService.downloadInvoicePdf(id);

        // Fetch invoice number for filename
        InvoiceResponse inv = invoiceService.getInvoiceById(id);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + inv.getInvoiceNumber() + ".pdf\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .body(pdf);
    }

    // ── STATS ─────────────────────────────────────────────────────────────────

    @GetMapping("/stats")
    @Operation(summary = "Invoice stats: paid revenue, outstanding, overdue count")
    public ResponseEntity<Map<String, Object>> stats() {
        return ResponseEntity.ok(invoiceService.getInvoiceStats());
    }
}