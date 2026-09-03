package com.eyatrooz.transaction_monitoring.case_management_service.kafka.consumer;

import com.eyatrooz.transaction_monitoring.case_management_service.dtos.TransactionFlaggedPayload;
import com.eyatrooz.transaction_monitoring.case_management_service.kafka.EventMessage;
import com.eyatrooz.transaction_monitoring.case_management_service.kafka.KafkaTopics;
import com.eyatrooz.transaction_monitoring.case_management_service.services.CaseCreationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class FlaggedTransactionConsumerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CaseCreationService caseCreationService;

    private FlaggedTransactionConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new FlaggedTransactionConsumer(objectMapper, caseCreationService);
    }

    private String flaggedTransactionMessage(long transactionId) {
        var payload = new TransactionFlaggedPayload();
        payload.setTransactionId(transactionId);
        payload.setAccountId("ACC-100");
        payload.setFlagged(true);
        payload.setRiskScore(BigDecimal.valueOf(9150));
        payload.setEvaluatedAt(Instant.now());

        var event = EventMessage.of(KafkaTopics.FLAGGED_TRANSACTION, payload);
        return objectMapper.writeValueAsString(event);
    }

    @Test
    void consumeFlaggedTransaction_delegatesToService() {
        var message = flaggedTransactionMessage(1001L);

        consumer.consumeFlaggedTransaction(message);

        verify(caseCreationService).processFlaggedTransaction(any());
    }

    @Test
    void consumeFlaggedTransaction_swallowsDuplicate_whenServiceThrowsConstraintViolation() {
        var message = flaggedTransactionMessage(1001L);

        // Idempotency: a duplicate causes CaseCreationService's transaction to roll back and throw here — the consumer must not propagate it.
        doThrow(new DataIntegrityViolationException("duplicate transaction_id"))
                .when(caseCreationService).processFlaggedTransaction(any());

        consumer.consumeFlaggedTransaction(message);

        verify(caseCreationService).processFlaggedTransaction(any());
    }

}
