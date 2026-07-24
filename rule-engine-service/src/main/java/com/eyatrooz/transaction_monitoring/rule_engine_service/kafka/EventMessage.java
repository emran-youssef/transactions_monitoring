package com.eyatrooz.transaction_monitoring.rule_engine_service.kafka;

import com.eyatrooz.transaction_monitoring.rule_engine_service.dtos.TransactionCreatedPayload;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventMessage<T> {

    private String eventId;
    private String eventType;
    private Instant occuredAt;
    private T payload;


    public static <T> EventMessage<T> of(String eventType, T payload){
       return EventMessage.<T>builder()
               .eventId(UUID.randomUUID().toString())
               .eventType(eventType)
               .occuredAt(Instant.now())
               .payload(payload)
               .build();
    }
}
