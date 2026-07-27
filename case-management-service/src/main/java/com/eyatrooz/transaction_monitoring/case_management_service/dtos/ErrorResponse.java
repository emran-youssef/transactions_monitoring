package com.eyatrooz.transaction_monitoring.case_management_service.dtos;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        Map<String, String> details
) {}
