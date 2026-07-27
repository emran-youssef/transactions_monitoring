package com.eyatrooz.transaction_monitoring.case_management_service.controllers;

import com.eyatrooz.transaction_monitoring.case_management_service.dtos.CaseActionRequest;
import com.eyatrooz.transaction_monitoring.case_management_service.dtos.CaseResponse;
import com.eyatrooz.transaction_monitoring.case_management_service.services.CaseWorkflowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/cases")
@RequiredArgsConstructor
public class CaseActionController {

    private final CaseWorkflowService caseWorkflowService;

    @PutMapping("/{id}/assign")
    public ResponseEntity<CaseResponse> assign(@PathVariable Long id, @Valid @RequestBody CaseActionRequest request) {
        var response = caseWorkflowService.assign(id, request.getAnalyst());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<CaseResponse> approve(@PathVariable Long id, @Valid @RequestBody CaseActionRequest request) {
        var response = caseWorkflowService.approve(id, request.getAnalyst(), request.getExplanation());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/escalate")
    public ResponseEntity<CaseResponse> escalate(@PathVariable Long id, @Valid @RequestBody CaseActionRequest request) {
        var response = caseWorkflowService.escalate(id, request.getAnalyst(), request.getExplanation());
        return ResponseEntity.ok(response);
    }
}
