package com.eyatrooz.transaction_monitoring.rule_engine_service.dtos;

import com.eyatrooz.transaction_monitoring.rule_engine_service.enums.TransactionType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class TransactionCreatedPayload {
    private Long id;
    private String accountId;
    private BigDecimal amount;
    private String currency;
    private TransactionType transactionType;
    private String counterparty;
    private String transactionStatus;
    private Instant createdAt;
    private Instant receivedAt;
}
