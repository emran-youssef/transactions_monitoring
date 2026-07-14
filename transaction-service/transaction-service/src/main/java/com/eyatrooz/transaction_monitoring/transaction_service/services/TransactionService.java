package com.eyatrooz.transaction_monitoring.transaction_service.services;

import com.eyatrooz.transaction_monitoring.transaction_service.dtos.TransactionRequest;
import com.eyatrooz.transaction_monitoring.transaction_service.dtos.TransactionResponse;
import com.eyatrooz.transaction_monitoring.transaction_service.enums.TransactionStatus;
import com.eyatrooz.transaction_monitoring.transaction_service.mappers.TransactionMapper;
import com.eyatrooz.transaction_monitoring.transaction_service.repositories.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;


    public TransactionResponse createTransaction(TransactionRequest request){
        log.info("Transaction Received from accountId={}, amount={}", request.getAccountId(), request.getAmount());

        var transaction = transactionMapper.toEntity(request);
        transaction.setTransactionStatus(TransactionStatus.RECEIVED);

        var saved = transactionRepository.save(transaction);
        log.info("Transaction saved: id={}, accountId={}, status={}", saved.getId(), saved.getAccountId(), saved.getTransactionStatus());

        return transactionMapper.toDto(saved);

    }
}
