package com.eyatrooz.transaction_monitoring.rule_engine_service.dtos;

import lombok.Data;

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
