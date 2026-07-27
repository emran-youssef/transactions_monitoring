package com.eyatrooz.transaction_monitoring.case_management_service.dtos;

import com.eyatrooz.transaction_monitoring.case_management_service.enums.CaseStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class CaseCreatedPayload {
    private Long id;
    private Long transactionId;
    private String accountId;
    private BigDecimal riskScore;
    private CaseStatus status;
    private Instant createdAt;
}
