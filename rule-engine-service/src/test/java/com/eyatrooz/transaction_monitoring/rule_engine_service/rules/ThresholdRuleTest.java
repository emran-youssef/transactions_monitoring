package com.eyatrooz.transaction_monitoring.rule_engine_service.rules;


import com.eyatrooz.transaction_monitoring.rule_engine_service.entities.TransactionHistory;
import com.eyatrooz.transaction_monitoring.rule_engine_service.enums.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ThresholdRuleTest {

    @Test
    void triggers_whenAmountExceedsThreshold(){

        // ARRANGE
        var rule = new ThresholdRule();
        ReflectionTestUtils.setField(rule, "threshold", BigDecimal.valueOf(10000));
        ReflectionTestUtils.setField(rule, "ruleName", "THRESHOLD");

        var transaction = TransactionHistory.builder()
                .transactionId(1L)
                .accountId("ACC-1")
                .amount(BigDecimal.valueOf(15000))
                .transactionType(TransactionType.DEPOSIT)
                .createdAt(Instant.now())
                .receivedAt(Instant.now())
                .build();

        var context = RuleContext.builder()
                .transaction(transaction)
                .recentHistory(List.of())
                .build();

        // ACT
        RuleResult result = rule.evaluate(context);

        // ASSERT : What we expect
        assertTrue(result.triggered());
        assertEquals("THRESHOLD", result.ruleName());
        assertEquals(0, BigDecimal.valueOf(5000).compareTo(result.riskScore()));

    }

    @Test
    void doesNotTrigger_whenAmountEqualThreshold(){

        var rule = new ThresholdRule();
        ReflectionTestUtils.setField(rule, "threshold", BigDecimal.valueOf(10000));
        ReflectionTestUtils.setField(rule, "ruleName", "THRESHOLD");

        var transaction = TransactionHistory.builder()
                .transactionId(1L)
                .accountId("ACC-1")
                .amount(BigDecimal.valueOf(10000))
                .transactionType(TransactionType.TRANSFER)
                .createdAt(Instant.now())
                .receivedAt(Instant.now())
                .build();

        var context = RuleContext.builder()
                .transaction(transaction)
                .recentHistory(List.of())
                .build();

        RuleResult result = rule.evaluate(context);

        assertFalse(result.triggered());
        assertEquals("THRESHOLD", result.ruleName());
        assertEquals(0, BigDecimal.valueOf(0).compareTo(result.riskScore()));

    }

    @Test
    void doesNotTrigger_whenAmountBelowThreshold(){

        var rule = new ThresholdRule();
        ReflectionTestUtils.setField(rule, "threshold", BigDecimal.valueOf(10000));
        ReflectionTestUtils.setField(rule, "ruleName", "THRESHOLD");

        var transaction = TransactionHistory.builder()
                .transactionId(1L)
                .accountId("ACC-1")
                .amount(BigDecimal.valueOf(9999))
                .transactionType(TransactionType.TRANSFER)
                .createdAt(Instant.now())
                .receivedAt(Instant.now())
                .build();

        var context = RuleContext.builder()
                .transaction(transaction)
                .recentHistory(List.of())
                .build();

        RuleResult result = rule.evaluate(context);

        assertFalse(result.triggered());
        assertEquals("THRESHOLD", result.ruleName());
        assertEquals(0, BigDecimal.valueOf(0).compareTo(result.riskScore()));


    }
}
