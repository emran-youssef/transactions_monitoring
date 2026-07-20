package com.eyatrooz.transaction_monitoring.rule_engine_service.kafka;

import com.eyatrooz.transaction_monitoring.rule_engine_service.dtos.TransactionCreatedPayload;
import com.eyatrooz.transaction_monitoring.rule_engine_service.entities.TransactionHistory;
import com.eyatrooz.transaction_monitoring.rule_engine_service.repositories.TransactionHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;


@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionCreatedConsumer {

    private final ObjectMapper objectMapper;
    private final TransactionHistoryRepository transactionHistoryRepository;

    /**
     * Consumes transaction creation events from Kafka.

     * Flow:
     * 1. Receives JSON message from transactions.created.v1 topic.
     * 2. Deserializes JSON into EventMessage<TransactionCreatedPayload>.
     * 3. Processes the transaction event.

     * Note:
     * constructParametricType() is required because Java generic types are erased
     * at runtime, so Jackson needs the payload type explicitly.
     */

    @KafkaListener(topics = "transactions.created.v1", groupId = "${spring.kafka.consumer.group-id}")
    public void onTransactionCreated(String message){
        EventMessage<TransactionCreatedPayload> event;
        try {
            event = objectMapper.readValue(
                    message,
                    objectMapper.getTypeFactory().constructParametricType(EventMessage.class, TransactionCreatedPayload.class)
            );
            log.info("Received message from kafka for transaction_id={}", event.getPayload().getId());

        } catch (JacksonException ex) {
            log.error("Failed to deserialize transactions.created.v1 message: {}", message, ex);
            return;
        }

        TransactionCreatedPayload payload = event.getPayload();
        if(transactionHistoryRepository.existsByTransactionId(payload.getId())){
            log.info("transactionId={} already in transaction_history, skipping", payload.getId());
            return;
        }

        var transactionHistory = TransactionHistory.builder()
                .transactionId(payload.getId())
                .accountId(payload.getAccountId())
                .amount(payload.getAmount())
                .transactionType(payload.getTransactionType())
                .createdAt(payload.getCreatedAt())
                .receivedAt(payload.getReceivedAt())
                .build();

        transactionHistoryRepository.save(transactionHistory);
        log.info("Persisted transaction_history for transactionId={}", payload.getId());
    }
}
