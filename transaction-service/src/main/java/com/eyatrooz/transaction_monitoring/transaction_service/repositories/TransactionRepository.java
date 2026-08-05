package com.eyatrooz.transaction_monitoring.transaction_service.repositories;

import com.eyatrooz.transaction_monitoring.transaction_service.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccountIdAndCreatedAtAfter(String accountId, Instant since);

}
