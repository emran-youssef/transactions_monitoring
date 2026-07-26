package com.eyatrooz.transaction_monitoring.case_management_service.kafka;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class EventMessage<T>{
    private String eventId;
    private String eventType;
    private Instant occurredAt;
    private T payload;

    public static<T> EventMessage<T> of(String eventType, T payload){
        return EventMessage.<T>builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .occurredAt(Instant.now())
                .payload(payload)
                .build();
    }

}
