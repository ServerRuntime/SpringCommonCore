package io.commoncore.monitoring;

import io.commoncore.config.CommonCoreProperties;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.config.MeterFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration for Micrometer metrics
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "commoncore.monitoring.enable-metrics", havingValue = "true", matchIfMissing = true)
public class MetricsConfig {

    private final CommonCoreProperties properties;

    /**
     * Enable @Timed annotation support
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    /**
     * Configure common tags for all metrics
     */
    @Bean
    public MeterFilter commonTagsMeterFilter() {
        List<Tag> tags = Arrays.asList(
                Tag.of("application", "commoncore"),
                Tag.of("environment", System.getProperty("spring.profiles.active", "default"))
        );
        return MeterFilter.commonTags(tags);
    }

    /**
     * Custom metrics service
     * MeterRegistry is injected as parameter to avoid circular dependency
     */
    @Bean
    public CustomMetricsService customMetricsService(MeterRegistry meterRegistry) {
        return new CustomMetricsService(meterRegistry, properties);
    }
}
