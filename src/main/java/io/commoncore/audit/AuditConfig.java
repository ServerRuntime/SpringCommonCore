package io.commoncore.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.internal.SessionFactoryImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.persistence.EntityManagerFactory;

/**
 * Configuration for Audit Logging
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "commoncore.audit.enabled", havingValue = "true", matchIfMissing = false)
public class AuditConfig {

    private final AuditService auditService;

    /**
     * ObjectMapper bean for JSON serialization
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    /**
     * Register Hibernate event listeners for audit
     */
    @Bean
    @ConditionalOnProperty(name = "commoncore.audit.enable-entity-interceptor", havingValue = "true", matchIfMissing = true)
    public AuditInterceptor auditInterceptor(EntityManagerFactory entityManagerFactory) {
        AuditInterceptor interceptor = new AuditInterceptor(auditService);
        
        if (entityManagerFactory.unwrap(SessionFactoryImpl.class) != null) {
            SessionFactoryImpl sessionFactory = entityManagerFactory.unwrap(SessionFactoryImpl.class);
            EventListenerRegistry registry = sessionFactory.getServiceRegistry()
                    .getService(EventListenerRegistry.class);
            
            registry.getEventListenerGroup(EventType.PRE_INSERT).appendListener(interceptor);
            registry.getEventListenerGroup(EventType.PRE_UPDATE).appendListener(interceptor);
            registry.getEventListenerGroup(EventType.PRE_DELETE).appendListener(interceptor);
        }
        
        return interceptor;
    }
}
