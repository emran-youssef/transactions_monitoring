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
    void assignMovesOpenCaseUnderReviewAndRecordsUpdateOutboxEvent() {
        var caseEntity = caseEntity(CaseStatus.OPEN, null);
        var casePayload = new CasePayload();
        var caseResponse = new CaseResponse();
        var outboxEvent = OutboxEvent.from(AggregateType.CASE_UPDATE, "7", KafkaTopics.CASE_UPDATED, "{}");

        when(caseRepository.findById(7L)).thenReturn(Optional.of(caseEntity));
        when(caseRepository.save(caseEntity)).thenReturn(caseEntity);
        when(caseMapper.toCasePayload(caseEntity)).thenReturn(casePayload);
        when(caseMapper.toResponse(caseEntity)).thenReturn(caseResponse);
        when(outboxEventFactory.create(AggregateType.CASE_UPDATE, "7", KafkaTopics.CASE_UPDATED, casePayload))
                .thenReturn(outboxEvent);

        var response = caseWorkflowService.assign(7L, "analyst.one");

        assertThat(response).isSameAs(caseResponse);
        assertThat(caseEntity.getStatus()).isEqualTo(CaseStatus.UNDER_REVIEW);
        assertThat(caseEntity.getAssignedAnalyst()).isEqualTo("analyst.one");
        assertThat(caseEntity.getHistory())
                .singleElement()
                .satisfies(history -> {
                    assertThat(history.getEventType()).isEqualTo(CaseHistoryEventType.CASE_ASSIGNED);
                    assertThat(history.getAnalyst()).isEqualTo("analyst.one");
                    assertThat(history.getCaseEntity()).isSameAs(caseEntity);
                });
        verify(outboxEventRepository).save(outboxEvent);
    }

    @Test
    void approveMovesAssignedCaseToApprovedAndRecordsUpdateOutboxEvent() {
        var caseEntity = caseEntity(CaseStatus.UNDER_REVIEW, "analyst.one");
        var casePayload = new CasePayload();
        var caseResponse = new CaseResponse();
        var outboxEvent = OutboxEvent.from(AggregateType.CASE_UPDATE, "7", KafkaTopics.CASE_UPDATED, "{}");

        when(caseRepository.findById(7L)).thenReturn(Optional.of(caseEntity));
        when(caseRepository.save(caseEntity)).thenReturn(caseEntity);
        when(caseMapper.toCasePayload(caseEntity)).thenReturn(casePayload);
        when(caseMapper.toResponse(caseEntity)).thenReturn(caseResponse);
        when(outboxEventFactory.create(AggregateType.CASE_UPDATE, "7", KafkaTopics.CASE_UPDATED, casePayload))
                .thenReturn(outboxEvent);

        var response = caseWorkflowService.approve(7L, "analyst.one", "valid activity");

        assertThat(response).isSameAs(caseResponse);
        assertThat(caseEntity.getStatus()).isEqualTo(CaseStatus.APPROVED);

        var caseCaptor = ArgumentCaptor.forClass(Case.class);
        verify(caseRepository).save(caseCaptor.capture());
        assertThat(caseCaptor.getValue().getHistory())
                .singleElement()
                .satisfies(history -> {
                    assertThat(history.getEventType()).isEqualTo(CaseHistoryEventType.CASE_APPROVED);
                    assertThat(history.getAnalyst()).isEqualTo("analyst.one");
                    assertThat(history.getComment()).isEqualTo("valid activity");
                });
        verify(outboxEventRepository).save(outboxEvent);
    }

    @Test
    void assignRejectsCaseThatIsNotOpen() {
        var caseEntity = caseEntity(CaseStatus.UNDER_REVIEW, "analyst.one");
        when(caseRepository.findById(7L)).thenReturn(Optional.of(caseEntity));

        assertThatThrownBy(() -> caseWorkflowService.assign(7L, "analyst.two"))
                .isInstanceOf(IllegalCaseTransitionException.class);

        verify(caseRepository, never()).save(any());
        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    void approveRejectsAnalystThatDoesNotOwnCase() {
        var caseEntity = caseEntity(CaseStatus.UNDER_REVIEW, "analyst.one");
        when(caseRepository.findById(7L)).thenReturn(Optional.of(caseEntity));

        assertThatThrownBy(() -> caseWorkflowService.approve(7L, "analyst.two", "valid activity"))
                .isInstanceOf(CaseNotAssignedException.class);

        verify(caseRepository, never()).save(any());
        verify(outboxEventRepository, never()).save(any());
    }

    private Case caseEntity(CaseStatus status, String assignedAnalyst) {
        return Case.builder()
                .id(7L)
                .transactionId(1001L)
                .accountId("ACC-100")
                .riskScore(new BigDecimal("91.50"))
                .status(status)
                .assignedAnalyst(assignedAnalyst)
                .build();
    }
}
