package com.eyatrooz.transaction_monitoring.case_management_service.mappers;

import com.eyatrooz.transaction_monitoring.case_management_service.dtos.CasePayload;
import com.eyatrooz.transaction_monitoring.case_management_service.dtos.CaseResponse;
import com.eyatrooz.transaction_monitoring.case_management_service.entities.Case;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = CaseHistoryMapper.class)
public interface CaseMapper {
    CasePayload toCasePayload(Case newCase);
    CaseResponse toResponse(Case newCase);
}
