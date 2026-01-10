package io.commoncore.audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * Context holder for audit information (current user, IP address, etc.)
 */
@Slf4j
@Component
public class AuditContext {

    private static final ThreadLocal<String> currentUsername = new ThreadLocal<>();
    private static final ThreadLocal<String> currentUserId = new ThreadLocal<>();
    private static final ThreadLocal<String> currentIpAddress = new ThreadLocal<>();

    /**
     * Get current username from context
     */
    public String getCurrentUsername() {
        String username = currentUsername.get();
        if (username != null) {
            return username;
        }
        
        // Try to get from Spring Security context
        try {
            org.springframework.security.core.context.SecurityContext securityContext = 
                    org.springframework.security.core.context.SecurityContextHolder.getContext();
            if (securityContext != null && securityContext.getAuthentication() != null) {
                Object principal = securityContext.getAuthentication().getPrincipal();
                if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                    return ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
                }
                return principal.toString();
            }
        } catch (Exception e) {
            log.debug("Could not get username from SecurityContext: {}", e.getMessage());
        }
        
        return "SYSTEM";
    }

    /**
     * Get current user ID from context
     */
    public String getCurrentUserId() {
        String userId = currentUserId.get();
        if (userId != null) {
            return userId;
        }
        
        // Try to extract from JWT or other authentication mechanism
        // This can be enhanced based on your authentication setup
        return null;
    }

    /**
     * Get current IP address from request
     */
    public String getCurrentIpAddress() {
        String ip = currentIpAddress.get();
        if (ip != null) {
            return ip;
        }
        
        try {
            ServletRequestAttributes attributes = 
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return getClientIpAddress(request);
            }
        } catch (Exception e) {
            log.debug("Could not get IP address from request: {}", e.getMessage());
        }
        
        return "UNKNOWN";
    }

    /**
     * Set current username (for testing or manual setting)
     */
    public void setCurrentUsername(String username) {
        currentUsername.set(username);
    }

    /**
     * Set current user ID (for testing or manual setting)
     */
    public void setCurrentUserId(String userId) {
        currentUserId.set(userId);
    }

    /**
     * Set current IP address (for testing or manual setting)
     */
    public void setCurrentIpAddress(String ipAddress) {
        currentIpAddress.set(ipAddress);
    }

    /**
     * Clear thread local variables
     */
    public void clear() {
        currentUsername.remove();
        currentUserId.remove();
        currentIpAddress.remove();
    }

    /**
     * Extract client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // Handle multiple IPs (X-Forwarded-For can contain multiple IPs)
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
}
