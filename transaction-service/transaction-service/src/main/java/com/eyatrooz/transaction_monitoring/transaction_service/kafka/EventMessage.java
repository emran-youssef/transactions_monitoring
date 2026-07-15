package com.eyatrooz.transaction_monitoring.transaction_service.kafka;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Builder
@Getter
public class EventMessage<T> {
    private final String eventId;
    private final String eventType;
    private final Instant occurredAt;
    private final T payload;

    public static <T> EventMessage<T> of(String eventType, T payload){
        return EventMessage.<T>builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .occurredAt(Instant.now())
                .payload(payload)
                .build();
    }
}
