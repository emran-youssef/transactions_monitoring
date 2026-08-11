package com.eyatrooz.transaction_monitoring.case_management_service.services;

import com.eyatrooz.transaction_monitoring.case_management_service.dtos.CasePayload;
import com.eyatrooz.transaction_monitoring.case_management_service.dtos.CaseResponse;
import com.eyatrooz.transaction_monitoring.case_management_service.entities.Case;
import com.eyatrooz.transaction_monitoring.case_management_service.entities.OutboxEvent;
import com.eyatrooz.transaction_monitoring.case_management_service.enums.CaseHistoryEventType;
import com.eyatrooz.transaction_monitoring.case_management_service.enums.CaseStatus;
import com.eyatrooz.transaction_monitoring.case_management_service.exceptions.CaseNotAssignedException;
import com.eyatrooz.transaction_monitoring.case_management_service.exceptions.IllegalCaseTransitionException;
import com.eyatrooz.transaction_monitoring.case_management_service.kafka.AggregateType;
import com.eyatrooz.transaction_monitoring.case_management_service.kafka.KafkaTopics;
import com.eyatrooz.transaction_monitoring.case_management_service.kafka.OutboxEventFactory;
import com.eyatrooz.transaction_monitoring.case_management_service.mappers.CaseHistoryMapper;
import com.eyatrooz.transaction_monitoring.case_management_service.mappers.CaseMapper;
import com.eyatrooz.transaction_monitoring.case_management_service.repositories.CaseRepository;
import com.eyatrooz.transaction_monitoring.case_management_service.repositories.OutboxEventsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseWorkflowServiceTest {

    @Mock
    private CaseMapper caseMapper;

    @Mock
    private CaseRepository caseRepository;

    @Mock
    private CaseHistoryMapper caseHistoryMapper;

    @Mock
    private OutboxEventFactory outboxEventFactory;

    @Mock
    private OutboxEventsRepository outboxEventRepository;

    @InjectMocks
    private CaseWorkflowService caseWorkflowService;

    @Test
    void assign_movesToUnderReview_whenCaseIsOpen() {

        var existingCase = Case.builder()
                .id(1L)
                .transactionId(1001L)
                .accountId("ACC-100")
                .status(CaseStatus.OPEN)
                .build();

        when(caseRepository.findById(1L)).thenReturn(Optional.of(existingCase));

        when(caseRepository.save(any())).thenReturn(existingCase);

        when(caseMapper.toCasePayload(any())).thenReturn(new CasePayload());

        when(outboxEventFactory.create(any(), any(), any(), any())).thenReturn(new OutboxEvent());

        var fakeResponse = new CaseResponse();
        fakeResponse.setId(1L);
        fakeResponse.setStatus(CaseStatus.UNDER_REVIEW);
        fakeResponse.setAssignedAnalyst("analyst1");

        when(caseMapper.toResponse(any())).thenReturn(fakeResponse);

        // ACT
        var result = caseWorkflowService.assign(1L, "analyst1");

        // ASSERT — the returned response reflects the new state
        assertEquals(CaseStatus.UNDER_REVIEW, result.getStatus());
        assertEquals("analyst1", result.getAssignedAnalyst());

        // ASSERT — the in-memory case object was actually mutated correctly before saving
        assertEquals(CaseStatus.UNDER_REVIEW, existingCase.getStatus());
        assertEquals("analyst1", existingCase.getAssignedAnalyst());

        // VERIFY — the save and outbox pipeline ran
        verify(caseRepository).save(existingCase);
        verify(outboxEventRepository).save(any());

    }

    @Test
    void approve_movesToApproved_whenUnderReviewAndAssignedToCorrectAnalyst() {

        var existingCase = Case.builder()
                .id(1L)
                .transactionId(1001L)
                .accountId("ACC-100")
                .status(CaseStatus.UNDER_REVIEW)
                .assignedAnalyst("analyst1")
                .build();

        when(caseRepository.findById(1L))
                .thenReturn(Optional.of(existingCase));

        when(caseRepository.save(any()))
                .thenReturn(existingCase);

        when(caseMapper.toCasePayload(any()))
                .thenReturn(new CasePayload());

        when(outboxEventFactory.create(any(), any(), any(), any()))
                .thenReturn(new OutboxEvent());

        var fakeResponse = new CaseResponse();
        fakeResponse.setStatus(CaseStatus.APPROVED);

        when(caseMapper.toResponse(any()))
                .thenReturn(fakeResponse);

        var result = caseWorkflowService.approve(1L, "analyst1", "Looks legitimate");

        assertEquals(CaseStatus.APPROVED, result.getStatus());
        assertEquals(CaseStatus.APPROVED, existingCase.getStatus());
        verify(caseRepository).save(existingCase);
        verify(outboxEventRepository).save(any());
    }

    @Test
    void approve_throwsIllegalCaseTransition_whenCaseNotUnderReview() {

        var existingCase = Case.builder()
                .id(1L)
                .status(CaseStatus.OPEN)   // wrong status — not yet assigned/under review
                .assignedAnalyst(null)
                .build();

        when(caseRepository.findById(1L))
                .thenReturn(Optional.of(existingCase));

        assertThrows(IllegalCaseTransitionException.class,
                () -> caseWorkflowService.approve(1L, "analyst1", "Looks legitimate"));

        // nothing should have been saved or published
        verify(caseRepository, never()).save(any());
        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    void approve_throwsCaseNotAssigned_whenDifferentAnalystTries() {

        var existingCase = Case.builder()
                .id(1L)
                .status(CaseStatus.UNDER_REVIEW)
                .assignedAnalyst("analyst1")   // assigned to analyst1
                .build();

        when(caseRepository.findById(1L))
                .thenReturn(Optional.of(existingCase));

        // analyst2 tries to approve
        assertThrows(CaseNotAssignedException.class,
                () -> caseWorkflowService.approve(1L, "analyst2", "Looks legitimate"));

        verify(caseRepository, never()).save(any());
        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    void escalate_movesToEscalated_whenUnderReviewAndAssignedToCorrectAnalyst() {

        var existingCase = Case.builder()
                .id(1L)
                .transactionId(1001L)
                .accountId("ACC-100")
                .status(CaseStatus.UNDER_REVIEW)
                .assignedAnalyst("analyst1")
                .build();

        when(caseRepository.findById(1L))
                .thenReturn(Optional.of(existingCase));

        when(caseRepository.save(any()))
                .thenReturn(existingCase);

        when(caseMapper.toCasePayload(any()))
                .thenReturn(new CasePayload());

        when(outboxEventFactory.create(any(), any(), any(), any()))
                .thenReturn(new OutboxEvent());

        var fakeResponse = new CaseResponse();
        fakeResponse.setStatus(CaseStatus.ESCALATED);

        when(caseMapper.toResponse(any()))
                .thenReturn(fakeResponse);

        var result = caseWorkflowService.escalate(1L, "analyst1", "Suspicious pattern confirmed");

        assertEquals(CaseStatus.ESCALATED, result.getStatus());
        assertEquals(CaseStatus.ESCALATED, existingCase.getStatus());
        verify(caseRepository).save(existingCase);
        verify(outboxEventRepository).save(any());
    }

    @Test
    void escalate_throwsIllegalCaseTransition_whenCaseNotUnderReview() {

        var existingCase = Case.builder()
                .id(1L)
                .status(CaseStatus.OPEN)
                .assignedAnalyst(null)
                .build();

        when(caseRepository.findById(1L))
                .thenReturn(Optional.of(existingCase));

        assertThrows(IllegalCaseTransitionException.class,
                () -> caseWorkflowService.escalate(1L, "analyst1", "Suspicious pattern confirmed"));

        verify(caseRepository, never()).save(any());
        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    void escalate_throwsCaseNotAssigned_whenDifferentAnalystTries() {
        var existingCase = Case.builder()
                .id(1L)
                .status(CaseStatus.UNDER_REVIEW)
                .assignedAnalyst("analyst1")
                .build();

        when(caseRepository.findById(1L))
                .thenReturn(Optional.of(existingCase));

        assertThrows(CaseNotAssignedException.class,
                () -> caseWorkflowService.escalate(1L, "analyst2", "Suspicious pattern confirmed"));

        verify(caseRepository, never()).save(any());
        verify(outboxEventRepository, never()).save(any());
    }

}
