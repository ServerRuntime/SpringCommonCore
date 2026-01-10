package io.commoncore.audit;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Audit log entity to store change tracking information
 */
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_entity_type", columnList = "entityType"),
    @Index(name = "idx_entity_id", columnList = "entityId"),
    @Index(name = "idx_action", columnList = "action"),
    @Index(name = "idx_created_at", columnList = "createdAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Entity type (e.g., "Score", "User")
     */
    @Column(nullable = false, length = 100)
    private String entityType;

    /**
     * Entity ID
     */
    @Column(nullable = false, length = 100)
    private String entityId;

    /**
     * Action performed (CREATE, UPDATE, DELETE)
     */
    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private AuditAction action;

    /**
     * User who performed the action
     */
    @Column(length = 100)
    private String username;

    /**
     * User ID (if available)
     */
    @Column(length = 100)
    private String userId;

    /**
     * IP address of the user
     */
    @Column(length = 50)
    private String ipAddress;

    /**
     * Old values (JSON format)
     */
    @Column(columnDefinition = "TEXT")
    private String oldValues;

    /**
     * New values (JSON format)
     */
    @Column(columnDefinition = "TEXT")
    private String newValues;

    /**
     * Changed fields (comma-separated)
     */
    @Column(length = 500)
    private String changedFields;

    /**
     * Additional metadata (JSON format)
     */
    @Column(columnDefinition = "TEXT")
    private String metadata;

    /**
     * Timestamp when the action was performed
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
