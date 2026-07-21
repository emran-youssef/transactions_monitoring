package com.eyatrooz.transaction_monitoring.rule_engine_service.rules;

import com.eyatrooz.transaction_monitoring.rule_engine_service.entities.TransactionHistory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class StructuringRule implements FraudRule {

    @Value("${rules.structuring.window-minute}")
    private long windowMinutes;

    @Value("${rules.structuring.threshold}")
    private BigDecimal threshold;

    @Value("${rules.structuring.min-transactions}")
    private int minTransactions;

    @Value("${rules.structuring.name}")
    private String ruleName;

    @Override
    public RuleResult evaluate(RuleContext context) {
        Instant windowStart = context.transaction().getCreatedAt()
                .minus(windowMinutes, ChronoUnit.MINUTES);

        var qualifying = context.recentHistory().stream()
                .filter(tx -> !tx.getCreatedAt().isBefore(windowStart))
                .filter(tx -> tx.getAmount().compareTo(threshold) < 0)
                .toList();

        if(qualifying.size() < minTransactions)
            return RuleResult.notTriggered(ruleName);

        var sum = qualifying.stream()
                .map(TransactionHistory::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if(sum.compareTo(threshold) > 0) {
            String details = "%d sub-threshold transactions totaling %s exceed threshold %s within %d minutes"
                    .formatted(qualifying.size(), sum, threshold, windowMinutes);
            return new RuleResult(ruleName, true, sum, details);
        }

        return RuleResult.notTriggered(ruleName);
    }
}
