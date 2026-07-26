package com.eyatrooz.transaction_monitoring.case_management_service.kafka;


import com.eyatrooz.transaction_monitoring.case_management_service.dtos.TransactionFlaggedPayload;
import com.eyatrooz.transaction_monitoring.case_management_service.services.CaseCreationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class FlaggedTransactionConsumer {

    private final ObjectMapper objectMapper;
    private final CaseCreationService  caseCreationService;

    @KafkaListener(topics = "transactions.flagged.v1", groupId = "case-management-service")
    public void consumeFlaggedTransaction(String message){
        EventMessage<TransactionFlaggedPayload> event;
        try {
            event = objectMapper.readValue(message,
                    objectMapper.getTypeFactory()
                                .constructParametricType(EventMessage.class, TransactionFlaggedPayload.class)
            );
            log.info("Received message form kafka for transactionId={}, eventId={}", event.getPayload().getTransactionId(), event.getEventId());

        } catch (JacksonException ex) {
            log.error("Failed to deserialize transactions.flagged.v1 message: {}", message, ex);
            return;
        }

        // proceeds the flaggedTransaction process: open case & history
        caseCreationService.processFlaggedTransaction(event.getPayload());
    }
}





