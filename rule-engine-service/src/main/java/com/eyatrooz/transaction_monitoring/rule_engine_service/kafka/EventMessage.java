package com.eyatrooz.transaction_monitoring.rule_engine_service.kafka;

import com.eyatrooz.transaction_monitoring.rule_engine_service.dtos.TransactionCreatedPayload;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
public class EventMessage<T> {
    private String eventId;
    private String eventType;
    private Instant occuredAt;
    private TransactionCreatedPayload payload;
}
