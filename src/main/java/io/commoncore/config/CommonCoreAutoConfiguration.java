package io.commoncore.config;

import io.commoncore.interceptor.AdvancedLoggingInterceptor;
import io.commoncore.interceptor.LoggingInterceptor;
import io.commoncore.interceptor.PerformanceMonitoringInterceptor;
import io.commoncore.interceptor.RateLimitingInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({HandlerInterceptor.class, WebMvcConfigurer.class})
@EnableConfigurationProperties(CommonCoreProperties.class)
@ComponentScan(basePackages = "io.commoncore")
public class CommonCoreAutoConfiguration implements WebMvcConfigurer {

    private final LoggingInterceptor loggingInterceptor;
    private final RateLimitingInterceptor rateLimitingInterceptor;
    private final AdvancedLoggingInterceptor advancedLoggingInterceptor;
    private final PerformanceMonitoringInterceptor performanceMonitoringInterceptor;
    private final CommonCoreProperties properties;

    public CommonCoreAutoConfiguration(LoggingInterceptor loggingInterceptor,
                                      RateLimitingInterceptor rateLimitingInterceptor,
                                      AdvancedLoggingInterceptor advancedLoggingInterceptor,
                                      PerformanceMonitoringInterceptor performanceMonitoringInterceptor,
                                      CommonCoreProperties properties) {
        this.loggingInterceptor = loggingInterceptor;
        this.rateLimitingInterceptor = rateLimitingInterceptor;
        this.advancedLoggingInterceptor = advancedLoggingInterceptor;
        this.performanceMonitoringInterceptor = performanceMonitoringInterceptor;
        this.properties = properties;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Rate Limiting Interceptor (öncelikli - önce çalışmalı)
        if (properties.getRateLimit().isEnabled()) {
            var rateLimitRegistration = registry.addInterceptor(rateLimitingInterceptor);
            
            // Include patterns
            if (!properties.getRateLimit().getIncludePatterns().isEmpty()) {
                rateLimitRegistration.addPathPatterns(
                    properties.getRateLimit().getIncludePatterns().toArray(new String[0])
                );
            }
            
            // Exclude patterns
            if (!properties.getRateLimit().getExcludePatterns().isEmpty()) {
                rateLimitRegistration.excludePathPatterns(
                    properties.getRateLimit().getExcludePatterns().toArray(new String[0])
                );
            }
        }

        // Advanced Logging Interceptor (if enabled)
        if (properties.getLogging().isStructuredLogging() || 
            properties.getLogging().isLogRequestBody() || 
            properties.getLogging().isLogResponseBody()) {
            var advancedLoggingRegistration = registry.addInterceptor(advancedLoggingInterceptor);
            
            // Include patterns
            if (!properties.getInterceptor().getIncludePatterns().isEmpty()) {
                advancedLoggingRegistration.addPathPatterns(
                    properties.getInterceptor().getIncludePatterns().toArray(new String[0])
                );
            }
            
            // Exclude patterns
            if (!properties.getInterceptor().getExcludePatterns().isEmpty()) {
                advancedLoggingRegistration.excludePathPatterns(
                    properties.getInterceptor().getExcludePatterns().toArray(new String[0])
                );
            }
        } else {
            // Standard Logging Interceptor
            if (properties.getInterceptor().isEnabled()) {
                var loggingRegistration = registry.addInterceptor(loggingInterceptor);
                
                // Include patterns
                if (!properties.getInterceptor().getIncludePatterns().isEmpty()) {
                    loggingRegistration.addPathPatterns(
                        properties.getInterceptor().getIncludePatterns().toArray(new String[0])
                    );
                }
                
                // Exclude patterns
                if (!properties.getInterceptor().getExcludePatterns().isEmpty()) {
                    loggingRegistration.excludePathPatterns(
                        properties.getInterceptor().getExcludePatterns().toArray(new String[0])
                    );
                }
            }
        }

        // Performance Monitoring Interceptor
        if (properties.getMonitoring().isEnabled()) {
            var performanceRegistration = registry.addInterceptor(performanceMonitoringInterceptor);
            
            // Include patterns
            if (!properties.getInterceptor().getIncludePatterns().isEmpty()) {
                performanceRegistration.addPathPatterns(
                    properties.getInterceptor().getIncludePatterns().toArray(new String[0])
                );
            }
            
            // Exclude patterns
            if (!properties.getInterceptor().getExcludePatterns().isEmpty()) {
                performanceRegistration.excludePathPatterns(
                    properties.getInterceptor().getExcludePatterns().toArray(new String[0])
                );
            }
        }
    }
}
