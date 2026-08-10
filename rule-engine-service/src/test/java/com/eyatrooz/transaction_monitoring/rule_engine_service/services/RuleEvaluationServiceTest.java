package com.eyatrooz.transaction_monitoring.rule_engine_service.services;

import com.eyatrooz.transaction_monitoring.rule_engine_service.entities.RuleEvaluationResult;
import com.eyatrooz.transaction_monitoring.rule_engine_service.entities.TransactionHistory;
import com.eyatrooz.transaction_monitoring.rule_engine_service.enums.TransactionType;
import com.eyatrooz.transaction_monitoring.rule_engine_service.kafka.helpers.OutboxEventFactory;
import com.eyatrooz.transaction_monitoring.rule_engine_service.mappers.RuleEvaluationMapper;
import com.eyatrooz.transaction_monitoring.rule_engine_service.repositories.OutboxEventsRepository;
import com.eyatrooz.transaction_monitoring.rule_engine_service.repositories.RuleEvaluationRepository;
import com.eyatrooz.transaction_monitoring.rule_engine_service.repositories.TransactionHistoryRepository;
import com.eyatrooz.transaction_monitoring.rule_engine_service.rules.RuleExecutor;
import com.eyatrooz.transaction_monitoring.rule_engine_service.rules.RuleExecutorResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RuleEvaluationServiceTest {

    @Mock
    private RuleExecutor ruleExecutor;

    @Mock
    private  RuleEvaluationMapper ruleEvaluationMapper;

    @Mock
    private  OutboxEventsRepository outboxEventsRepository;

    @Mock
    private  RuleEvaluationRepository ruleEvaluationRepository;

    @Mock
    private  TransactionHistoryRepository transactionHistoryRepository;

    @Mock
    private  OutboxEventFactory outboxEventFactory;

    @InjectMocks
    private RuleEvaluationService ruleEvaluationService;


    @Test
    void evaluate_savesAndPublishesOutbox_whenFlagged(){

        // ARRANGE — the transaction being evaluated
        var transaction = TransactionHistory.builder()
                .transactionId(1L)
                .accountId("ACC-1")
                .amount(BigDecimal.valueOf(15000))
                .transactionType(TransactionType.DEPOSIT)
                .createdAt(Instant.now())
                .receivedAt(Instant.now())
                .build();

        // ARRANGE — fake result the (mocked) RuleExecutor will return: simulates a flagged transaction
        var fakeResult = new RuleExecutorResult(true, BigDecimal.valueOf(5000), List.of());

        // ARRANGE — script the mocked dependencies
        when(transactionHistoryRepository.findByAccountIdAndCreatedAtAfter(any(), any()))
                .thenReturn(List.of());

        when(ruleExecutor.execute(any()))
                .thenReturn(fakeResult);

        // ACT
        ruleEvaluationService.evaluate(transaction);

        // VERIFY — the evaluation was persisted
        verify(ruleEvaluationRepository).save(any());

        // VERIFY — since flagged=true, the outbox event pipeline ran too
        verify(ruleEvaluationMapper).toFlaggedPayload(any());
        verify(outboxEventFactory).create(any(), any(), any(), any());
        verify(outboxEventsRepository).save(any());
    }

}