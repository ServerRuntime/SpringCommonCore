package io.commoncore.monitoring;

import io.commoncore.config.CommonCoreProperties;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Service for custom business metrics
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CustomMetricsService {

    private final MeterRegistry meterRegistry;
    private final CommonCoreProperties properties;
    private final ConcurrentHashMap<String, Counter> counters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Timer> timers = new ConcurrentHashMap<>();

    /**
     * Increment a counter metric
     */
    public void incrementCounter(String name, String... tags) {
        if (!properties.getMonitoring().isEnableMetrics()) {
            return;
        }

        String key = name + String.join("", tags);
        Counter counter = counters.computeIfAbsent(key, k ->
                Counter.builder(name)
                        .description("Custom counter: " + name)
                        .tags(tags)
                        .register(meterRegistry)
        );
        counter.increment();
    }

    /**
     * Record a timer metric
     */
    public void recordTimer(String name, long duration, TimeUnit unit, String... tags) {
        if (!properties.getMonitoring().isEnableMetrics()) {
            return;
        }

        String key = name + String.join("", tags);
        Timer timer = timers.computeIfAbsent(key, k ->
                Timer.builder(name)
                        .description("Custom timer: " + name)
                        .tags(tags)
                        .register(meterRegistry)
        );
        timer.record(duration, unit);
    }

    /**
     * Record a gauge metric
     */
    public void recordGauge(String name, double value, String... tags) {
        if (!properties.getMonitoring().isEnableMetrics()) {
            return;
        }

        Gauge.builder(name, () -> value)
                .description("Custom gauge: " + name)
                .tags(tags)
                .register(meterRegistry);
    }

    /**
     * Record business-specific metrics
     */
    public void recordBusinessMetric(String metricName, double value, String... tags) {
        recordGauge("business." + metricName, value, tags);
    }
}
