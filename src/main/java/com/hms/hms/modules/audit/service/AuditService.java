package com.hms.hms.modules.audit.service;

import com.hms.hms.modules.audit.entity.AuditLog;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface AuditService {

    void log(String action,
             String entity,
             UUID entityId,
             String performedBy,
             String role,
             String description);
    Page<AuditLog> getLogs(int page, int size, String action);
}