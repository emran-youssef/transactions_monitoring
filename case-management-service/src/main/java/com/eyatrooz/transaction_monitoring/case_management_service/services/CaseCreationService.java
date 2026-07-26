package com.eyatrooz.transaction_monitoring.case_management_service.services;

import com.eyatrooz.transaction_monitoring.case_management_service.dtos.TransactionFlaggedPayload;
import com.eyatrooz.transaction_monitoring.case_management_service.entities.Case;
import com.eyatrooz.transaction_monitoring.case_management_service.entities.CaseHistory;
import com.eyatrooz.transaction_monitoring.case_management_service.entities.FlaggedTransactionEvent;
import com.eyatrooz.transaction_monitoring.case_management_service.enums.CaseStatus;
import com.eyatrooz.transaction_monitoring.case_management_service.repositories.CaseRepository;
import com.eyatrooz.transaction_monitoring.case_management_service.repositories.FlaggedTransactionEventRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaseCreationService {

    private final CaseRepository caseRepository;
    private final FlaggedTransactionEventRepository flaggedTransactionEventRepository;

    @Transactional
    public void processFlaggedTransaction(TransactionFlaggedPayload payload){
        Long transactionId = payload.getTransactionId();

        if(transactionExists(transactionId)) {
            log.warn("Flagged Event already recorded for transactionId={}", payload.getTransactionId());
            return;
        }

        var flaggedTransaction = FlaggedTransactionEvent.from(payload);
        flaggedTransactionEventRepository.save(flaggedTransaction);

        if(caseExists(transactionId)){
            log.warn("Case already exists for transactionId={}, skipping creation", transactionId);
            return;
        }

        // creates new case and open a history
        var newCase = Case.from(payload);

        // persist newCase and history via cascade.All
        var saved = caseRepository.save(newCase);
        log.info("Case created: id={}, transactionId={}, status={}", saved.getId(), saved.getTransactionId(), saved.getStatus());
    }

    private boolean transactionExists(Long transactionId){
        return flaggedTransactionEventRepository.existsByTransactionId(transactionId);
    }
    private boolean caseExists(Long transactionId){
        return caseRepository.existsByTransactionId(transactionId);
    }



}
