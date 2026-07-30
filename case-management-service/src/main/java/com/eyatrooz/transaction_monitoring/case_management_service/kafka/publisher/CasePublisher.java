package com.eyatrooz.transaction_monitoring.case_management_service.kafka.publisher;

import com.eyatrooz.transaction_monitoring.case_management_service.dtos.CasePayload;
import com.eyatrooz.transaction_monitoring.case_management_service.kafka.EventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class CasePublisher {

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafka;

    public void publish(String topic, CasePayload response){
        var event = EventMessage.of(topic, response);

        try {
            String json = objectMapper.writeValueAsString(event);
            kafka.send(topic, response.getAccountId(), json);
            log.info("Published event: type={}, accountId={}", topic, response.getAccountId());

        } catch (JacksonException ex) {
            log.error("Failed to serialize event: type={}, accountId={}", topic, response.getAccountId(), ex);
        }
    }

}
