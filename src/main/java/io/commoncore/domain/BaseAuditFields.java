package io.commoncore.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Embedded audit fields for entities
 * Tüm entity'lerde ortak olan created/updated alanlarını içerir
 * 
 * Kullanım:
 * @Entity
 * public class MyEntity {
 *     @Embedded
 *     private BaseAuditFields auditFields = new BaseAuditFields();
 *     
 *     @PrePersist
 *     protected void onCreate() {
 *         auditFields.initialize();
 *     }
 *     
 *     @PreUpdate
 *     protected void onUpdate() {
 *         auditFields.update();
 *     }
 * }
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseAuditFields {
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by", length = 100)
    private String createdBy;
    
    @Column(name = "updated_by", length = 100)
    private String updatedBy;
    
    /**
     * Entity oluşturulduğunda çağrılır
     * Otomatik olarak createdAt ve updatedAt set edilir
     */
    public void initialize() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }
    
    /**
     * Entity güncellendiğinde çağrılır
     * Otomatik olarak updatedAt güncellenir
     */
    public void update() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Convenience method to set created by user
     */
    public void setCreatedByUser(String username) {
        this.createdBy = username;
    }
    
    /**
     * Convenience method to set updated by user
     */
    public void setUpdatedByUser(String username) {
        this.updatedBy = username;
    }
}
