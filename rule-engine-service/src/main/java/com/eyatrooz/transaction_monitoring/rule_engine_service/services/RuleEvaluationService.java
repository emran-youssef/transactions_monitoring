package com.eyatrooz.transaction_monitoring.rule_engine_service.services;

import com.eyatrooz.transaction_monitoring.rule_engine_service.entities.RuleEvaluation;
import com.eyatrooz.transaction_monitoring.rule_engine_service.entities.RuleEvaluationResult;
import com.eyatrooz.transaction_monitoring.rule_engine_service.entities.TransactionHistory;
import com.eyatrooz.transaction_monitoring.rule_engine_service.enums.RuleName;
import com.eyatrooz.transaction_monitoring.rule_engine_service.kafka.FlaggedTransactionPublisher;
import com.eyatrooz.transaction_monitoring.rule_engine_service.repositories.RuleEvaluationRepository;
import com.eyatrooz.transaction_monitoring.rule_engine_service.rules.RuleContext;
import com.eyatrooz.transaction_monitoring.rule_engine_service.rules.RuleExecutor;
import com.eyatrooz.transaction_monitoring.rule_engine_service.rules.RuleExecutorResult;
import com.eyatrooz.transaction_monitoring.rule_engine_service.rules.RuleResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RuleEvaluationService {

    private final RuleExecutor ruleExecutor;
    private final FlaggedTransactionPublisher publisher;
    private final RuleEvaluationRepository ruleEvaluationRepository;

    public void evaluate(TransactionHistory transaction){
        RuleContext context = new RuleContext(transaction, List.of());

        RuleExecutorResult result = ruleExecutor.execute(context);

        // persist the result to ruleEvaluation.
        var ruleEvaluation = toEvaluation(result, transaction);

        ruleEvaluationRepository.save(ruleEvaluation);  // cascades to rule_evaluation_results
        log.info("Persisted rule_evaluation for transactionId={}, flagged={}, score={}",
                transaction.getTransactionId(), result.flagged(),  result.totalScore());

        if(ruleEvaluation.getFlagged()) {
            publisher.publishFlagged(ruleEvaluation);
            log.info("Published flagged event to kafka for transactionId={}", transaction.getTransactionId());
        }
    }

    private RuleEvaluation toEvaluation(RuleExecutorResult result, TransactionHistory transactionHistory) {
        RuleEvaluation evaluation = RuleEvaluation.builder()
                .transactionId(transactionHistory.getTransactionId())
                .accountId(transactionHistory.getAccountId())
                .riskScore(result.totalScore())
                .flagged(result.flagged())
                .build();

        evaluation.setResults(
                result.ruleResults().stream()
                        .map(rule -> toResultsRow(rule, evaluation))
                        .collect(Collectors.toCollection(ArrayList::new))
                );

        return evaluation;
}

private RuleEvaluationResult toResultsRow(RuleResult result, RuleEvaluation parent){
        return RuleEvaluationResult.builder()
                .ruleEvaluation(parent)
                .ruleName(RuleName.valueOf(result.ruleName()))
                .triggered(result.triggered())
                .score(result.riskScore())
                .details(result.details())
                .build();
}

}
