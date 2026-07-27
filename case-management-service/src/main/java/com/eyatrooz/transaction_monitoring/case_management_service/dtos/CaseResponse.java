package com.eyatrooz.transaction_monitoring.case_management_service.dtos;

import com.eyatrooz.transaction_monitoring.case_management_service.enums.CaseStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
public class CaseResponse {
    private Long id;
    private Long transactionId;
    private String accountId;
    private BigDecimal riskScore;
    private CaseStatus status;
    private String assignedAnalyst;
    private Instant createdAt;
    private Instant updatedAt;
    private List<CaseHistoryResponse> history;
}
