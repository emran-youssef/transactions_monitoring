package com.eyatrooz.transaction_monitoring.case_management_service.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CaseActionRequest {

    @NotBlank
    private String analyst;
}
