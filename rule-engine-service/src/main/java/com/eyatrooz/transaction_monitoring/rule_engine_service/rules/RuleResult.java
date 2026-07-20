package com.eyatrooz.transaction_monitoring.rule_engine_service.rules;

import java.math.BigDecimal;

public record RuleResult(
        String ruleName,
        boolean triggered,
        BigDecimal riskScore,
        String details
) {
    public static RuleResult notTriggered(String ruleName){
        return new RuleResult(ruleName, false, BigDecimal.ZERO, null);
    }
}
