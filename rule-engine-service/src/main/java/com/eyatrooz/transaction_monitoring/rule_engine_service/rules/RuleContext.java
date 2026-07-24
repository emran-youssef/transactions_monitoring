package com.eyatrooz.transaction_monitoring.rule_engine_service.rules;

import com.eyatrooz.transaction_monitoring.rule_engine_service.entities.TransactionHistory;
import lombok.Builder;

import java.util.List;

@Builder
public record RuleContext(
        TransactionHistory transaction,
        List<TransactionHistory> recentHistory

) { }
