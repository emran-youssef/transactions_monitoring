package com.eyatrooz.transaction_monitoring.rule_engine_service.kafka.helpers;

import com.eyatrooz.transaction_monitoring.rule_engine_service.entities.OutboxEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventFactory {

    private final ObjectMapper objectMapper;

    public <T>OutboxEvent create(String aggregateType, String aggregateId, String topic, T payload){
        var event = EventMessage.of(topic, payload);
        try {
            var json = objectMapper.writeValueAsString(event);
            return OutboxEvent.from(aggregateType, aggregateId, topic, json);
        } catch(JacksonException e) {
            throw new IllegalStateException(
                    "Failed to serialize outbox event for aggregateId=" + aggregateId, e);
        }
    }
}
