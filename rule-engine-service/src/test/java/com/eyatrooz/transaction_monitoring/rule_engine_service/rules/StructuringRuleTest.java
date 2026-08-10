package com.eyatrooz.transaction_monitoring.rule_engine_service.rules;

import com.eyatrooz.transaction_monitoring.rule_engine_service.entities.TransactionHistory;
import com.eyatrooz.transaction_monitoring.rule_engine_service.enums.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class StructuringRuleTest {

    @Test
    void triggers_whenSubThresholdTransactionsSumExceedsThreshold(){

        var rule = new StructuringRule();

        ReflectionTestUtils.setField(rule, "windowMinutes", 60L);
        ReflectionTestUtils.setField(rule, "threshold", BigDecimal.valueOf(1000));
        ReflectionTestUtils.setField(rule, "minTransactions", 3);
        ReflectionTestUtils.setField(rule, "ruleName", "STRUCTURING");

        var transaction = TransactionHistory.builder()
                .transactionId(1L)
                .accountId("ACC-1")
                .amount(BigDecimal.valueOf(200))
                .transactionType(TransactionType.DEPOSIT)
                .createdAt(Instant.now())
                .receivedAt(Instant.now())
                .build();

        List<TransactionHistory> recentTransactions = new ArrayList<>();

        for(int i = 0; i <= 2; i++){
            recentTransactions.add(
                    TransactionHistory.builder()
                            .transactionId((long) (i + 2))
                            .accountId("ACC-1")
                            .amount(BigDecimal.valueOf(350))
                            .transactionType(TransactionType.TRANSFER)
                            .createdAt(Instant.now().minus(50, ChronoUnit.MINUTES))
                            .receivedAt(Instant.now())
                            .build());
        }

        var context = RuleContext.builder()
                .recentHistory(recentTransactions).transaction(transaction).build();

        var result = rule.evaluate(context);

        assertTrue(result.triggered());
        assertEquals("STRUCTURING", result.ruleName());
        assertEquals(0, BigDecimal.valueOf(1050).compareTo(result.riskScore()));

    }


    @Test
    void doesNotTrigger_whenSumStaysBelowThreshold() {

        var rule = new StructuringRule();

        ReflectionTestUtils.setField(rule, "windowMinutes", 60L);
        ReflectionTestUtils.setField(rule, "threshold", BigDecimal.valueOf(1000));
        ReflectionTestUtils.setField(rule, "minTransactions", 3);
        ReflectionTestUtils.setField(rule, "ruleName", "STRUCTURING");

        var transaction = TransactionHistory.builder()
                .transactionId(1L)
                .accountId("ACC-1")
                .amount(BigDecimal.valueOf(200))
                .transactionType(TransactionType.DEPOSIT)
                .createdAt(Instant.now())
                .receivedAt(Instant.now())
                .build();

        List<TransactionHistory> recentTransactions = new ArrayList<>();

        // 3 qualifying transactions (enough to meet minTransactions), but sum stays below threshold
        for (int i = 0; i <= 2; i++) {
            recentTransactions.add(
                    TransactionHistory.builder()
                            .transactionId((long) (i + 2))
                            .accountId("ACC-1")
                            .amount(BigDecimal.valueOf(200))
                            .transactionType(TransactionType.TRANSFER)
                            .createdAt(Instant.now().minus(50, ChronoUnit.MINUTES))
                            .receivedAt(Instant.now())
                            .build());
        }

        var context = RuleContext.builder()
                .recentHistory(recentTransactions).transaction(transaction).build();

        var result = rule.evaluate(context);

        assertFalse(result.triggered());
        assertEquals("STRUCTURING", result.ruleName());
        assertEquals(0, BigDecimal.ZERO.compareTo(result.riskScore()));
    }
}
