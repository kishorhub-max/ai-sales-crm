package com.aisalescrm.dto.response;

import com.aisalescrm.enums.OpportunityStage;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpportunityResponse {

    private Long             id;
    private String           dealName;
    private BigDecimal       value;
    private OpportunityStage stage;
    private Integer          probability;
    private LocalDate        expectedCloseDate;
    private String           description;
    private String           lostReason;

    // Weighted value = value * probability / 100
    private BigDecimal weightedValue;

    // Customer — flattened
    private Long   customerId;
    private String customerName;
    private String customerCompany;

    // Assigned rep — flattened
    private Long   assignedToId;
    private String assignedToName;

    // Product — flattened
    private Long   productId;
    private String productName;

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}