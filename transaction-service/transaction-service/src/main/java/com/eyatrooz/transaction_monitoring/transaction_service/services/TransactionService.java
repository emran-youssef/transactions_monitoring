package com.eyatrooz.transaction_monitoring.transaction_service.services;

import com.eyatrooz.transaction_monitoring.transaction_service.dtos.TransactionRequest;
import com.eyatrooz.transaction_monitoring.transaction_service.dtos.TransactionResponse;
import com.eyatrooz.transaction_monitoring.transaction_service.entities.OutboxEvent;
import com.eyatrooz.transaction_monitoring.transaction_service.enums.TransactionStatus;
import com.eyatrooz.transaction_monitoring.transaction_service.kafka.EventMessage;
import com.eyatrooz.transaction_monitoring.transaction_service.kafka.KafkaTopic;
import com.eyatrooz.transaction_monitoring.transaction_service.mappers.TransactionMapper;
import com.eyatrooz.transaction_monitoring.transaction_service.repositories.OutboxEventsRepository;
import com.eyatrooz.transaction_monitoring.transaction_service.repositories.TransactionRepository;
import jakarta.transaction.Transactional;
import tools.jackson.core.JacksonException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final ObjectMapper objectMapper;
    private final TransactionMapper transactionMapper;
    private final TransactionRepository transactionRepository;
    private final OutboxEventsRepository outboxEventsRepository;

    private static final String AGGREGATE_TYPE = "Transaction";
    private static final String TOPIC = KafkaTopic.TOPIC;

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request){
        log.info("=== Transaction Received from accountId={}, amount={} ===", request.getAccountId(), request.getAmount());

        var transaction = transactionMapper.toEntity(request);
        transaction.setTransactionStatus(TransactionStatus.RECEIVED);

        var saved = transactionRepository.save(transaction);
        log.info("Transaction saved: id={}, accountId={}, status={}", saved.getId(), saved.getAccountId(), saved.getTransactionStatus());

        var response = transactionMapper.toDto(saved);
        var event = EventMessage.of(TOPIC, response);

        try{
            var payload = objectMapper.writeValueAsString(event);
            var outboxEvent = OutboxEvent.from(AGGREGATE_TYPE, saved.getId().toString(), TOPIC, payload);
            outboxEventsRepository.save(outboxEvent);
            log.info("Outbox event recorded: type={}, accountId={}", TOPIC, request.getAccountId());

        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to serialize outbox event for transaction id=" + saved.getId(), e);
        }

        return response;

    }
}
