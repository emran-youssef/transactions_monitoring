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
import org.springframework.transaction.annotation.Transactional;
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

        var flaggedTransaction = FlaggedTransactionEvent.from(transactionFlagged);

        // Idempotency: unique constraint idx_flagged_events_transaction_id rejects a repeat insert for a duplicate transactionId, rolling back this whole transaction.
        flaggedTransactionEventRepository.save(flaggedTransaction);
        log.info("Flagged transaction event persisted for transactionId={}", transactionFlagged.getTransactionId());

        // NOTE: newCase creation opens a history as well
        var newCase = Case.createFrom(transactionFlagged);

        // Idempotency: unique constraint idx_cases_transaction_id rejects a repeat insert for a duplicate transactionId, rolling back this whole transaction.
        // persist newCase, and history persisted by Spring via cascade.All
        var newCasePersisted = caseRepository.save(newCase);
        log.info("Case created: id={}, transactionId={}, status={}",
                newCasePersisted.getId(), newCasePersisted.getTransactionId(), newCasePersisted.getStatus());

        // outbox publisher
        var casePayload = caseMapper.toCasePayload(newCasePersisted);
        outboxEventRepository.save(outboxEventFactory.create(AggregateType.CASE_CREATION, newCasePersisted.getId().toString(), KafkaTopics.CASE_CREATED, casePayload));
        log.info("Outbox event recorded: type={}, accountId={}", KafkaTopics.CASE_CREATED, newCasePersisted.getAccountId());

    }


}
