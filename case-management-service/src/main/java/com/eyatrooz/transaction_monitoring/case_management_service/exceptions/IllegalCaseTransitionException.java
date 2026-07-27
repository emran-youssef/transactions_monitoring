package com.eyatrooz.transaction_monitoring.case_management_service.exceptions;

public class IllegalCaseTransitionException extends RuntimeException {
    public IllegalCaseTransitionException(String message) {
        super(message);
    }
}
