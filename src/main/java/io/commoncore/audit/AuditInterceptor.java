package io.commoncore.audit;

import jakarta.persistence.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.event.spi.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Hibernate interceptor for automatic audit logging
 * Tracks entity changes (CREATE, UPDATE, DELETE)
 */
@Slf4j
@ConditionalOnProperty(name = "commoncore.audit.enabled", havingValue = "true", matchIfMissing = false)
public class AuditInterceptor implements PreInsertEventListener, PreUpdateEventListener, PreDeleteEventListener {

    private final AuditService auditService;

    public AuditInterceptor(AuditService auditService) {
        this.auditService = auditService;
    }

    @Override
    public boolean onPreInsert(PreInsertEvent event) {
        Object entity = event.getEntity();
        if (isAuditable(entity)) {
            try {
                String entityType = getEntityType(entity);
                String entityId = getEntityId(entity);
                
                Map<String, Object> entityMap = entityToMap(entity);
                auditService.log(AuditAction.CREATE, entityType, entityId, null, entityMap, null);
            } catch (Exception e) {
                log.error("Failed to audit INSERT for {}: {}", entity.getClass().getSimpleName(), e.getMessage(), e);
            }
        }
        return false; // Continue with the operation
    }

    @Override
    public boolean onPreUpdate(PreUpdateEvent event) {
        Object entity = event.getEntity();
        if (isAuditable(entity)) {
            try {
                String entityType = getEntityType(entity);
                String entityId = getEntityId(entity);
                
                // Get old state from database (if available)
                Object oldEntity = getOldEntityState(event);
                Map<String, Object> newEntityMap = entityToMap(entity);
                
                auditService.log(AuditAction.UPDATE, entityType, entityId, oldEntity, newEntityMap, null);
            } catch (Exception e) {
                log.error("Failed to audit UPDATE for {}: {}", entity.getClass().getSimpleName(), e.getMessage(), e);
            }
        }
        return false; // Continue with the operation
    }

    @Override
    public boolean onPreDelete(PreDeleteEvent event) {
        Object entity = event.getEntity();
        if (isAuditable(entity)) {
            try {
                String entityType = getEntityType(entity);
                String entityId = getEntityId(entity);
                
                Map<String, Object> entityMap = entityToMap(entity);
                auditService.log(AuditAction.DELETE, entityType, entityId, entityMap, null, null);
            } catch (Exception e) {
                log.error("Failed to audit DELETE for {}: {}", entity.getClass().getSimpleName(), e.getMessage(), e);
            }
        }
        return false; // Continue with the operation
    }

    /**
     * Check if entity is auditable
     */
    private boolean isAuditable(Object entity) {
        return entity != null && entity.getClass().isAnnotationPresent(Auditable.class);
    }

    /**
     * Get entity type name
     */
    private String getEntityType(Object entity) {
        Auditable auditable = entity.getClass().getAnnotation(Auditable.class);
        if (auditable != null && !auditable.value().isEmpty()) {
            return auditable.value();
        }
        return entity.getClass().getSimpleName();
    }

    /**
     * Get entity ID as string
     */
    private String getEntityId(Object entity) {
        try {
            Field idField = getIdField(entity.getClass());
            if (idField != null) {
                idField.setAccessible(true);
                Object id = idField.get(entity);
                return id != null ? id.toString() : "UNKNOWN";
            }
        } catch (Exception e) {
            log.debug("Could not get entity ID: {}", e.getMessage());
        }
        return "UNKNOWN";
    }

    /**
     * Get ID field from entity class
     */
    private Field getIdField(Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                return field;
            }
        }
        // Check parent classes
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null && superClass != Object.class) {
            return getIdField(superClass);
        }
        return null;
    }

    /**
     * Convert entity to map
     */
    private Map<String, Object> entityToMap(Object entity) {
        Map<String, Object> map = new HashMap<>();
        try {
            Class<?> clazz = entity.getClass();
            while (clazz != null && clazz != Object.class) {
                for (Field field : clazz.getDeclaredFields()) {
                    // Skip JPA-related fields
                    if (field.isAnnotationPresent(Transient.class) || 
                        field.isAnnotationPresent(ManyToOne.class) ||
                        field.isAnnotationPresent(OneToMany.class) ||
                        field.isAnnotationPresent(ManyToMany.class) ||
                        field.isAnnotationPresent(OneToOne.class)) {
                        continue;
                    }
                    
                    field.setAccessible(true);
                    Object value = field.get(entity);
                    if (value != null) {
                        map.put(field.getName(), value);
                    }
                }
                clazz = clazz.getSuperclass();
            }
        } catch (Exception e) {
            log.warn("Failed to convert entity to map: {}", e.getMessage());
        }
        return map;
    }

    /**
     * Get old entity state (simplified - in production, you might want to fetch from DB)
     */
    private Object getOldEntityState(PreUpdateEvent event) {
        // Hibernate provides old state in the event
        // This is a simplified version - you might want to reconstruct the entity
        return null; // Can be enhanced to reconstruct old entity from event.getOldState()
    }
}
