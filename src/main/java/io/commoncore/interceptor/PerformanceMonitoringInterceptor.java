package io.commoncore.interceptor;

import io.commoncore.config.CommonCoreProperties;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Performance monitoring interceptor
 * Tracks memory, CPU, and execution time metrics
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class PerformanceMonitoringInterceptor implements HandlerInterceptor {

    private final CommonCoreProperties properties;
    private final MeterRegistry meterRegistry;
    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private final RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
    private final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!properties.getMonitoring().isEnabled()) {
            return true;
        }

        long startTime = System.currentTimeMillis();
        request.setAttribute("performanceStartTime", startTime);

        if (properties.getMonitoring().isMonitorMemory()) {
            long initialMemory = getUsedMemory();
            request.setAttribute("initialMemory", initialMemory);
        }

        if (properties.getMonitoring().isMonitorCpu()) {
            long initialCpuTime = threadBean.getCurrentThreadCpuTime();
            request.setAttribute("initialCpuTime", initialCpuTime);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (!properties.getMonitoring().isEnabled()) {
            return;
        }

        long startTime = (Long) request.getAttribute("performanceStartTime");
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        metrics.put("method", request.getMethod());
        metrics.put("uri", request.getRequestURI());
        metrics.put("status", response.getStatus());
        metrics.put("executionTime", executionTime);
        metrics.put("executionTimeUnit", "ms");

        if (properties.getMonitoring().isMonitorMemory()) {
            long initialMemory = (Long) request.getAttribute("initialMemory");
            long finalMemory = getUsedMemory();
            long memoryUsed = finalMemory - initialMemory;
            metrics.put("memoryUsed", memoryUsed);
            metrics.put("memoryUsedUnit", "bytes");
            metrics.put("memoryUsedMB", memoryUsed / (1024.0 * 1024.0));
            metrics.put("totalMemory", getTotalMemory());
            metrics.put("maxMemory", getMaxMemory());
        }

        if (properties.getMonitoring().isMonitorCpu()) {
            long initialCpuTime = (Long) request.getAttribute("initialCpuTime");
            long finalCpuTime = threadBean.getCurrentThreadCpuTime();
            long cpuTimeUsed = finalCpuTime - initialCpuTime;
            metrics.put("cpuTimeUsed", cpuTimeUsed);
            metrics.put("cpuTimeUsedUnit", "ns");
        }

        // Log metrics
        if (properties.getLogging().isStructuredLogging()) {
            logStructuredMetrics(metrics);
        } else {
            logSimpleMetrics(metrics);
        }

        // Record Micrometer metrics
        if (properties.getMonitoring().isEnableMetrics()) {
            recordMicrometerMetrics(request, response, executionTime, metrics);
        }

        // Check for slow queries
        if (executionTime > properties.getMonitoring().getSlowQueryThreshold()) {
            log.warn("Slow request detected: {} {} took {}ms (threshold: {}ms)",
                    request.getMethod(), request.getRequestURI(), executionTime,
                    properties.getMonitoring().getSlowQueryThreshold());
        }
    }

    private void logSimpleMetrics(Map<String, Object> metrics) {
        log.info("Performance metrics - Method: {} URI: {} Status: {} ExecutionTime: {}ms MemoryUsed: {}MB",
                metrics.get("method"), metrics.get("uri"), metrics.get("status"),
                metrics.get("executionTime"), metrics.get("memoryUsedMB"));
    }

    private void logStructuredMetrics(Map<String, Object> metrics) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            log.info(mapper.writeValueAsString(metrics));
        } catch (Exception e) {
            log.warn("Failed to serialize metrics to JSON: {}", e.getMessage());
            logSimpleMetrics(metrics);
        }
    }

    private void recordMicrometerMetrics(HttpServletRequest request, HttpServletResponse response,
                                         long executionTime, Map<String, Object> metrics) {
        try {
            // HTTP Request Timer
            Timer.Sample sample = Timer.start(meterRegistry);
            sample.stop(Timer.builder("http.request.duration")
                    .description("HTTP request duration")
                    .tag("method", request.getMethod())
                    .tag("uri", sanitizeUri(request.getRequestURI()))
                    .tag("status", String.valueOf(response.getStatus()))
                    .register(meterRegistry));

            // HTTP Request Counter
            meterRegistry.counter("http.requests.total",
                    "method", request.getMethod(),
                    "uri", sanitizeUri(request.getRequestURI()),
                    "status", String.valueOf(response.getStatus())
            ).increment();

            // Memory Gauge
            if (properties.getMonitoring().isMonitorMemory() && metrics.containsKey("memoryUsedMB")) {
                Gauge.builder("http.request.memory.used", () -> (Double) metrics.get("memoryUsedMB"))
                        .description("Memory used per request")
                        .tag("method", request.getMethod())
                        .tag("uri", sanitizeUri(request.getRequestURI()))
                        .register(meterRegistry);
            }

            // Execution Time Gauge
            Gauge.builder("http.request.execution.time", () -> executionTime)
                    .description("Request execution time")
                    .tag("method", request.getMethod())
                    .tag("uri", sanitizeUri(request.getRequestURI()))
                    .register(meterRegistry);
        } catch (Exception e) {
            log.warn("Failed to record Micrometer metrics: {}", e.getMessage());
        }
    }

    private long getUsedMemory() {
        return memoryBean.getHeapMemoryUsage().getUsed();
    }

    private long getTotalMemory() {
        return memoryBean.getHeapMemoryUsage().getCommitted();
    }

    private long getMaxMemory() {
        return memoryBean.getHeapMemoryUsage().getMax();
    }


    private String sanitizeUri(String uri) {
        // Replace path variables with placeholder
        return uri.replaceAll("/\\d+", "/{id}")
                .replaceAll("/[a-f0-9-]{36}", "/{uuid}")
                .replaceAll("/[^/]+$", "/{param}");
    }
}
