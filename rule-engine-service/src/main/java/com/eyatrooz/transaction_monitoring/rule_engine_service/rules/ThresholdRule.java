package com.eyatrooz.transaction_monitoring.rule_engine_service.rules;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
public class ThresholdRule implements FraudRule{

    @Value("${rules.threshold.amount:10000}")
    private BigDecimal threshold;

    @Value("${rules.threshold.name:THRESHOLD}")
    private String ruleName;

    @Override
    public RuleResult evaluate(RuleContext context) {

        var amount = context.transaction().getAmount();
        if(amount.compareTo(threshold) > 0 ) {
             String details = "Transaction amount %s exceeds threshold %s"
                    .formatted(amount, threshold);

            log.info("ThresholdRule triggered: accountId={}, amount={}, threshold={}, excess={}",
                    context.transaction().getAccountId(), amount, threshold, amount.subtract(threshold));

            return new RuleResult(ruleName, true, amount.subtract(threshold),details);
        }
        return  RuleResult.notTriggered(ruleName);
    }
}
