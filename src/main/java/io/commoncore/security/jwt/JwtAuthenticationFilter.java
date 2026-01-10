package io.commoncore.security.jwt;

import io.commoncore.config.CommonCoreProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@ConditionalOnProperty(name = "commoncore.security.jwt.enabled", havingValue = "true")
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private CommonCoreProperties properties;
    
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String requestPath = request.getRequestURI();
        List<String> excludePaths = new ArrayList<>(properties.getSecurity().getJwt().getExcludePaths());
        
        // Add default exclude paths (same as SecurityConfig)
        excludePaths.addAll(List.of("/h2-console/**", "/actuator/**", "/error", "/swagger-ui/**", "/v3/api-docs/**"));
        
        // Check if path should be excluded
        boolean shouldExclude = excludePaths.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, requestPath));
        
        if (shouldExclude) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String headerName = properties.getSecurity().getJwt().getHeaderName();
        String tokenPrefix = properties.getSecurity().getJwt().getTokenPrefix();
        String authHeader = request.getHeader(headerName);
        
        if (authHeader != null && authHeader.startsWith(tokenPrefix)) {
            String token = authHeader.substring(tokenPrefix.length());
            
            try {
                if (jwtUtil.validateToken(token)) {
                    String username = jwtUtil.extractUsername(token);
                    
                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        UsernamePasswordAuthenticationToken authToken = 
                            new UsernamePasswordAuthenticationToken(
                                username, null, 
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                            );
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Invalid or expired token\"}");
                return;
            }
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Missing or invalid authorization header\"}");
            return;
        }
        
        filterChain.doFilter(request, response);
    }
}
