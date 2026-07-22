package com.eyatrooz.transaction_monitoring.rule_engine_service.rules;

import java.math.BigDecimal;
import java.util.List;

public record RuleExecutorResult(
        boolean flagged,
        BigDecimal totalScore,
        List<RuleResult> ruleResults
) { }
