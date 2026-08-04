package com.eyatrooz.transaction_monitoring.rule_engine_service.kafka.consumer;

import com.eyatrooz.transaction_monitoring.rule_engine_service.dtos.TransactionCreatedPayload;
import com.eyatrooz.transaction_monitoring.rule_engine_service.entities.TransactionHistory;
import com.eyatrooz.transaction_monitoring.rule_engine_service.kafka.helpers.EventMessage;
import com.eyatrooz.transaction_monitoring.rule_engine_service.kafka.helpers.KafkaTopic;
import com.eyatrooz.transaction_monitoring.rule_engine_service.repositories.RuleEvaluationRepository;
import com.eyatrooz.transaction_monitoring.rule_engine_service.repositories.TransactionHistoryRepository;
import com.eyatrooz.transaction_monitoring.rule_engine_service.services.RuleEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;


@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionCreatedConsumer {
    private final RuleEvaluationRepository ruleEvaluationRepository;


    private final ObjectMapper objectMapper;
    private final RuleEvaluationService ruleEvaluationService;
    private final TransactionHistoryRepository transactionHistoryRepository;


    @KafkaListener(topics = KafkaTopic.TRANSACTION_CREATED, groupId = "${spring.kafka.consumer.group-id}")
    public void onTransactionCreated(String message) {
        EventMessage<TransactionCreatedPayload> event;
        try {
            event = objectMapper.readValue(
                    message,
                    objectMapper.getTypeFactory().constructParametricType(EventMessage.class, TransactionCreatedPayload.class)
            );
            log.info("Received message from kafka for transaction_id={}", event.getPayload().getId());

        } catch (JacksonException ex) {
            log.error("Failed to deserialize transactions.created.v1 message: {}", message, ex);
            return;
        }

        var payload = event.getPayload();

        log.info(" === Processing transaction {} === ", payload.getId());

        var transaction = TransactionHistory.from(payload);
        if (!transactionHistoryRepository.existsByTransactionId(payload.getId())) {
            transactionHistoryRepository.save(transaction);
            log.info("Persisted transaction_history for transactionId={}", payload.getId());
        } else {
            log.info("transactionId={} already in transaction_history, skipping insert", payload.getId());
        }

        if (!ruleEvaluationRepository.existsByTransactionId(payload.getId())){
            ruleEvaluationService.evaluate(transaction);
        } else {
            log.info("transactionId={} already evaluated, skipping", payload.getId());
        }


        }
    }
