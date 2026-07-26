package com.eyatrooz.transaction_monitoring.case_management_service.repositories;

import com.eyatrooz.transaction_monitoring.case_management_service.entities.CaseHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CaseHistoryRepository extends JpaRepository<CaseHistory, Long> {
}
