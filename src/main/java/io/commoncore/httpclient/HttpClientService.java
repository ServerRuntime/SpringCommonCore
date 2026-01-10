package io.commoncore.httpclient;

import io.commoncore.config.CommonCoreProperties;
import io.commoncore.exception.BaseValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * HTTP Client Service for making HTTP requests to external services
 * Supports GET, POST, PUT, DELETE methods with automatic error handling and logging
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "commoncore.http-client.enabled", havingValue = "true", matchIfMissing = true)
public class HttpClientService {

    private final RestTemplate restTemplate;
    private final CommonCoreProperties.HttpClient config;

    @Autowired
    public HttpClientService(RestTemplate restTemplate, CommonCoreProperties properties) {
        this.restTemplate = restTemplate;
        this.config = properties.getHttpClient();
    }

    /**
     * GET request
     */
    public <T> T get(String url, Class<T> responseType) {
        return get(url, responseType, null, null);
    }

    /**
     * GET request with headers
     */
    public <T> T get(String url, Class<T> responseType, HttpHeaders headers) {
        return get(url, responseType, headers, null);
    }

    /**
     * GET request with headers and path variables
     */
    public <T> T get(String url, Class<T> responseType, HttpHeaders headers, Map<String, String> pathVariables) {
        return executeRequest(HttpMethod.GET, url, null, responseType, headers, pathVariables);
    }

    /**
     * POST request
     */
    public <T> T post(String url, Object requestBody, Class<T> responseType) {
        return post(url, requestBody, responseType, null);
    }

    /**
     * POST request with headers
     */
    public <T> T post(String url, Object requestBody, Class<T> responseType, HttpHeaders headers) {
        return executeRequest(HttpMethod.POST, url, requestBody, responseType, headers, null);
    }

    /**
     * PUT request
     */
    public <T> T put(String url, Object requestBody, Class<T> responseType) {
        return put(url, requestBody, responseType, null);
    }

    /**
     * PUT request with headers
     */
    public <T> T put(String url, Object requestBody, Class<T> responseType, HttpHeaders headers) {
        return executeRequest(HttpMethod.PUT, url, requestBody, responseType, headers, null);
    }

    /**
     * DELETE request
     */
    public <T> T delete(String url, Class<T> responseType) {
        return delete(url, responseType, null);
    }

    /**
     * DELETE request with headers
     */
    public <T> T delete(String url, Class<T> responseType, HttpHeaders headers) {
        return executeRequest(HttpMethod.DELETE, url, null, responseType, headers, null);
    }

    /**
     * Execute HTTP request with retry mechanism
     */
    private <T> T executeRequest(HttpMethod method, String url, Object requestBody,
                                 Class<T> responseType, HttpHeaders headers,
                                 Map<String, String> pathVariables) {
        int attempts = 0;
        Exception lastException = null;

        while (attempts <= (config.isEnableRetry() ? config.getMaxRetryAttempts() : 0)) {
            try {
                return executeRequestInternal(method, url, requestBody, responseType, headers, pathVariables);
            } catch (Exception e) {
                lastException = e;
                attempts++;

                if (config.isEnableRetry() && attempts <= config.getMaxRetryAttempts()) {
                    log.warn("Request failed (attempt {}/{}), retrying in {}ms: {}", 
                            attempts, config.getMaxRetryAttempts(), config.getRetryDelayMs(), e.getMessage());
                    try {
                        Thread.sleep(config.getRetryDelayMs());
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Request interrupted", ie);
                    }
                } else {
                    break;
                }
            }
        }

        // If we get here, all retries failed
        handleException(lastException, method, url);
        return null; // This will never be reached, but needed for compilation
    }

    /**
     * Execute HTTP request internally
     */
    private <T> T executeRequestInternal(HttpMethod method, String url, Object requestBody,
                                        Class<T> responseType, HttpHeaders headers,
                                        Map<String, String> pathVariables) {
        if (config.isEnableLogging()) {
            log.info("HTTP {} Request: {}", method, url);
            if (requestBody != null) {
                log.debug("Request Body: {}", requestBody);
            }
        }

        // Prepare headers
        HttpHeaders requestHeaders = prepareHeaders(headers);

        // Create request entity
        HttpEntity<?> requestEntity = new HttpEntity<>(requestBody, requestHeaders);

        // Build URL with path variables if provided
        String finalUrl = buildUrl(url, pathVariables);

        try {
            ResponseEntity<T> response = restTemplate.exchange(
                    finalUrl,
                    method,
                    requestEntity,
                    responseType
            );

            if (config.isEnableLogging()) {
                log.info("HTTP {} Response: {} - Status: {}", method, url, response.getStatusCode());
                log.debug("Response Body: {}", response.getBody());
            }

            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("HTTP Client Error {}: {} - Response: {}", e.getStatusCode(), url, e.getResponseBodyAsString());
            throw new BaseValidationException(
                    String.format("HTTP Client Error: %s - %s", e.getStatusCode(), e.getResponseBodyAsString())
            );
        } catch (HttpServerErrorException e) {
            log.error("HTTP Server Error {}: {} - Response: {}", e.getStatusCode(), url, e.getResponseBodyAsString());
            throw new RuntimeException(
                    String.format("HTTP Server Error: %s - %s", e.getStatusCode(), e.getResponseBodyAsString()),
                    e
            );
        } catch (ResourceAccessException e) {
            log.error("Resource Access Error: {} - {}", url, e.getMessage());
            throw new RuntimeException(
                    String.format("Resource Access Error: %s - %s", url, e.getMessage()),
                    e
            );
        }
    }

    /**
     * Prepare HTTP headers
     */
    private HttpHeaders prepareHeaders(HttpHeaders customHeaders) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (customHeaders != null) {
            headers.putAll(customHeaders);
        }

        return headers;
    }

    /**
     * Build URL with path variables
     */
    private String buildUrl(String url, Map<String, String> pathVariables) {
        if (pathVariables == null || pathVariables.isEmpty()) {
            return url;
        }

        String finalUrl = url;
        for (Map.Entry<String, String> entry : pathVariables.entrySet()) {
            finalUrl = finalUrl.replace("{" + entry.getKey() + "}", entry.getValue());
        }

        return finalUrl;
    }

    /**
     * Handle exception after retries
     */
    private void handleException(Exception e, HttpMethod method, String url) {
        if (e instanceof HttpClientErrorException) {
            HttpClientErrorException httpException = (HttpClientErrorException) e;
            throw new BaseValidationException(
                    String.format("HTTP Client Error: %s %s - %s", method, url, httpException.getResponseBodyAsString())
            );
        } else if (e instanceof HttpServerErrorException) {
            HttpServerErrorException httpException = (HttpServerErrorException) e;
            throw new RuntimeException(
                    String.format("HTTP Server Error: %s %s - %s", method, url, httpException.getResponseBodyAsString()),
                    e
            );
        } else {
            throw new RuntimeException(
                    String.format("Request failed: %s %s - %s", method, url, e.getMessage()),
                    e
            );
        }
    }
}
