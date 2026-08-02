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

      /**
     * THIS CLASS IS NEVER USED NOW, THE OUTBOX PATTERN HANDLE THE PUBLISHING
     */

    private static final String TOPIC = KafkaTopic.TOPIC;

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafka;

    public void publishTransactionCreated(TransactionResponse response){
        var event = EventMessage.of(TOPIC, response);
        try {

            String json = objectMapper.writeValueAsString(event);
            kafka.send(TOPIC, response.getAccountId(), json);
            log.info("Published event: type={}, accountId={}", TOPIC, response.getAccountId());

        } catch (JacksonException e) {
            log.error("Failed to serialize event: type={}, accountId={}", TOPIC, response.getAccountId(), e);
        }
    }
}
