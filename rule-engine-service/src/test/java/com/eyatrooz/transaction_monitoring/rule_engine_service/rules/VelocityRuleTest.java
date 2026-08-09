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

public class VelocityRuleTest {

    @Test
    void triggers_WhenTransactionsCountMeetLimit(){

        var rule = new VelocityRule();
        ReflectionTestUtils.setField(rule, "windowMinutes", 10L);
        ReflectionTestUtils.setField(rule, "maxCount", 5);
        ReflectionTestUtils.setField(rule, "ruleName", "VELOCITY");

        var transaction = TransactionHistory.builder()
                .transactionId(1L)
                .accountId("ACC-1")
                .amount(BigDecimal.valueOf(150))
                .transactionType(TransactionType.TRANSFER)
                .createdAt(Instant.now())
                .receivedAt(Instant.now())
                .build();

        List<TransactionHistory> recentTransactions = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            recentTransactions.add(
                    TransactionHistory.builder()
                            .transactionId((long) (i + 2))
                            .accountId("ACC-1")
                            .amount(BigDecimal.valueOf(200))
                            .transactionType(TransactionType.TRANSFER)
                            .createdAt(Instant.now().minus(1, ChronoUnit.MINUTES))
                            .receivedAt(Instant.now())
                            .build());
        }

        var context = RuleContext.builder()
                .transaction(transaction).recentHistory(recentTransactions).build();

        var result = rule.evaluate(context);

        assertTrue(result.triggered());
        assertEquals("VELOCITY", result.ruleName());
        assertEquals(0, BigDecimal.valueOf(5).compareTo(result.riskScore()));
    }

    @Test
    void doesNotTrigger_whenOldTransactionsFallOutsideWindow() {

        var rule = new VelocityRule();
        ReflectionTestUtils.setField(rule, "windowMinutes", 10L);
        ReflectionTestUtils.setField(rule, "maxCount", 5);
        ReflectionTestUtils.setField(rule, "ruleName", "VELOCITY");

        var transaction = TransactionHistory.builder()
                .transactionId(1L)
                .accountId("ACC-1")
                .amount(BigDecimal.valueOf(150))
                .transactionType(TransactionType.TRANSFER)
                .createdAt(Instant.now())
                .receivedAt(Instant.now())
                .build();

        List<TransactionHistory> recentTransactions = new ArrayList<>();

        // 3 transactions INSIDE the 10-minute window (should count)
        for (int i = 0; i < 3; i++) {
            recentTransactions.add(
                    TransactionHistory.builder()
                            .transactionId((long) (i + 2))
                            .accountId("ACC-1")
                            .amount(BigDecimal.valueOf(200))
                            .transactionType(TransactionType.TRANSFER)
                            .createdAt(Instant.now().minus(2, ChronoUnit.MINUTES))
                            .receivedAt(Instant.now())
                            .build());
        }

        // 3 transactions OUTSIDE the window (should be excluded)
        for (int i = 0; i < 3; i++) {
            recentTransactions.add(
                    TransactionHistory.builder()
                            .transactionId((long) (i + 5))
                            .accountId("ACC-1")
                            .amount(BigDecimal.valueOf(200))
                            .transactionType(TransactionType.TRANSFER)
                            .createdAt(Instant.now().minus(15, ChronoUnit.MINUTES))
                            .receivedAt(Instant.now())
                            .build());
        }

        var context = RuleContext.builder()
                .transaction(transaction).recentHistory(recentTransactions).build();

        var result = rule.evaluate(context);

        assertFalse(result.triggered());
        assertEquals("VELOCITY", result.ruleName());
        assertEquals(0, BigDecimal.ZERO.compareTo(result.riskScore()));
    }


}
