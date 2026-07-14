package com.eyatrooz.transaction_monitoring.transaction_service.exceptions;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        ErrorResponse response = new ErrorResponse(
                Instant.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error", Map.of()
        );
        return ResponseEntity.internalServerError().body(response);
    }

}
