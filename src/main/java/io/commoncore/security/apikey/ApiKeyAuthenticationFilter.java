package io.commoncore.security.apikey;

import io.commoncore.config.CommonCoreProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@ConditionalOnProperty(name = "commoncore.security.api-key.enabled", havingValue = "true")
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private CommonCoreProperties properties;
    
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String requestPath = request.getRequestURI();
        List<String> excludePaths = properties.getSecurity().getApiKey().getExcludePaths();
        
        // Check if path should be excluded
        boolean shouldExclude = excludePaths.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, requestPath));
        
        if (shouldExclude) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String headerName = properties.getSecurity().getApiKey().getHeaderName();
        String expectedApiKey = properties.getSecurity().getApiKey().getApiKeyValue();
        String apiKey = request.getHeader(headerName);
        
        if (apiKey == null || !apiKey.equals(expectedApiKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Invalid or missing API key\"}");
            return;
        }
        
        filterChain.doFilter(request, response);
    }
}
