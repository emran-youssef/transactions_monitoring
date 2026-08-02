package com.eyatrooz.transaction_monitoring.rule_engine_service.kafka;

import com.eyatrooz.transaction_monitoring.rule_engine_service.entities.OutboxEvent;
import com.eyatrooz.transaction_monitoring.rule_engine_service.repositories.OutboxEventsRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private static final int BATCH_SIZE = 100;

    private final KafkaTemplate<String, String> kafka;
    private final OutboxEventsRepository outboxEventsRepository;

    @Transactional
    @Scheduled(fixedDelay = 1000)
    public void publishPendingEvent(){
        List<OutboxEvent> pending = outboxEventsRepository
                .findByPublishedFalseOrderByCreatedAtAsc(PageRequest.of(0, BATCH_SIZE));

        for(OutboxEvent event : pending){
            try {
                kafka.send(event.getEventType(), event.getAggregateId(), event.getPayload())
                        .get(); // Wait for Kafka acknowledgment before marking the event as published.

                event.setPublished(true);
                event.setPublishedAt(Instant.now());
                outboxEventsRepository.save(event);

                log.info("Outbox event published: id={}, type={}, aggregateId={}",
                        event.getId(), event.getEventType(), event.getAggregateId());

            } catch (Exception e) {
                log.error("Failed to publish outbox event: id={}, type={}", event.getId(), event.getEventType(), e);
                // leave unpublished - retried automatically on next poll cycle
            }
        }
    }
}
