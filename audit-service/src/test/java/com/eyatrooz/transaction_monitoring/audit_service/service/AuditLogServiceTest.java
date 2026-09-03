package com.eyatrooz.transaction_monitoring.audit_service.service;

import com.eyatrooz.transaction_monitoring.audit_service.kafka.EventMessage;
import com.eyatrooz.transaction_monitoring.audit_service.repositories.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    private EventMessage<String> newEvent() {
        return EventMessage.<String>builder()
                .eventId("evt-1001")
                .eventType("transactions.created.v1")
                .occurredAt(Instant.now())
                .payload("irrelevant")
                .build();
    }

    @Test
    void record_persistsAuditLogEntry_whenNew() {
        var event = newEvent();

        // ACT
        auditLogService.record(event, "1001", "{\"raw\":\"message\"}");

        // VERIFY — entry was persisted
        verify(auditLogRepository).save(any());
    }

    @Test
    void record_swallowsDuplicate_whenEventIdAlreadyRecorded() {
        var event = newEvent();

        // ARRANGE — unique constraint uq_audit_log_event_id rejects the duplicate insert
        when(auditLogRepository.save(any()))
                .thenThrow(new DataIntegrityViolationException("duplicate eventId"));

        // ACT + VERIFY — record() must not propagate the violation
        assertDoesNotThrow(() -> auditLogService.record(event, "1001", "{\"raw\":\"message\"}"));

        // VERIFY — the save was still attempted
        verify(auditLogRepository).save(any());
    }

}
