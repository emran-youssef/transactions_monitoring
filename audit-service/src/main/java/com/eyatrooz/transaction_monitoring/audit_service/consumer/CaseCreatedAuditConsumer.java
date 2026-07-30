package com.eyatrooz.transaction_monitoring.audit_service.consumer;

import com.eyatrooz.transaction_monitoring.audit_service.kafka.EventMessage;
import com.eyatrooz.transaction_monitoring.audit_service.kafka.payload.CaseCreatedPayload;
import com.eyatrooz.transaction_monitoring.audit_service.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class CaseCreatedAuditConsumer {

    private final ObjectMapper objectMapper;
    private final AuditLogService auditLogService;

    @KafkaListener(topics = "cases.created.v1", groupId = "${spring.kafka.consumer.group-id}")
    public void onCaseCreated(String message) {
        EventMessage<CaseCreatedPayload> event;
        try {
            event = objectMapper.readValue(
                    message,
                    objectMapper.getTypeFactory().constructParametricType(EventMessage.class, CaseCreatedPayload.class)
            );
        } catch (JacksonException ex) {
            log.error("Failed to deserialize cases.created.v1 message: {}", message, ex);
            return;
        }

        String entityId = event.getPayload().getCaseId();
        auditLogService.record(event, entityId);
    }
}
