package com.eyatrooz.transaction_monitoring.case_management_service.mappers;

import com.eyatrooz.transaction_monitoring.case_management_service.dtos.CaseCreatedPayload;
import com.eyatrooz.transaction_monitoring.case_management_service.entities.Case;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CaseMapper {
    CaseCreatedPayload toCasePayload(Case newCase);
}
