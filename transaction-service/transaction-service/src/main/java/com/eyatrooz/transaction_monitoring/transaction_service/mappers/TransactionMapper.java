package com.eyatrooz.transaction_monitoring.transaction_service.mappers;

import com.eyatrooz.transaction_monitoring.transaction_service.dtos.TransactionRequest;
import com.eyatrooz.transaction_monitoring.transaction_service.dtos.TransactionResponse;
import com.eyatrooz.transaction_monitoring.transaction_service.entities.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    TransactionResponse toDto(Transaction transaction);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "transactionStatus", ignore = true)
    @Mapping(target = "receivedAt", ignore = true)
    Transaction toEntity(TransactionRequest request);
}
