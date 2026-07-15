package com.aisalescrm.service;

import com.aisalescrm.dto.request.InvoicePaymentRequest;
import com.aisalescrm.dto.request.InvoiceRequest;
import com.aisalescrm.dto.response.InvoiceResponse;
import com.aisalescrm.dto.response.PageResponse;
import com.aisalescrm.enums.InvoiceStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface InvoiceService {

    InvoiceResponse createInvoice(InvoiceRequest request);

    InvoiceResponse generateFromOrder(Long orderId);

    InvoiceResponse getInvoiceById(Long id);

    PageResponse<InvoiceResponse> getAllInvoices(
            InvoiceStatus status, Long customerId, String search, Pageable pageable);

    InvoiceResponse markAsSent(Long id);

    InvoiceResponse markAsPaid(Long id, InvoicePaymentRequest request);

    InvoiceResponse cancelInvoice(Long id);

    List<InvoiceResponse> getOverdueInvoices();

    List<InvoiceResponse> getInvoicesByCustomer(Long customerId);

    byte[] downloadInvoicePdf(Long id);

    Map<String, Object> getInvoiceStats();
}
