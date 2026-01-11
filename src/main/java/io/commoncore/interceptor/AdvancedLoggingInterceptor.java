package io.commoncore.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.commoncore.config.CommonCoreProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Advanced logging interceptor with request/response body logging,
 * header logging, and sensitive data masking
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AdvancedLoggingInterceptor implements HandlerInterceptor {

    private final CommonCoreProperties properties;
    private final ObjectMapper objectMapper;
    private final SensitiveDataMasker sensitiveDataMasker;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        long startTime = System.currentTimeMillis();
        request.setAttribute("startTime", startTime);
        request.setAttribute("requestId", UUID.randomUUID().toString());

        if (properties.getLogging().isStructuredLogging()) {
            logStructuredRequest(request);
        } else {
            logSimpleRequest(request);
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        // PostHandle is called after handler execution but before view rendering
        // We'll log response in afterCompletion for complete information
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        long startTime = (Long) request.getAttribute("startTime");
        long endTime = System.currentTimeMillis();
        long executeTime = endTime - startTime;
        String requestId = (String) request.getAttribute("requestId");

        if (properties.getLogging().isStructuredLogging()) {
            logStructuredResponse(request, response, executeTime, requestId, ex);
        } else {
            logSimpleResponse(request, response, executeTime, ex);
        }
    }

    private void logSimpleRequest(HttpServletRequest request) {
        log.info("Incoming request: {} {} from {}", 
                request.getMethod(), request.getRequestURI(), request.getRemoteAddr());
        
        if (properties.getLogging().isLogHeaders()) {
            logHeaders(request);
        }
    }

    private void logStructuredRequest(HttpServletRequest request) {
        Map<String, Object> logData = new HashMap<>();
        logData.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        logData.put("level", "INFO");
        logData.put("type", "request");
        logData.put("requestId", request.getAttribute("requestId"));
        logData.put("method", request.getMethod());
        logData.put("uri", request.getRequestURI());
        logData.put("queryString", request.getQueryString());
        logData.put("remoteAddr", request.getRemoteAddr());
        logData.put("remoteHost", request.getRemoteHost());
        logData.put("userAgent", request.getHeader("User-Agent"));

        if (properties.getLogging().isLogHeaders()) {
            logData.put("headers", getHeaders(request));
        }

        if (properties.getLogging().isLogRequestBody()) {
            String body = getRequestBody(request);
            if (body != null && !body.isEmpty()) {
                logData.put("body", sensitiveDataMasker.maskSensitiveData(body));
            }
        }

        try {
            log.info(objectMapper.writeValueAsString(logData));
        } catch (Exception e) {
            log.warn("Failed to serialize log data to JSON: {}", e.getMessage());
            logSimpleRequest(request);
        }
    }

    private void logSimpleResponse(HttpServletRequest request, HttpServletResponse response, long executeTime, Exception ex) {
        if (ex != null) {
            log.error("Request failed: {} {} - Status: {} - Time: {}ms - Error: {}", 
                    request.getMethod(), request.getRequestURI(), response.getStatus(), executeTime, ex.getMessage());
        } else {
            log.info("Request processed: {} {} - Status: {} - Time: {}ms", 
                    request.getMethod(), request.getRequestURI(), response.getStatus(), executeTime);
        }
    }

    private void logStructuredResponse(HttpServletRequest request, HttpServletResponse response, 
                                       long executeTime, String requestId, Exception ex) {
        Map<String, Object> logData = new HashMap<>();
        logData.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        logData.put("level", ex != null ? "ERROR" : "INFO");
        logData.put("type", "response");
        logData.put("requestId", requestId);
        logData.put("method", request.getMethod());
        logData.put("uri", request.getRequestURI());
        logData.put("status", response.getStatus());
        logData.put("duration", executeTime);
        logData.put("durationUnit", "ms");

        if (properties.getLogging().isLogHeaders()) {
            logData.put("responseHeaders", getResponseHeaders(response));
        }

        if (properties.getLogging().isLogResponseBody()) {
            String body = getResponseBody(response);
            if (body != null && !body.isEmpty()) {
                logData.put("body", sensitiveDataMasker.maskSensitiveData(body));
            }
        }

        if (ex != null) {
            logData.put("error", ex.getMessage());
            logData.put("errorType", ex.getClass().getName());
        }

        try {
            if (ex != null) {
                log.error(objectMapper.writeValueAsString(logData));
            } else {
                log.info(objectMapper.writeValueAsString(logData));
            }
        } catch (Exception e) {
            log.warn("Failed to serialize log data to JSON: {}", e.getMessage());
            logSimpleResponse(request, response, executeTime, ex);
        }
    }

    private void logHeaders(HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();
        Map<String, String> headers = new HashMap<>();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        log.debug("Request headers: {}", sensitiveDataMasker.maskHeaders(headers));
    }

    private Map<String, String> getHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        return sensitiveDataMasker.maskHeaders(headers);
    }

    private Map<String, String> getResponseHeaders(HttpServletResponse response) {
        Map<String, String> headers = new HashMap<>();
        Collection<String> headerNames = response.getHeaderNames();
        for (String headerName : headerNames) {
            headers.put(headerName, response.getHeader(headerName));
        }
        return sensitiveDataMasker.maskHeaders(headers);
    }

    private String getRequestBody(HttpServletRequest request) {
        try {
            if (request instanceof ContentCachingRequestWrapper) {
                byte[] content = ((ContentCachingRequestWrapper) request).getContentAsByteArray();
                if (content.length > properties.getLogging().getMaxBodySize()) {
                    return "[Body too large: " + content.length + " bytes]";
                }
                String contentType = request.getContentType();
                if (contentType != null && properties.getLogging().getLoggableContentTypes().stream()
                        .anyMatch(contentType::contains)) {
                    return new String(content, StandardCharsets.UTF_8);
                }
            }
            return null;
        } catch (Exception e) {
            log.warn("Failed to read request body: {}", e.getMessage());
            return null;
        }
    }

    private String getResponseBody(HttpServletResponse response) {
        try {
            if (response instanceof ContentCachingResponseWrapper) {
                byte[] content = ((ContentCachingResponseWrapper) response).getContentAsByteArray();
                if (content.length > properties.getLogging().getMaxBodySize()) {
                    return "[Body too large: " + content.length + " bytes]";
                }
                String contentType = response.getContentType();
                if (contentType != null && properties.getLogging().getLoggableContentTypes().stream()
                        .anyMatch(contentType::contains)) {
                    return new String(content, StandardCharsets.UTF_8);
                }
            }
            return null;
        } catch (Exception e) {
            log.warn("Failed to read response body: {}", e.getMessage());
            return null;
        }
    }
}
