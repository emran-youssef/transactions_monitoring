package com.eyatrooz.transaction_monitoring.transaction_service.kafka;

import com.eyatrooz.transaction_monitoring.transaction_service.dtos.TransactionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionEventPublisher {

    private static final String TOPIC = "transactions.created.v1";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publishTransactionCreated(TransactionResponse response){
        var event = EventMessage.of(TOPIC, response);
        try {

            // * The event is manually serialized to JSON because the Kafka producer
            // * is configured with StringSerializer. The consumer receives the payload
            // * as a JSON string and deserializes it using ObjectMapper.

            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC, response.getAccountId(), json);
            log.info("Published event: type={}, accountId={}", TOPIC, response.getAccountId());

        } catch (JacksonException e) {
            log.error("Failed to serialize event: type={}, accountId={}", TOPIC, response.getAccountId(), e);
        }
    }
}
