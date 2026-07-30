package com.eyatrooz.transaction_monitoring.audit_service.service;

import com.eyatrooz.transaction_monitoring.audit_service.entity.AuditLogEntry;
import com.eyatrooz.transaction_monitoring.audit_service.kafka.EventMessage;
import com.eyatrooz.transaction_monitoring.audit_service.repositories.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public void record(EventMessage<?> eventMessage, String entityId) {
        if (auditLogRepository.findByEventId(eventMessage.getEventId()).isPresent()) {
            log.debug("eventId={} already recorded, skipping", eventMessage.getEventId());
            return;
        }

        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(eventMessage.getPayload());
        } catch (JacksonException ex) {
            log.error("Failed to serialize payload for eventId={}", eventMessage.getEventId(), ex);
            throw new RuntimeException("Failed to serialize audit payload", ex);
        }

        AuditLogEntry entry = AuditLogEntry.builder()
                .eventId(eventMessage.getEventId())
                .eventType(eventMessage.getEventType())
                .entityId(entityId)
                .occurredAt(eventMessage.getOccurredAt())
                .payload(payloadJson)
                .build();

        auditLogRepository.save(entry);
        log.info("Recorded audit log entry eventId={} eventType={} entityId={}",
                eventMessage.getEventId(), eventMessage.getEventType(), entityId);
    }
}
