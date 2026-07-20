package com.eyatrooz.transaction_monitoring.rule_engine_service.rules;


public interface FraudRule {
    RuleResult evaluate(RuleContext context);
}
