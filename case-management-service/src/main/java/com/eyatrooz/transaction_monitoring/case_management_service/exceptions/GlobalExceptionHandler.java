package com.eyatrooz.transaction_monitoring.case_management_service.exceptions;

import com.eyatrooz.transaction_monitoring.case_management_service.dtos.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> details = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fieldError -> details.put(fieldError.getField(), fieldError.getDefaultMessage()));

        ErrorResponse response = new ErrorResponse(
                Instant.now(), HttpStatus.BAD_REQUEST.value(), "Validation failed", details
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(CaseNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCaseNotFound(CaseNotFoundException ex) {
        ErrorResponse response = new ErrorResponse(
                Instant.now(), HttpStatus.NOT_FOUND.value(), ex.getMessage(), Map.of()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(IllegalCaseTransitionException.class)
    public ResponseEntity<ErrorResponse> handleIllegalTransition(IllegalCaseTransitionException ex) {
        ErrorResponse response = new ErrorResponse(
                Instant.now(), HttpStatus.CONFLICT.value(), ex.getMessage(), Map.of()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(CaseNotAssignedException.class)
    public ResponseEntity<Map<String, String>> handleCaseNotAssigned(CaseNotAssignedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Forbidden", "message", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        ErrorResponse response = new ErrorResponse(
                Instant.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error", Map.of()
        );
        return ResponseEntity.internalServerError().body(response);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<?> handleAuthorizationDenied(AuthorizationDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Forbidden", "message", "You do not have permission to perform this action"));
    }
}
