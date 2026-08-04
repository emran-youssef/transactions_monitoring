package com.eyatrooz.transaction_monitoring.audit_service.kafka.consumer;

import com.eyatrooz.transaction_monitoring.audit_service.kafka.EventMessage;
import com.eyatrooz.transaction_monitoring.audit_service.kafka.KafkaTopics;
import com.eyatrooz.transaction_monitoring.audit_service.kafka.payload.CaseUpdatedPayload;
import com.eyatrooz.transaction_monitoring.audit_service.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class CaseUpdatedAuditConsumer {

    private final ObjectMapper objectMapper;
    private final AuditLogService auditLogService;

    @KafkaListener(topics = KafkaTopics.CASE_UPDATED, groupId = "${spring.kafka.consumer.group-id}")
    public void onCaseUpdated(String message) throws JacksonException {
        EventMessage<CaseUpdatedPayload> event = objectMapper.readValue(message,
                objectMapper.getTypeFactory().constructParametricType(EventMessage.class, CaseUpdatedPayload.class));

        String entityId = event.getPayload().getId();
        auditLogService.record(event, entityId, message);
    }
}
