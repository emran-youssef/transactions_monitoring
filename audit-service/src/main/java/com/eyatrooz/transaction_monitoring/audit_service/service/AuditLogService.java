package com.eyatrooz.transaction_monitoring.audit_service.service;

import com.eyatrooz.transaction_monitoring.audit_service.entity.AuditLogEntry;
import com.eyatrooz.transaction_monitoring.audit_service.kafka.EventMessage;
import com.eyatrooz.transaction_monitoring.audit_service.repositories.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;

    public void record(EventMessage<?> eventMessage, String entityId, String kafkaPayload) {
        if (auditLogRepository.findByEventId(eventMessage.getEventId()).isPresent()) {
            log.debug("eventId={} already recorded, skipping", eventMessage.getEventId());
            return;
        }

        AuditLogEntry entry = AuditLogEntry.builder()
                .eventId(eventMessage.getEventId())
                .eventType(eventMessage.getEventType())
                .entityId(entityId)
                .occurredAt(eventMessage.getOccurredAt())
                .payload(kafkaPayload)
                .build();

        auditLogRepository.save(entry);
        log.info("Recorded audit log entry eventId={} eventType={} entityId={}",
                eventMessage.getEventId(), eventMessage.getEventType(), entityId);
    }
}
