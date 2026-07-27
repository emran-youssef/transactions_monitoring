package com.eyatrooz.transaction_monitoring.case_management_service.services;

import com.eyatrooz.transaction_monitoring.case_management_service.dtos.CaseResponse;
import com.eyatrooz.transaction_monitoring.case_management_service.entities.Case;
import com.eyatrooz.transaction_monitoring.case_management_service.entities.CaseHistory;
import com.eyatrooz.transaction_monitoring.case_management_service.enums.CaseStatus;
import com.eyatrooz.transaction_monitoring.case_management_service.exceptions.CaseNotFoundException;
import com.eyatrooz.transaction_monitoring.case_management_service.exceptions.IllegalCaseTransitionException;
import com.eyatrooz.transaction_monitoring.case_management_service.mappers.CaseHistoryMapper;
import com.eyatrooz.transaction_monitoring.case_management_service.mappers.CaseMapper;
import com.eyatrooz.transaction_monitoring.case_management_service.repositories.CaseRepository;
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


    @Transactional
    public CaseResponse assign(Long caseId, String analyst){
        var fetchedCase = loadCase(caseId);
        requireStatus(fetchedCase, CaseStatus.OPEN);

        fetchedCase.setStatus(CaseStatus.UNDER_REVIEW);
        fetchedCase.addHistory(CaseHistory.assigned(analyst));

        // persist the case, and history persisted by Spring via cascade.All
        var persisted = caseRepository.save(fetchedCase);
        log.info("Case assigned: id={}, analyst={}, status={}", persisted.getId(), analyst, persisted.getStatus());

        return caseMapper.toResponse(persisted);
    }

    @Transactional
    public CaseResponse approve(Long caseId, String analyst){
        var fetchedCase = loadCase(caseId);

        requireStatus(fetchedCase, CaseStatus.UNDER_REVIEW);
        requireAssignedAnalyst(fetchedCase, analyst);

        fetchedCase.setStatus(CaseStatus.APPROVED);
        fetchedCase.addHistory(CaseHistory.approved(analyst));

        // persist the case, and history persisted by Spring via cascade.All
        var persisted = caseRepository.save(fetchedCase);
        log.info("Case approved: id={}, analyst={}, status={}", persisted.getId(), analyst, persisted.getStatus());

        return caseMapper.toResponse(persisted);
    }

    @Transactional
    public CaseResponse escalate(Long caseId, String analyst){
        var fetchedCase = loadCase(caseId);

        requireStatus(fetchedCase, CaseStatus.UNDER_REVIEW);
        requireAssignedAnalyst(fetchedCase, analyst);

        fetchedCase.setStatus(CaseStatus.ESCALATED);
        fetchedCase.addHistory(CaseHistory.escalated(analyst));

        // persist the case, and history persisted by Spring via cascade.All
        var persisted = caseRepository.save(fetchedCase);
        log.info("Case escalated: id={}, analyst={}, status={}", persisted.getId(), analyst, persisted.getStatus());

        return caseMapper.toResponse(persisted);
    }


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

    private void requireAssignedAnalyst(Case caseEntity, String analyst) {
        if(!analyst.equals(caseEntity.getAssignedAnalyst()))
            throw new IllegalCaseTransitionException("Only assigned analyst can perform this action");
    }
}

