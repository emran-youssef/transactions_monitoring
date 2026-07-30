package com.eyatrooz.transaction_monitoring.audit_service.kafka.consumer;

import com.eyatrooz.transaction_monitoring.audit_service.kafka.EventMessage;
import com.eyatrooz.transaction_monitoring.audit_service.kafka.payload.TransactionCreatedPayload;
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
public class TransactionCreatedAuditConsumer {

    private final ObjectMapper objectMapper;
    private final AuditLogService auditLogService;

    @KafkaListener(topics = "transactions.created.v1", groupId = "${spring.kafka.consumer.group-id}")
    public void onTransactionCreated(String message) {
        EventMessage<TransactionCreatedPayload> event;
        try {
            event = objectMapper.readValue(
                    message,
                    objectMapper.getTypeFactory().constructParametricType(EventMessage.class, TransactionCreatedPayload.class)
            );
        } catch (JacksonException ex) {
            log.error("Failed to deserialize transactions.created.v1 message: {}", message, ex);
            return;
        }

        String entityId = event.getPayload().getId();
        auditLogService.record(event, entityId, message);
    }
}
