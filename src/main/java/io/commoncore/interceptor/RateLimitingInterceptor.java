package io.commoncore.interceptor;

import io.commoncore.config.CommonCoreProperties;
import io.commoncore.exception.RateLimitExceededException;
import io.commoncore.ratelimit.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class RateLimitingInterceptor implements HandlerInterceptor {

    private final RateLimiter rateLimiter;
    private final CommonCoreProperties.RateLimit rateLimitConfig;

    public RateLimitingInterceptor(CommonCoreProperties properties) {
        this.rateLimitConfig = properties.getRateLimit();
        this.rateLimiter = new RateLimiter(
            rateLimitConfig.getMaxRequests(),
            rateLimitConfig.getWindowSizeInSeconds()
        );
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!rateLimitConfig.isEnabled()) {
            return true;
        }

        String key = getRateLimitKey(request);
        
        if (!rateLimiter.tryAcquire(key)) {
            long retryAfter = rateLimiter.getRetryAfterSeconds(key);
            log.warn("Rate limit exceeded for key: {} - Retry after: {} seconds", key, retryAfter);
            
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", String.valueOf(retryAfter));
            response.setHeader("X-RateLimit-Limit", String.valueOf(rateLimitConfig.getMaxRequests()));
            response.setHeader("X-RateLimit-Window", String.valueOf(rateLimitConfig.getWindowSizeInSeconds()));
            
            throw new RateLimitExceededException(
                "Rate limit exceeded. Maximum " + rateLimitConfig.getMaxRequests() + 
                " requests per " + rateLimitConfig.getWindowSizeInSeconds() + " seconds",
                retryAfter
            );
        }

        // Set rate limit headers
        response.setHeader("X-RateLimit-Limit", String.valueOf(rateLimitConfig.getMaxRequests()));
        response.setHeader("X-RateLimit-Window", String.valueOf(rateLimitConfig.getWindowSizeInSeconds()));
        
        return true;
    }

    private String getRateLimitKey(HttpServletRequest request) {
        if (rateLimitConfig.isPerIp()) {
            return getClientIpAddress(request);
        }
        return "global";
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
