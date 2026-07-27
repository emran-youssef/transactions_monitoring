package com.eyatrooz.transaction_monitoring.case_management_service.entities;

import com.eyatrooz.transaction_monitoring.case_management_service.enums.CaseHistoryEventType;
import com.eyatrooz.transaction_monitoring.case_management_service.enums.CaseStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "case_history")
public class CaseHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    private Case caseEntity;

    @Column(name = "event_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private CaseHistoryEventType eventType;

    @Column(name = "analyst")
    private String analyst;

    @Column(name = "comment", length = 512)
    private String comment;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }


    public static CaseHistory opened(Long transactionId){
        return CaseHistory.builder()
                .eventType(CaseHistoryEventType.CASE_CREATED)
                .comment("Case created for flagged transaction, transactionId=" + transactionId)
                .build();
    }

    public static CaseHistory assigned(String analyst){
        return CaseHistory.builder()
                .eventType(CaseHistoryEventType.CASE_ASSIGNED)
                .analyst(analyst)
                .comment("Case assigned to analyst: "+analyst)
                .build();

    }

    public static CaseHistory approved(String analyst, String explanation){
        return CaseHistory.builder()
                .eventType(CaseHistoryEventType.CASE_APPROVED)
                .analyst(analyst)
                .comment(explanation)
                .build();
    }

    public static CaseHistory escalated(String analyst, String explanation){
        return CaseHistory.builder()
                .eventType(CaseHistoryEventType.CASE_ESCALATED)
                .analyst(analyst)
                .comment(explanation)
                .build();
    }


}
