package com.eyatrooz.transaction_monitoring.case_management_service.kafka;

import com.eyatrooz.transaction_monitoring.case_management_service.dtos.CaseCreatedPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class CaseCreatedPublisher {

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafka;

    private static final String TOPIC = "cases.created.v1";

    public void publish(CaseCreatedPayload response){
        var event = EventMessage.of(TOPIC, response);

        try {
            String json = objectMapper.writeValueAsString(event);
            kafka.send(TOPIC, response.getAccountId(), json);
            log.info("Published event: type={}, accountId={}", TOPIC, response.getAccountId());

        } catch (JacksonException ex) {
            log.error("Failed to serialize event: type={}, accountId={}", TOPIC, response.getAccountId(), ex);
        }
    }

}
