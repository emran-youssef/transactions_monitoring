package com.eyatrooz.transaction_monitoring.case_management_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class TransactionFlaggedPayload {
    private Long transactionId;
    private String accountId;
    private Boolean flagged;
    private BigDecimal riskScore;
    private Instant evaluatedAt;
}
