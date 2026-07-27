package com.eyatrooz.transaction_monitoring.case_management_service.mappers;

import com.eyatrooz.transaction_monitoring.case_management_service.dtos.CaseHistoryResponse;
import com.eyatrooz.transaction_monitoring.case_management_service.entities.CaseHistory;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CaseHistoryMapper {
    CaseHistoryResponse toDto(CaseHistory history);
}
