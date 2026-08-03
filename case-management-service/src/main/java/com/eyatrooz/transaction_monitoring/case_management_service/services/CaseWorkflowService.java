package com.eyatrooz.transaction_monitoring.case_management_service.services;

import com.eyatrooz.transaction_monitoring.case_management_service.dtos.CaseResponse;
import com.eyatrooz.transaction_monitoring.case_management_service.entities.Case;
import com.eyatrooz.transaction_monitoring.case_management_service.entities.CaseHistory;
import com.eyatrooz.transaction_monitoring.case_management_service.enums.CaseStatus;
import com.eyatrooz.transaction_monitoring.case_management_service.exceptions.CaseNotFoundException;
import com.eyatrooz.transaction_monitoring.case_management_service.exceptions.CaseNotAssignedException;
import com.eyatrooz.transaction_monitoring.case_management_service.exceptions.IllegalCaseTransitionException;
import com.eyatrooz.transaction_monitoring.case_management_service.kafka.AggregateType;
import com.eyatrooz.transaction_monitoring.case_management_service.kafka.KafkaTopics;
import com.eyatrooz.transaction_monitoring.case_management_service.kafka.OutboxEventFactory;
import com.eyatrooz.transaction_monitoring.case_management_service.mappers.CaseHistoryMapper;
import com.eyatrooz.transaction_monitoring.case_management_service.mappers.CaseMapper;
import com.eyatrooz.transaction_monitoring.case_management_service.repositories.CaseRepository;
import com.eyatrooz.transaction_monitoring.case_management_service.repositories.OutboxEventsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class CaseWorkflowService {

    private final CaseMapper caseMapper;
    private final CaseRepository caseRepository;
    private final CaseHistoryMapper caseHistoryMapper;
    private final OutboxEventFactory outboxEventFactory;
    private final OutboxEventsRepository outboxEventRepository;


    @Transactional
    public CaseResponse assign(Long caseId, String analyst){
        log.info("== assigning case:{} ==", caseId);

        var fetchedCase = loadCase(caseId);

        // case status must be open befoe assignment
        requireStatus(fetchedCase, CaseStatus.OPEN);

        fetchedCase.setStatus(CaseStatus.UNDER_REVIEW);
        fetchedCase.setAssignedAnalyst(analyst);
        fetchedCase.addHistory(CaseHistory.assigned(analyst));

        // persist the case, and history persisted by Spring via cascade.All
        var persisted = caseRepository.save(fetchedCase);
        log.info("Case assigned: id={}, analyst={}, status={}", persisted.getId(), analyst, persisted.getStatus());

        // for the event
        var casePayload = caseMapper.toCasePayload(persisted);
        outboxEventRepository.save(outboxEventFactory.create(AggregateType.CASE_UPDATE, persisted.getId().toString(), KafkaTopics.CASE_UPDATED, casePayload));
        log.info("Outbox event recorded: type={}, accountId={}", KafkaTopics.CASE_UPDATED, persisted.getAccountId());

        return caseMapper.toResponse(persisted);
    }


    @Transactional
    public CaseResponse approve(Long caseId, String currentUsername, String explanation){
        log.info("== Approving case {} ==", caseId);

        var fetchedCase = loadCase(caseId);

        requireStatus(fetchedCase, CaseStatus.UNDER_REVIEW);
        requireAssignedAnalyst(fetchedCase, currentUsername);

        fetchedCase.setStatus(CaseStatus.APPROVED);
        fetchedCase.addHistory(CaseHistory.approved(currentUsername, explanation));

        // persist the case, and history persisted by Spring via cascade.All
        var persisted = caseRepository.save(fetchedCase);
        log.info("Case approved: id={}, analyst={}, status={}", persisted.getId(), currentUsername, persisted.getStatus());

        var casePayload = caseMapper.toCasePayload(persisted);
        outboxEventRepository.save(outboxEventFactory.create(AggregateType.CASE_UPDATE, persisted.getId().toString(), KafkaTopics.CASE_UPDATED, casePayload));
        log.info("Outbox event recorded: type={}, accountId={}", KafkaTopics.CASE_UPDATED, persisted.getAccountId());

        return caseMapper.toResponse(persisted);
    }

    @Transactional
    public CaseResponse escalate(Long caseId, String currentUsername, String explanation){
        log.info("== escalating case:{} ==", caseId);
        var fetchedCase = loadCase(caseId);

        requireStatus(fetchedCase, CaseStatus.UNDER_REVIEW);
        requireAssignedAnalyst(fetchedCase, currentUsername);

        fetchedCase.setStatus(CaseStatus.ESCALATED);
        fetchedCase.addHistory(CaseHistory.escalated(currentUsername, explanation));

        // persist the case, and history persisted by Spring via cascade.All
        var persisted = caseRepository.save(fetchedCase);
        log.info("Case escalated: id={}, analyst={}, status={}", persisted.getId(), currentUsername, persisted.getStatus());

        var casePayload = caseMapper.toCasePayload(persisted);
        outboxEventRepository.save(outboxEventFactory.create(AggregateType.CASE_UPDATE, persisted.getId().toString(), KafkaTopics.CASE_UPDATED, casePayload));
        log.info("Outbox event recorded: type={}, accountId={}", KafkaTopics.CASE_UPDATED, persisted.getAccountId());

        return caseMapper.toResponse(persisted);
    }

    public CaseResponse fetchCase(Long id){
        var fetchedCase = caseRepository.findById(id)
                .orElseThrow(()-> new CaseNotFoundException("Case not found!"));
        return caseMapper.toResponse(fetchedCase);
    }

    // -- helpers
    private Case loadCase(Long caseId){
        return caseRepository.findById(caseId)
                .orElseThrow(()->new CaseNotFoundException("Case not found, caseId="+ caseId));
    }

    private void requireStatus(Case caseEntity, CaseStatus expected){
        if(caseEntity.getStatus() != expected)
            throw new IllegalCaseTransitionException(
                    "Case id=" + caseEntity.getId() + " expected status=" + expected +
                            " but was=" + caseEntity.getStatus());
    }

    private void requireAssignedAnalyst(Case caseEntity, String currentUsername) {
        if(!currentUsername.equals(caseEntity.getAssignedAnalyst()))
            throw new CaseNotAssignedException("Case is not assigned to this analyst");
    }
}

