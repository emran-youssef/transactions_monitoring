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
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertThrows;
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

    private TransactionFlaggedPayload newFlaggedPayload() {
        var transaction = new TransactionFlaggedPayload();
        transaction.setTransactionId(1001L);
        transaction.setAccountId("ACC-100");
        transaction.setFlagged(true);
        transaction.setRiskScore(BigDecimal.valueOf(9150));
        transaction.setEvaluatedAt(Instant.now());
        return transaction;
    }

    @Test
    void processFlaggedTransaction_createsCaseAndOutBox_whenNew(){

        var transaction = newFlaggedPayload();

        when(caseRepository.save(any()))
                .thenReturn(Case.builder().id(1L).transactionId(1001L).accountId("ACC-100").status(CaseStatus.OPEN).build());

        when(caseMapper.toCasePayload(any()))
                .thenReturn(new CasePayload());

        when(outboxEventFactory.create(any(), any(), any(), any()))
                .thenReturn(new OutboxEvent());

        // ACT
        caseCreationService.processFlaggedTransaction(transaction);

        // VERIFY — flagged event was persisted
        verify(flaggedTransactionEventRepository).save(any());

        // VERIFY — the new case was persisted
        verify(caseRepository).save(any());

        // VERIFY — outbox pipeline ran
        verify(caseMapper).toCasePayload(any());
        verify(outboxEventFactory).create(any(), any(), any(), any());
        verify(outboxEventRepository).save(any());
    }

    @Test
    void processFlaggedTransaction_throwsAndSkipsRestOfWork_whenFlaggedEventAlreadyExists(){

        // ARRANGE — unique constraint on flagged_transaction_events.transaction_id rejects the duplicate insert
        var transaction = newFlaggedPayload();

        when(flaggedTransactionEventRepository.save(any()))
                .thenThrow(new DataIntegrityViolationException("duplicate transaction_id"));

        // ACT + VERIFY — the whole @Transactional method aborts, propagating the violation to the caller
        assertThrows(DataIntegrityViolationException.class,
                () -> caseCreationService.processFlaggedTransaction(transaction));

        // VERIFY — nothing past the failed save runs
        verify(caseRepository, never()).save(any());
        verify(caseMapper, never()).toCasePayload(any());
        verify(outboxEventFactory, never()).create(any(), any(), any(), any());
        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    void processFlaggedTransaction_throwsAndSkipsOutbox_whenCaseAlreadyExists(){

        // ARRANGE — flagged event save succeeds, but the case's unique constraint on transaction_id rejects the duplicate
        var transaction = newFlaggedPayload();

        when(caseRepository.save(any()))
                .thenThrow(new DataIntegrityViolationException("duplicate transaction_id"));

        // ACT + VERIFY
        assertThrows(DataIntegrityViolationException.class,
                () -> caseCreationService.processFlaggedTransaction(transaction));

        // VERIFY — flagged event save was still attempted...
        verify(flaggedTransactionEventRepository).save(any());
        // ...but nothing past the failed case save runs
        verify(caseMapper, never()).toCasePayload(any());
        verify(outboxEventFactory, never()).create(any(), any(), any(), any());
        verify(outboxEventRepository, never()).save(any());
    }

}
