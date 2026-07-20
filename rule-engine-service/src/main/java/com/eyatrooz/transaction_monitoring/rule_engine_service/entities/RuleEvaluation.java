package com.eyatrooz.transaction_monitoring.rule_engine_service.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Table(name = "rule_evaluations")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(name = "transaction_id", nullable = false, unique = true)
    private Long transactionId;

    @Column(name = "account_id", nullable = false)
    private String accountId;

    @Column(name = "overall_risk_score", nullable = false)
    private BigDecimal riskScore;

    @Column(name = "flagged", nullable = false)
    private Boolean flagged;

    @Column(name = "evaluated_at", nullable = false)
    private Instant evaluatedAt;

    @OneToMany(mappedBy = "ruleEvaluation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RuleEvaluationResult> results = new ArrayList<>();

    @PrePersist
    private void onCreate(){
        if(this.evaluatedAt == null)
            this.evaluatedAt = Instant.now();
    }

}
