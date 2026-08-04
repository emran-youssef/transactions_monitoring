package com.eyatrooz.transaction_monitoring.audit_service.kafka.consumer;

import com.eyatrooz.transaction_monitoring.audit_service.kafka.EventMessage;
import com.eyatrooz.transaction_monitoring.audit_service.kafka.KafkaTopics;
import com.eyatrooz.transaction_monitoring.audit_service.kafka.payload.TransactionFlaggedPayload;
import com.eyatrooz.transaction_monitoring.audit_service.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class TransactionFlaggedAuditConsumer {

    private final ObjectMapper objectMapper;
    private final AuditLogService auditLogService;

    @KafkaListener(topics = KafkaTopics.FLAGGED_TRANSACTION, groupId = "${spring.kafka.consumer.group-id}")
    public void onTransactionFlagged(String message) throws JacksonException {
        EventMessage<TransactionFlaggedPayload> event = objectMapper.readValue(message,
                objectMapper.getTypeFactory().constructParametricType(EventMessage.class, TransactionFlaggedPayload.class));

        String entityId = event.getPayload().getTransactionId();
        auditLogService.record(event, entityId, message);
    }
}
