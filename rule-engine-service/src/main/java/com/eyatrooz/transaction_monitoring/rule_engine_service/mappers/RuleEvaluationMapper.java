package com.eyatrooz.transaction_monitoring.rule_engine_service.mappers;

import com.eyatrooz.transaction_monitoring.rule_engine_service.dtos.TransactionFlaggedPayload;
import com.eyatrooz.transaction_monitoring.rule_engine_service.entities.RuleEvaluation;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RuleEvaluationMapper {
    TransactionFlaggedPayload toFlaggedPayload(RuleEvaluation evaluation);
}
