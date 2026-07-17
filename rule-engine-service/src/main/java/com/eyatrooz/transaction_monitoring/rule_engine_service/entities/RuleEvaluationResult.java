package com.eyatrooz.transaction_monitoring.rule_engine_service.entities;

import com.eyatrooz.transaction_monitoring.rule_engine_service.enums.RuleName;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Table(name = "rule_evaluation_results")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class RuleEvaluationResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_evaluation_id", nullable = false)
    private RuleEvaluation ruleEvaluation;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_name", nullable = false)
    private RuleName ruleName;

    @Column(name = "triggered", nullable = false)
    private Boolean triggered;

    @Column(name = "score", nullable = false)
    private BigDecimal score;

    @Column(name = "details", nullable = false)
    private String details;




}
