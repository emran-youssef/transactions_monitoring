package com.eyatrooz.transaction_monitoring.case_management_service.dtos;

import com.eyatrooz.transaction_monitoring.case_management_service.enums.CaseHistoryEventType;
import lombok.Data;

import java.time.Instant;

@Data
public class CaseHistoryResponse {
    private CaseHistoryEventType eventType;
    private String analyst;
    private String comment;
    private Instant createdAt;
}
