package com.eyatrooz.transaction_monitoring.transaction_service.dtos;

import com.eyatrooz.transaction_monitoring.transaction_service.enums.TransactionStatus;
import com.eyatrooz.transaction_monitoring.transaction_service.enums.TransactionType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class TransactionResponse {

    private Long id;
    private String accountId;
    private BigDecimal amount;
    private String currency;
    private TransactionType transactionType;
    private String counterparty;
    private TransactionStatus transactionStatus;
    private Instant createdAt;
    private Instant receivedAt;

}
