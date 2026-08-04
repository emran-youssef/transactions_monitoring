package com.eyatrooz.transaction_monitoring.audit_service.kafka.consumer;

import com.eyatrooz.transaction_monitoring.audit_service.kafka.EventMessage;
import com.eyatrooz.transaction_monitoring.audit_service.kafka.KafkaTopics;
import com.eyatrooz.transaction_monitoring.audit_service.kafka.payload.TransactionCreatedPayload;
import com.eyatrooz.transaction_monitoring.audit_service.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class TransactionCreatedAuditConsumer {

    private final ObjectMapper objectMapper;
    private final AuditLogService auditLogService;

    @KafkaListener(topics = KafkaTopics.TRANSACTION_CREATED, groupId = "${spring.kafka.consumer.group-id}")
    public void onTransactionCreated(String message) throws JacksonException {
        EventMessage<TransactionCreatedPayload> event = objectMapper.readValue(message,
                objectMapper.getTypeFactory().constructParametricType(EventMessage.class, TransactionCreatedPayload.class));

        String entityId = event.getPayload().getId();
        auditLogService.record(event, entityId, message);
    }
}

