package com.eyatrooz.transaction_monitoring.case_management_service.repositories;

import com.eyatrooz.transaction_monitoring.case_management_service.entities.FlaggedTransactionEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FlaggedTransactionEventRepository extends JpaRepository<FlaggedTransactionEvent, Long> {

    boolean existsByTransactionId(Long transactionId);
}
