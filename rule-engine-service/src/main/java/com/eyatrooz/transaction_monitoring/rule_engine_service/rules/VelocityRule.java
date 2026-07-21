package com.eyatrooz.transaction_monitoring.rule_engine_service.rules;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;


@Slf4j
@Component
public class VelocityRule implements FraudRule {

    @Value("${rules.velocity.widows-minutes:10}")
    private long windowMinutes;

    @Value("${rules.velocity.count}")
    private int maxCount;

    @Value("${rules.velocity.name}")
    private String ruleName;


    @Override
    public RuleResult evaluate(RuleContext context) {

        // 1. Determine when the lookback window starts (transaction time minus the window length)
        Instant windowStart = context.transaction().getCreatedAt()
                .minus(windowMinutes, ChronoUnit.MINUTES);

        // 2. Count how many transactions (including this one) fall inside that window
        long countInWindow = context.recentHistory()
                .stream().filter(tx -> !tx.getCreatedAt().isBefore(windowStart)).count();

        if (countInWindow >= maxCount) {
            String details = "%d transactions in the last %d minutes meets/exceeds limit of %d"
                    .formatted(countInWindow, windowMinutes, maxCount);

            log.info("VelocityRule triggered: accountId={}, countInWindow={}, windowMinutes={}, maxCount={}",
                    context.transaction().getAccountId(), countInWindow, windowMinutes, maxCount);

            return new RuleResult(ruleName, true, BigDecimal.valueOf(countInWindow), details);
        }

        return RuleResult.notTriggered(ruleName);
    }
}
