package com.eyatrooz.transaction_monitoring.audit_service.kafka.consumer;

import com.eyatrooz.transaction_monitoring.audit_service.kafka.EventMessage;
import com.eyatrooz.transaction_monitoring.audit_service.kafka.payload.TransactionFlaggedPayload;
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
public class TransactionFlaggedAuditConsumer {

    private final ObjectMapper objectMapper;
    private final AuditLogService auditLogService;

    @KafkaListener(topics = "transactions.flagged.v1", groupId = "${spring.kafka.consumer.group-id}")
    public void onTransactionFlagged(String message) {
        EventMessage<TransactionFlaggedPayload> event;
        try {
            event = objectMapper.readValue(
                    message,
                    objectMapper.getTypeFactory().constructParametricType(EventMessage.class, TransactionFlaggedPayload.class)
            );
        } catch (JacksonException ex) {
            log.error("Failed to deserialize transactions.flagged.v1 message: {}", message, ex);
            return;
        }

        String entityId = event.getPayload().getTransactionId();
        auditLogService.record(event, entityId, message);
    }
}
