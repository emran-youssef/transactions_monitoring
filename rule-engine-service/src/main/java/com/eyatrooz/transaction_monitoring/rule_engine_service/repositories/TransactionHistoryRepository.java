package com.eyatrooz.transaction_monitoring.rule_engine_service.repositories;

import com.eyatrooz.transaction_monitoring.rule_engine_service.entities.TransactionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory, Long> {

    boolean existsByTransactionId(Long transactionId);
    List<TransactionHistory> findByAccountIdAndCreatedAtAfter(String accountId, Instant since);
    Optional<TransactionHistory>findByTransactionId(Long id);
}
