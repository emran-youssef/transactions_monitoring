package com.eyatrooz.transaction_monitoring.rule_engine_service.repositories;

import com.eyatrooz.transaction_monitoring.rule_engine_service.entities.TransactionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory, Long> {

    List<TransactionHistory> findByAccountIdAndCreatedAtAfter(String accountId, Instant since);

    boolean existsByTransactionId(Long transactionId);
}
