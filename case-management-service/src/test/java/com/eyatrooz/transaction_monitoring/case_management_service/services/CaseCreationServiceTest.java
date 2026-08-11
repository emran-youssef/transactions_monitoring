package com.eyatrooz.transaction_monitoring.case_management_service.services;

import com.eyatrooz.transaction_monitoring.case_management_service.dtos.CasePayload;
import com.eyatrooz.transaction_monitoring.case_management_service.dtos.TransactionFlaggedPayload;
import com.eyatrooz.transaction_monitoring.case_management_service.entities.Case;
import com.eyatrooz.transaction_monitoring.case_management_service.entities.OutboxEvent;
import com.eyatrooz.transaction_monitoring.case_management_service.enums.CaseStatus;
import com.eyatrooz.transaction_monitoring.case_management_service.kafka.OutboxEventFactory;
import com.eyatrooz.transaction_monitoring.case_management_service.mappers.CaseMapper;
import com.eyatrooz.transaction_monitoring.case_management_service.repositories.CaseRepository;
import com.eyatrooz.transaction_monitoring.case_management_service.repositories.FlaggedTransactionEventRepository;
import com.eyatrooz.transaction_monitoring.case_management_service.repositories.OutboxEventsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class CaseCreationServiceTest {

    @Mock
    private CaseMapper caseMapper;

    @Mock
    private CaseRepository caseRepository;

    @Mock
    private OutboxEventFactory outboxEventFactory;

    @Mock
    private OutboxEventsRepository outboxEventRepository;

    @Mock
    private FlaggedTransactionEventRepository flaggedTransactionEventRepository;

    @InjectMocks
    private CaseCreationService caseCreationService;

    @Test
    void processFlaggedTransaction_createsCaseAndOutBox_whenNew(){

        var transaction = new TransactionFlaggedPayload();
        transaction.setTransactionId(1001L);
        transaction.setAccountId("ACC-100");
        transaction.setFlagged(true);
        transaction.setRiskScore(BigDecimal.valueOf(9150));
        transaction.setEvaluatedAt(Instant.now());

        when(flaggedTransactionEventRepository.existsByTransactionId(any()))
                .thenReturn(false);

        when(caseRepository.existsByTransactionId(any()))
                .thenReturn(false);

        when(caseRepository.save(any()))
                .thenReturn(Case.builder().id(1L).transactionId(1001L).accountId("ACC-100").status(CaseStatus.OPEN).build());

        when(caseMapper.toCasePayload(any()))
                .thenReturn(new CasePayload());

        when(outboxEventFactory.create(any(), any(), any(), any()))
                .thenReturn(new OutboxEvent());

        // ACT
        caseCreationService.processFlaggedTransaction(transaction);

        // VERIFY — flagged event was recorded (since it didn't exist yet)
        verify(flaggedTransactionEventRepository).save(any());

        // VERIFY — the new case was persisted
        verify(caseRepository).save(any());

        // VERIFY — outbox pipeline ran
        verify(caseMapper).toCasePayload(any());
        verify(outboxEventFactory).create(any(), any(), any(), any());
        verify(outboxEventRepository).save(any());
    }

    @Test
    void processFlaggedTransaction_skipsCaseCreation_whenCaseAlreadyExists(){

        // ARRANGE
        var transaction = new TransactionFlaggedPayload();
        transaction.setTransactionId(1001L);
        transaction.setAccountId("ACC-100");
        transaction.setFlagged(true);
        transaction.setRiskScore(BigDecimal.valueOf(9150));
        transaction.setEvaluatedAt(Instant.now());

        when(flaggedTransactionEventRepository.existsByTransactionId(any()))
                .thenReturn(true);

        when(caseRepository.existsByTransactionId(any()))
                .thenReturn(true);

        // ACT
        caseCreationService.processFlaggedTransaction(transaction);

        // VERIFY
        verify(flaggedTransactionEventRepository, never()).save(any());
        verify(caseRepository, never()).save(any());
        verify(caseMapper, never()).toCasePayload(any());
        verify(outboxEventFactory, never()).create(any(), any(), any(), any());
        verify(outboxEventRepository, never()).save(any());


    }

}


