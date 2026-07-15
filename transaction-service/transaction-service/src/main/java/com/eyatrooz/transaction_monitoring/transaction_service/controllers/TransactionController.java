package com.eyatrooz.transaction_monitoring.transaction_service.controllers;

import com.eyatrooz.transaction_monitoring.transaction_service.dtos.TransactionRequest;
import com.eyatrooz.transaction_monitoring.transaction_service.dtos.TransactionResponse;
import com.eyatrooz.transaction_monitoring.transaction_service.services.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/create")
    public ResponseEntity<TransactionResponse> create(@Valid @RequestBody TransactionRequest request){
        var response = transactionService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }
}
