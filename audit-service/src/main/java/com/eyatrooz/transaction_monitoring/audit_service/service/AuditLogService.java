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

    public void record(EventMessage<?> message, String entityId, String payload) {
        if (auditLogRepository.findByEventId(message.getEventId()).isPresent()) {
            log.debug("eventId={} already recorded, skipping", message.getEventId());
            return;
        }

        var audit = AuditLogEntry.from(message, entityId, payload);

        auditLogRepository.save(audit);
        log.info("Recorded audit log entry eventId={} eventType={} entityId={}",
                message.getEventId(), message.getEventType(), entityId);
    }
}
