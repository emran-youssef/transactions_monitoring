package com.eyatrooz.transaction_monitoring.rule_engine_service.rules;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RuleExecutor {

    private final List<FraudRule> rules;

    public RuleExecutorResult execute(RuleContext context){
        List<RuleResult> results = rules.stream()
                .map(rule-> rule.evaluate(context))
                .toList();

        BigDecimal totalScore = results.stream()
                .filter(result -> result.triggered())
                .map(result -> result.riskScore())
                .reduce(BigDecimal.ZERO, (acc, score) -> acc.add(score));    // (acc) start on 0 in this case

        boolean flagged = results.stream().anyMatch(result-> result.triggered());

        return new RuleExecutorResult(flagged, totalScore, results);


    }



}
