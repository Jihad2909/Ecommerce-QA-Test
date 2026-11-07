package com.commercial.project.repositories;

import com.commercial.project.models.AuditLog;
import org.hibernate.boot.models.JpaAnnotations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

}
