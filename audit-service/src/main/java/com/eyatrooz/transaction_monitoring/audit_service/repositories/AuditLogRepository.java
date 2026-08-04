package com.eyatrooz.transaction_monitoring.audit_service.repositories;

import com.eyatrooz.transaction_monitoring.audit_service.entity.AuditLogEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AuditLogRepository extends JpaRepository<AuditLogEntry, Long> {

    Optional<AuditLogEntry> findByEventId(String eventId);
    List<AuditLogEntry> findByEntityIdOrderByOccurredAtAsc(String entityId);

    boolean existsByEventId(String eventId);
}
