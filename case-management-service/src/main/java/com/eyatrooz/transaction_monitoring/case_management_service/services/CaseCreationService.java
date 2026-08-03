package com.eyatrooz.transaction_monitoring.case_management_service.services;

import  com.eyatrooz.transaction_monitoring.case_management_service.dtos.TransactionFlaggedPayload;
import com.eyatrooz.transaction_monitoring.case_management_service.entities.Case;
import com.eyatrooz.transaction_monitoring.case_management_service.entities.FlaggedTransactionEvent;
import com.eyatrooz.transaction_monitoring.case_management_service.kafka.AggregateType;
import com.eyatrooz.transaction_monitoring.case_management_service.kafka.KafkaTopics;
import com.eyatrooz.transaction_monitoring.case_management_service.kafka.OutboxEventFactory;
import com.eyatrooz.transaction_monitoring.case_management_service.mappers.CaseMapper;
import com.eyatrooz.transaction_monitoring.case_management_service.repositories.CaseRepository;
import com.eyatrooz.transaction_monitoring.case_management_service.repositories.FlaggedTransactionEventRepository;
import com.eyatrooz.transaction_monitoring.case_management_service.repositories.OutboxEventsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class CaseCreationService {

    private final CaseMapper caseMapper;
    private final CaseRepository caseRepository;
    private final OutboxEventFactory outboxEventFactory;
    private final OutboxEventsRepository outboxEventRepository;
    private final FlaggedTransactionEventRepository flaggedTransactionEventRepository;


    @Transactional
    public void processFlaggedTransaction(TransactionFlaggedPayload transactionFlagged){
        log.info("=== Processing flagged transaction {} ===", transactionFlagged.getTransactionId());

        Long transactionId = transactionFlagged.getTransactionId();

        // Idempotency check #1: event-level
        if(transactionExists(transactionId)) {
            log.warn("Flagged Event already recorded for transactionId={}", transactionFlagged.getTransactionId());
            return ;
        }

        var flaggedTransaction = FlaggedTransactionEvent.from(transactionFlagged);
        flaggedTransactionEventRepository.save(flaggedTransaction);

        // Idempotency check #2: case-level
        if(caseExists(transactionId)){
            log.warn("Case already exists for transactionId={}, skipping creation", transactionId);
            return ;
        }

        // NOTE: newCase creation opens a history as well
        var newCase = Case.createFrom(transactionFlagged);

        // persist newCase, and history persisted by Spring via cascade.All
        var newCasePersisted = caseRepository.save(newCase);
        log.info("Case created: id={}, transactionId={}, status={}",
                newCasePersisted.getId(), newCasePersisted.getTransactionId(), newCasePersisted.getStatus());

        var casePayload = caseMapper.toCasePayload(newCasePersisted);
        outboxEventRepository.save(outboxEventFactory.create(AggregateType.CASE_CREATION, newCasePersisted.getId().toString(), KafkaTopics.CASE_CREATED, casePayload));
        log.info("Outbox event recorded: type={}, accountId={}", KafkaTopics.CASE_CREATED, newCasePersisted.getAccountId());

    }


    // -- helpers
    private boolean transactionExists(Long transactionId){
        return flaggedTransactionEventRepository.existsByTransactionId(transactionId);}

    private boolean caseExists(Long transactionId){
        return caseRepository.existsByTransactionId(transactionId);
    }



}
