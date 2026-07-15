package com.aisalescrm.service;

import com.aisalescrm.dto.request.ConvertLeadRequest;
import com.aisalescrm.dto.request.CustomerRequest;
import com.aisalescrm.dto.response.CustomerResponse;
import com.aisalescrm.dto.response.CustomerStatsResponse;
import com.aisalescrm.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomerService {

    CustomerResponse createCustomer(CustomerRequest request);

    CustomerResponse getCustomerById(Long id);

    PageResponse<CustomerResponse> getAllCustomers(
            Boolean active,
            Long assignedToId,
            String search,
            Pageable pageable
    );

    CustomerResponse updateCustomer(Long id, CustomerRequest request);

    CustomerResponse convertLeadToCustomer(ConvertLeadRequest request);

    void deleteCustomer(Long id);

    List<CustomerResponse> getTopCustomers(int limit);

    List<CustomerResponse> getRecentCustomers(int limit);

    CustomerStatsResponse getCustomerStats();

    void updatePurchaseSummary(Long customerId, double orderTotal);
}