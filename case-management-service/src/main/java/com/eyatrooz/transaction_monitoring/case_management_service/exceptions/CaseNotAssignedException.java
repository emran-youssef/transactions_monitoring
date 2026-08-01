package com.eyatrooz.transaction_monitoring.case_management_service.exceptions;

public class CaseNotAssignedException extends RuntimeException {

    public CaseNotAssignedException(String message) {
        super(message);
    }
}
