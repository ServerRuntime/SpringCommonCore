package io.commoncore.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "commoncore")
public class CommonCoreProperties {

    private Interceptor interceptor = new Interceptor();
    private RateLimit rateLimit = new RateLimit();
    private Security security = new Security();
    private Actuator actuator = new Actuator();
    private HttpClient httpClient = new HttpClient();
    private Pagination pagination = new Pagination();
    private Audit audit = new Audit();
    private Logging logging = new Logging();
    private Monitoring monitoring = new Monitoring();

    @Data
    public static class Interceptor {
        /**
         * Path patterns to include for logging interceptor
         * Default: /api/**
         */
        private List<String> includePatterns = new ArrayList<>(List.of("/api/**"));

        /**
         * Path patterns to exclude from logging interceptor
         * Default: empty
         */
        private List<String> excludePatterns = new ArrayList<>();

        /**
         * Enable/disable logging interceptor
         * Default: true
         */
        private boolean enabled = true;
    }

    @Data
    public static class RateLimit {
        /**
         * Enable/disable rate limiting
         * Default: true
         */
        private boolean enabled = true;

        /**
         * Maximum number of requests allowed
         * Default: 100
         */
        private int maxRequests = 100;

        /**
         * Time window in seconds
         * Default: 60 (1 minute)
         */
        private long windowSizeInSeconds = 60;

        /**
         * Apply rate limit per IP address
         * If false, applies globally
         * Default: true
         */
        private boolean perIp = true;

        /**
         * Path patterns to include for rate limiting
         * Default: /api/**
         */
        private List<String> includePatterns = new ArrayList<>(List.of("/api/**"));

        /**
         * Path patterns to exclude from rate limiting
         * Default: empty
         */
        private List<String> excludePatterns = new ArrayList<>();
    }

    @Data
    public static class Security {
        /**
         * Enable/disable security features
         * Default: true
         */
        private boolean enabled = true;

        private Cors cors = new Cors();
        private Jwt jwt = new Jwt();
        private ApiKey apiKey = new ApiKey();
        private BasicAuth basicAuth = new BasicAuth();

        @Data
        public static class Cors {
            /**
             * Enable/disable CORS
             * Default: true
             */
            private boolean enabled = true;

            /**
             * Allowed origins
             * Default: * (all origins)
             */
            private List<String> allowedOrigins = new ArrayList<>(List.of("*"));

            /**
             * Allowed HTTP methods
             * Default: GET, POST, PUT, DELETE, OPTIONS
             */
            private List<String> allowedMethods = new ArrayList<>(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

            /**
             * Allowed headers
             * Default: * (all headers)
             */
            private List<String> allowedHeaders = new ArrayList<>(List.of("*"));

            /**
             * Allow credentials
             * Default: false
             */
            private boolean allowCredentials = false;

            /**
             * Max age in seconds
             * Default: 3600 (1 hour)
             */
            private long maxAge = 3600;
        }

        @Data
        public static class Jwt {
            /**
             * Enable/disable JWT authentication
             * Default: false
             */
            private boolean enabled = false;

            /**
             * JWT secret key
             * Default: your-secret-key-change-in-production
             */
            private String secret = "your-secret-key-change-in-production";

            /**
             * Token expiration time in milliseconds
             * Default: 86400000 (24 hours)
             */
            private long expirationMs = 86400000;

            /**
             * Header name for JWT token
             * Default: Authorization
             */
            private String headerName = "Authorization";

            /**
             * Token prefix
             * Default: Bearer 
             */
            private String tokenPrefix = "Bearer ";

            /**
             * Path patterns to exclude from JWT authentication
             * Default: empty
             */
            private List<String> excludePaths = new ArrayList<>();
        }

        @Data
        public static class ApiKey {
            /**
             * Enable/disable API Key authentication
             * Default: false
             */
            private boolean enabled = false;

            /**
             * Header name for API Key
             * Default: X-API-Key
             */
            private String headerName = "X-API-Key";

            /**
             * Expected API Key value
             * Default: change-me
             */
            private String apiKeyValue = "change-me";

            /**
             * Path patterns to exclude from API Key authentication
             * Default: empty
             */
            private List<String> excludePaths = new ArrayList<>();
        }

        @Data
        public static class BasicAuth {
            /**
             * Enable/disable Basic Authentication
             * Default: false
             */
            private boolean enabled = false;

            /**
             * Username for Basic Auth
             * Default: admin
             */
            private String username = "admin";

            /**
             * Password for Basic Auth
             * Default: password
             */
            private String password = "password";

            /**
             * Path patterns to exclude from Basic Auth
             * Default: empty
             */
            private List<String> excludePaths = new ArrayList<>();
        }
    }

    @Data
    public static class Actuator {
        /**
         * Enable/disable Actuator
         * Default: true
         */
        private boolean enabled = true;

