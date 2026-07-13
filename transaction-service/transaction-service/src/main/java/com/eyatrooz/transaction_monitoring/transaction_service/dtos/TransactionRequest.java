package com.eyatrooz.transaction_monitoring.transaction_service.dtos;

import com.eyatrooz.transaction_monitoring.transaction_service.enums.TransactionType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class TransactionRequest {

    @NotBlank
    private String accountId;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false, message = "amount must be greater than zero")
    private BigDecimal amount;

    @NotBlank
    @Size(min = 3, max = 3, message = "currency must be a 3-letter ISO code")
    private String currency;

    @NotNull
    private TransactionType transactionType;

    private String counterparty;  //optional

    @NotNull
    @PastOrPresent
    private Instant createdAt;

}
