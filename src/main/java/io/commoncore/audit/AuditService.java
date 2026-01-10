package io.commoncore.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for audit logging operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "commoncore.audit.enabled", havingValue = "true", matchIfMissing = false)
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;
    private final AuditContext auditContext;

    /**
     * Log an audit event
     */
    @Transactional
    public void log(AuditAction action, String entityType, String entityId, 
                    Object oldEntity, Object newEntity, Map<String, Object> metadata) {
        try {
            if (!isAuditEnabled()) {
                return;
            }

            AuditLog auditLog = AuditLog.builder()
                    .entityType(entityType)
                    .entityId(entityId)
                    .action(action)
                    .username(auditContext.getCurrentUsername())
                    .userId(auditContext.getCurrentUserId())
                    .ipAddress(auditContext.getCurrentIpAddress())
                    .oldValues(convertToJson(oldEntity))
                    .newValues(convertToJson(newEntity))
                    .changedFields(extractChangedFields(oldEntity, newEntity))
                    .metadata(convertMetadataToJson(metadata))
                    .createdAt(LocalDateTime.now())
                    .build();

            auditLogRepository.save(auditLog);

            if (log.isDebugEnabled()) {
                log.debug("Audit log created: {} {} {} by {}", action, entityType, entityId, 
                        auditContext.getCurrentUsername());
            }
        } catch (Exception e) {
            log.error("Failed to create audit log for {} {}: {}", entityType, entityId, e.getMessage(), e);
            // Don't throw exception to avoid breaking the main operation
        }
    }

    /**
     * Log a simple audit event without entity comparison
     */
    @Transactional
    public void log(AuditAction action, String entityType, String entityId, Map<String, Object> metadata) {
        log(action, entityType, entityId, null, null, metadata);
    }

    /**
     * Log user action
     */
    @Transactional
    public void logUserAction(AuditAction action, String description, Map<String, Object> metadata) {
        Map<String, Object> actionMetadata = new HashMap<>();
        if (metadata != null) {
            actionMetadata.putAll(metadata);
        }
        actionMetadata.put("description", description);
        
        log(action, "USER_ACTION", auditContext.getCurrentUserId() != null ? 
                auditContext.getCurrentUserId() : "ANONYMOUS", null, null, actionMetadata);
    }

    /**
     * Convert object to JSON string
     */
    private String convertToJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("Failed to convert object to JSON: {}", e.getMessage());
            return obj.toString();
        }
    }

    /**
     * Extract changed fields between old and new entity
     */
    private String extractChangedFields(Object oldEntity, Object newEntity) {
        if (oldEntity == null || newEntity == null) {
            return null;
        }
        
        try {
            Map<String, Object> oldMap = objectMapper.convertValue(oldEntity, Map.class);
            Map<String, Object> newMap = objectMapper.convertValue(newEntity, Map.class);
            
            StringBuilder changedFields = new StringBuilder();
            for (String key : newMap.keySet()) {
                Object oldValue = oldMap.get(key);
                Object newValue = newMap.get(key);
                
                if (oldValue == null && newValue != null) {
                    changedFields.append(key).append(",");
                } else if (oldValue != null && !oldValue.equals(newValue)) {
                    changedFields.append(key).append(",");
                }
            }
            
            return changedFields.length() > 0 ? 
                    changedFields.substring(0, changedFields.length() - 1) : null;
        } catch (Exception e) {
            log.warn("Failed to extract changed fields: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Convert metadata map to JSON
     */
    private String convertMetadataToJson(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }
        return convertToJson(metadata);
    }

    /**
     * Check if audit is enabled
     */
    private boolean isAuditEnabled() {
        return true; // Can be enhanced with configuration check
    }
}