        /**
         * Exposed endpoints (comma-separated list)
         * Default: health,info
         */
        private List<String> exposedEndpoints = new ArrayList<>(List.of("health", "info"));

        /**
         * Base path for actuator endpoints
         * Default: /actuator
         */
        private String basePath = "/actuator";

        /**
         * Show details for health endpoint
         * Options: never, when-authorized, always
         * Default: never
         */
        private String healthShowDetails = "never";

        /**
         * Include health groups
         * Default: empty
         */
        private List<String> healthGroups = new ArrayList<>();
    }

    @Data
    public static class HttpClient {
        /**
         * Enable/disable HTTP client
         * Default: true
         */
        private boolean enabled = true;

        /**
         * Connection timeout in milliseconds
         * Default: 5000 (5 seconds)
         */
        private int connectTimeout = 5000;

        /**
         * Read timeout in milliseconds
         * Default: 10000 (10 seconds)
         */
        private int readTimeout = 10000;

        /**
         * Enable request/response logging
         * Default: true
         */
        private boolean enableLogging = true;

        /**
         * Enable retry mechanism
         * Default: false
         */
        private boolean enableRetry = false;

        /**
         * Maximum number of retry attempts
         * Default: 3
         */
        private int maxRetryAttempts = 3;

        /**
         * Retry delay in milliseconds
         * Default: 1000 (1 second)
         */
        private long retryDelayMs = 1000;
    }

    @Data
    public static class Pagination {
        /**
         * Enable/disable pagination
         * Default: true
         */
        private boolean enabled = true;

        /**
         * Default page size
         * Default: 10
         */
        private int defaultPageSize = 10;

        /**
         * Maximum page size
         * Default: 100
         */
        private int maxPageSize = 100;

        /**
         * Default page number (0-indexed)
         * Default: 0
         */
        private int defaultPage = 0;
    }

    @Data
    public static class Audit {
        /**
         * Enable/disable audit logging
         * Default: false
         */
        private boolean enabled = false;

        /**
         * Enable entity audit interceptor
         * Default: true
         */
        private boolean enableEntityInterceptor = true;

        /**
         * Enable user action logging
         * Default: true
         */
        private boolean enableUserActionLogging = true;

        /**
         * Enable change tracking
         * Default: true
         */
        private boolean enableChangeTracking = true;

        /**
         * Entity types to audit (empty = audit all @Auditable entities)
         * Default: empty (all)
         */
        private List<String> entityTypes = new ArrayList<>();

        /**
         * Actions to audit (empty = audit all actions)
         * Default: empty (all)
         */
        private List<String> actions = new ArrayList<>();

        /**
         * Retention period in days (0 = keep forever)
         * Default: 90
         */
        private int retentionDays = 90;
    }

    @Data
    public static class Logging {
        /**
         * Enable/disable structured logging (JSON format)
         * Default: false
         */
        private boolean structuredLogging = false;

        /**
         * Enable/disable request body logging
         * Default: false
         */
        private boolean logRequestBody = false;

        /**
         * Enable/disable response body logging
         * Default: false
         */
        private boolean logResponseBody = false;

        /**
         * Enable/disable header logging
         * Default: false
         */
        private boolean logHeaders = false;

        /**
         * Maximum body size to log (in bytes)
         * Default: 10000 (10KB)
         */
        private int maxBodySize = 10000;

        /**
         * Sensitive fields to mask (e.g., password, token, creditCard)
         * Default: password, token, authorization, creditCard, cvv, ssn
         */
        private List<String> sensitiveFields = new ArrayList<>(List.of(
                "password", "token", "authorization", "creditCard", "cvv", "ssn", "secret"
        ));

        /**
         * Mask pattern for sensitive data
         * Default: ****
         */
        private String maskPattern = "****";

        /**
         * Content types to log body for
         * Default: application/json, application/xml
         */
        private List<String> loggableContentTypes = new ArrayList<>(List.of(
                "application/json", "application/xml"
        ));
    }

    @Data
    public static class Monitoring {
        /**
         * Enable/disable performance monitoring
         * Default: true
         */
        private boolean enabled = true;

        /**
         * Enable/disable memory monitoring
         * Default: true
         */
        private boolean monitorMemory = true;

        /**
         * Enable/disable CPU monitoring
         * Default: true
         */
        private boolean monitorCpu = true;

        /**
         * Enable/disable database query time monitoring
         * Default: true
         */
        private boolean monitorDbQueryTime = true;

        /**
         * Slow query threshold in milliseconds
         * Default: 1000 (1 second)
         */
        private long slowQueryThreshold = 1000;

        /**
         * Enable/disable Micrometer metrics
         * Default: true
         */
        private boolean enableMetrics = true;

        /**
         * Enable/disable Prometheus export
         * Default: false
         */
        private boolean enablePrometheus = false;

        /**
         * Custom metrics to track
         * Default: empty
         */
        private List<String> customMetrics = new ArrayList<>();
    }
}
