package com.aisalescrm.service.impl;

import com.aisalescrm.dto.request.InvoicePaymentRequest;
import com.aisalescrm.dto.request.InvoiceRequest;
import com.aisalescrm.dto.response.InvoiceResponse;
import com.aisalescrm.dto.response.PageResponse;
import com.aisalescrm.entity.Customer;
import com.aisalescrm.entity.Invoice;
import com.aisalescrm.entity.Order;
import com.aisalescrm.enums.InvoiceStatus;
import com.aisalescrm.exception.BusinessException;
import com.aisalescrm.exception.DuplicateResourceException;
import com.aisalescrm.exception.ResourceNotFoundException;
import com.aisalescrm.mapper.InvoiceMapper;
import com.aisalescrm.repository.CustomerRepository;
import com.aisalescrm.repository.InvoiceRepository;
import com.aisalescrm.repository.OrderRepository;
import com.aisalescrm.service.InvoiceService;
import com.aisalescrm.service.PdfGeneratorService;
import com.aisalescrm.util.OrderNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository  invoiceRepository;
    private final OrderRepository    orderRepository;
    private final CustomerRepository customerRepository;
    private final InvoiceMapper      invoiceMapper;
    private final PdfGeneratorService pdfGeneratorService;
    private final OrderNumberGenerator numberGenerator;

    // ── Create (manual) ──────────────────────────────────────────────────────

    @Override
    public InvoiceResponse createInvoice(InvoiceRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", request.getCustomerId()));

        Invoice invoice = Invoice.builder()
                .invoiceNumber(numberGenerator.generateInvoiceNumber())
                .customer(customer)
                .status(InvoiceStatus.DRAFT)
                .issueDate(request.getIssueDate())
                .dueDate(request.getDueDate())
                .notes(request.getNotes())
                .build();

        // Financials
        BigDecimal subtotal       = nullSafe(request.getSubtotal());
        BigDecimal discountPct    = nullSafe(request.getDiscountPercent());
        BigDecimal taxPct         = nullSafe(request.getTaxPercent());
        BigDecimal discountAmount = subtotal.multiply(discountPct)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal afterDiscount  = subtotal.subtract(discountAmount);
        BigDecimal taxAmount      = afterDiscount.multiply(taxPct)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal total          = afterDiscount.add(taxAmount);

        invoice.setSubtotal(subtotal);
        invoice.setDiscountPercent(discountPct);
        invoice.setDiscountAmount(discountAmount);
        invoice.setTaxPercent(taxPct);
        invoice.setTaxAmount(taxAmount);
        invoice.setTotal(total);

        // Link order if provided
        if (request.getOrderId() != null) {
            Order order = orderRepository.findById(request.getOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Order", request.getOrderId()));
            invoice.setOrder(order);
        }

        Invoice saved = invoiceRepository.save(invoice);
        log.info("Invoice created: {}", saved.getInvoiceNumber());
        return invoiceMapper.toResponse(saved);
    }

    // ── Generate from Order ───────────────────────────────────────────────────

    @Override
    public InvoiceResponse generateFromOrder(Long orderId) {
        // Guard: prevent duplicate invoice per order
        invoiceRepository.findByOrderId(orderId).ifPresent(inv -> {
            throw new DuplicateResourceException(
                    "Invoice already exists for order. Invoice: " + inv.getInvoiceNumber());
        });

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        Invoice invoice = Invoice.builder()
                .invoiceNumber(numberGenerator.generateInvoiceNumber())
                .customer(order.getCustomer())
                .order(order)
                .status(InvoiceStatus.DRAFT)
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))
                // Copy financials directly from order
                .subtotal(order.getSubtotal())
                .discountPercent(order.getDiscountPercent())
                .discountAmount(order.getDiscountAmount())
                .taxPercent(order.getTaxPercent())
                .taxAmount(order.getTaxAmount())
                .total(order.getTotal())
                .build();

        Invoice saved = invoiceRepository.save(invoice);
        log.info("Invoice {} generated from order {}", saved.getInvoiceNumber(), order.getOrderNumber());
        return invoiceMapper.toResponse(saved);
    }

    // ── Read ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceById(Long id) {
        return invoiceMapper.toResponse(findInvoice(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InvoiceResponse> getAllInvoices(InvoiceStatus status,
                                                        Long customerId,
                                                        String search,
                                                        Pageable pageable) {
        String cleanSearch = search == null ? "" : search.trim();        Page<Invoice> page = invoiceRepository.findAllWithFilters(
                status, customerId, cleanSearch, pageable);
        return PageResponse.of(page.map(invoiceMapper::toResponse));
    }

    // ── Status Transitions ────────────────────────────────────────────────────

    @Override
    public InvoiceResponse markAsSent(Long id) {
        Invoice invoice = findInvoice(id);
        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new BusinessException("Only DRAFT invoices can be marked as SENT");
        }
        invoice.setStatus(InvoiceStatus.SENT);
        return invoiceMapper.toResponse(invoiceRepository.save(invoice));
    }

    @Override
    public InvoiceResponse markAsPaid(Long id, InvoicePaymentRequest request) {
        Invoice invoice = findInvoice(id);
        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new BusinessException("Cannot mark a cancelled invoice as paid");
        }
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new BusinessException("Invoice is already marked as paid");
        }
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidDate(request.getPaidDate());
        invoice.setPaymentMethod(request.getPaymentMethod());
        Invoice saved = invoiceRepository.save(invoice);
        log.info("Invoice {} marked as PAID via {}", saved.getInvoiceNumber(), request.getPaymentMethod());
        return invoiceMapper.toResponse(saved);
    }

    @Override
    public InvoiceResponse cancelInvoice(Long id) {
        Invoice invoice = findInvoice(id);
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new BusinessException("Cannot cancel a paid invoice");
        }
        invoice.setStatus(InvoiceStatus.CANCELLED);
        return invoiceMapper.toResponse(invoiceRepository.save(invoice));
    }

    // ── Overdue ───────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getOverdueInvoices() {
        return invoiceRepository.findOverdueInvoices(LocalDate.now())
                .stream().map(invoiceMapper::toResponse).collect(Collectors.toList());
    }

    // ── By Customer ───────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getInvoicesByCustomer(Long customerId) {
        return invoiceRepository.findByCustomerIdOrderByIssueDateDesc(customerId)
                .stream().map(invoiceMapper::toResponse).collect(Collectors.toList());
    }

    // ── PDF Download ──────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadInvoicePdf(Long id) {
        Invoice invoice = findInvoice(id);
        return pdfGeneratorService.generateInvoicePdf(invoice);
    }

    // ── Stats ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getInvoiceStats() {
        Map<String, Object> stats = new HashMap<>();

        BigDecimal paid        = invoiceRepository.sumPaidRevenue();
        BigDecimal outstanding = invoiceRepository.sumOutstandingRevenue();

        stats.put("paidRevenue",        paid        != null ? paid        : BigDecimal.ZERO);
        stats.put("outstandingRevenue", outstanding != null ? outstanding : BigDecimal.ZERO);
        stats.put("overdueCount",       invoiceRepository.findOverdueInvoices(LocalDate.now()).size());
        stats.put("totalInvoices",      invoiceRepository.count());

        Map<String, Map<String, Object>> byStatus = new HashMap<>();
        for (Object[] row : invoiceRepository.getStatsByStatus()) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("count",   row[1]);
            entry.put("total",   row[2] != null ? row[2] : BigDecimal.ZERO);
            byStatus.put(row[0].toString(), entry);
        }
        stats.put("byStatus", byStatus);

        return stats;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Invoice findInvoice(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", id));
    }

    private BigDecimal nullSafe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}