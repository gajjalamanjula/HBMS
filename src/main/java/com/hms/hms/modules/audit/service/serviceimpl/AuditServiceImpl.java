package com.hms.hms.modules.audit.service.serviceimpl;

import com.hms.hms.modules.audit.entity.AuditLog;
import com.hms.hms.modules.audit.repository.AuditLogRepository;
import com.hms.hms.modules.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Override
    public void log(String action,
                    String entity,
                    UUID entityId,
                    String performedBy,
                    String role,
                    String description) {

        AuditLog log = AuditLog.builder()
                .action(action)
                .entityName(entity)
                .entityId(entityId)
                .performedBy(performedBy)
                .role(role)
                .description(description)
                .build();

        auditLogRepository.save(log);
    }

    @Override
    public Page<AuditLog> getLogs(int page, int size, String action) {

        Pageable pageable = PageRequest.of(page, size);

        if (action != null && !action.isEmpty()) {
            return auditLogRepository.findByActionContainingIgnoreCase(action, pageable);
        }

        return auditLogRepository.findAll(pageable);
    }
}