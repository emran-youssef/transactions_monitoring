package com.eyatrooz.transaction_monitoring.transaction_service.services;

import com.eyatrooz.transaction_monitoring.transaction_service.dtos.TransactionRequest;
import com.eyatrooz.transaction_monitoring.transaction_service.dtos.TransactionResponse;
import com.eyatrooz.transaction_monitoring.transaction_service.enums.TransactionStatus;
import com.eyatrooz.transaction_monitoring.transaction_service.kafka.helpers.AggregateType;
import com.eyatrooz.transaction_monitoring.transaction_service.kafka.helpers.KafkaTopic;
import com.eyatrooz.transaction_monitoring.transaction_service.kafka.helpers.OutboxEventFactory;
import com.eyatrooz.transaction_monitoring.transaction_service.mappers.TransactionMapper;
import com.eyatrooz.transaction_monitoring.transaction_service.repositories.OutboxEventsRepository;
import com.eyatrooz.transaction_monitoring.transaction_service.repositories.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionMapper transactionMapper;
    private final TransactionRepository transactionRepository;
    private final OutboxEventsRepository outboxEventsRepository;
    private final OutboxEventFactory outboxEventFactory;

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request){
        log.info("=== Transaction Received from accountId={}, amount={} ===", request.getAccountId(), request.getAmount());

        var transaction = transactionMapper.toEntity(request);
        transaction.setTransactionStatus(TransactionStatus.RECEIVED);

        var saved = transactionRepository.save(transaction);
        log.info("Transaction saved: id={}, accountId={}, status={}", saved.getId(), saved.getAccountId(), saved.getTransactionStatus());

        var response = transactionMapper.toDto(saved);
        outboxEventsRepository.save(outboxEventFactory.create(AggregateType.TRANSACTION, saved.getId().toString(), KafkaTopic.TOPIC, response));
        log.info("Outbox event recorded: type={}, accountId={}", KafkaTopic.TOPIC, request.getAccountId());

        return response;

    }
}
