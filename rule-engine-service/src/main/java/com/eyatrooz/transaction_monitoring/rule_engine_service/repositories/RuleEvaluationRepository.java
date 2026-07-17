package com.eyatrooz.transaction_monitoring.rule_engine_service.repositories;

import com.eyatrooz.transaction_monitoring.rule_engine_service.entities.RuleEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RuleEvaluationRepository extends JpaRepository<RuleEvaluation, Long> {
    boolean existsByTransactionId(Long transactionId);
}
