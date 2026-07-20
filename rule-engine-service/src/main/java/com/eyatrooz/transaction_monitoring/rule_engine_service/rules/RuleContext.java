package com.eyatrooz.transaction_monitoring.rule_engine_service.rules;

import com.eyatrooz.transaction_monitoring.rule_engine_service.entities.TransactionHistory;

import java.util.List;

public record RuleContext(
        TransactionHistory transaction,
        List<TransactionHistory> recentHistory

) { }
