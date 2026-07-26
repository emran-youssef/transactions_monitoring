package com.eyatrooz.transaction_monitoring.case_management_service.repositories;

import com.eyatrooz.transaction_monitoring.case_management_service.entities.Case;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CaseRepository extends JpaRepository<Case, Long> {
    Optional<Case> findByTransactionId(Long aLong);
}
