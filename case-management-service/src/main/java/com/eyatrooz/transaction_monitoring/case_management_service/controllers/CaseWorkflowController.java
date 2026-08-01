package com.eyatrooz.transaction_monitoring.case_management_service.controllers;

import com.eyatrooz.transaction_monitoring.case_management_service.dtos.CaseActionRequest;
import com.eyatrooz.transaction_monitoring.case_management_service.dtos.CaseResponse;
import com.eyatrooz.transaction_monitoring.case_management_service.services.CaseWorkflowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;



@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cases")
public class CaseWorkflowController {

    private final CaseWorkflowService caseWorkflowService;

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CaseResponse> assign(@PathVariable Long id, @Valid @RequestBody CaseActionRequest request) {
        var response = caseWorkflowService.assign(id, request.getAnalyst());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ANALYST')")
    public ResponseEntity<CaseResponse> approve(@PathVariable Long id, @Valid @RequestBody CaseActionRequest request) {
        var response = caseWorkflowService.approve(id, currentUsername(), request.getExplanation());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/escalate")
    @PreAuthorize("hasRole('ANALYST')")
    public ResponseEntity<CaseResponse> escalate(@PathVariable Long id, @Valid @RequestBody CaseActionRequest request) {
        var response = caseWorkflowService.escalate(id, currentUsername(), request.getExplanation());
        return ResponseEntity.ok(response);

    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    public ResponseEntity<CaseResponse> fetchCase(@PathVariable Long id){
        return ResponseEntity.ok(caseWorkflowService.fetchCase(id));
    }

    private String currentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
