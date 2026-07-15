package com.aisalescrm.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerStatsResponse {

    private long totalCustomers;
    private long activeCustomers;
    private long customersWithPurchases;
    private Double totalRevenue;
    private Map<String, Long> byIndustry;
}