package com.eyatrooz.transaction_monitoring.rule_engine_service.kafka;

import com.eyatrooz.transaction_monitoring.rule_engine_service.dtos.TransactionFlaggedPayload;
import com.eyatrooz.transaction_monitoring.rule_engine_service.entities.RuleEvaluation;
import com.eyatrooz.transaction_monitoring.rule_engine_service.mappers.RuleEvaluationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class FlaggedTransactionPublisher{

    private final ObjectMapper objectMapper;
    private final RuleEvaluationMapper ruleEvaluationMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String TOPIC = "transactions.flagged.v1";

    public void publishFlagged(RuleEvaluation ruleEvaluation){

        var payload = ruleEvaluationMapper.toFlaggedPayload(ruleEvaluation);
        var event = EventMessage.of(TOPIC, payload);

        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC, ruleEvaluation.getAccountId(), json);
            log.info("Published event: type={}, transactionId={}", TOPIC, ruleEvaluation.getTransactionId());

        } catch (JacksonException e) {
            log.error("Failed to serialize event: type={}, transactionId={}", TOPIC, ruleEvaluation.getTransactionId(), e);
        }
    }
}