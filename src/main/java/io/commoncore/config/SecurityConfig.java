package io.commoncore.config;

import io.commoncore.security.apikey.ApiKeyAuthenticationFilter;
import io.commoncore.security.jwt.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebSecurity
@ConditionalOnProperty(name = "commoncore.security.enabled", havingValue = "true", matchIfMissing = true)
public class SecurityConfig {
    
    @Autowired
    private CommonCoreProperties properties;
    
    @Autowired(required = false)
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Autowired(required = false)
    private ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;
    
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> {
                // Get all exclude paths
                List<String> excludePaths = getExcludePaths();
                
                // Configure exclude paths
                excludePaths.forEach(path -> auth.requestMatchers(path).permitAll());
                
                // All other requests require authentication if any security is enabled
                boolean anySecurityEnabled = properties.getSecurity().getJwt().isEnabled() ||
                                           properties.getSecurity().getApiKey().isEnabled() ||
                                           properties.getSecurity().getBasicAuth().isEnabled();
                
                if (anySecurityEnabled) {
                    auth.anyRequest().authenticated();
                } else {
                    auth.anyRequest().permitAll();
                }
            });
        
        // Add JWT filter if enabled
        if (properties.getSecurity().getJwt().isEnabled() && jwtAuthenticationFilter != null) {
            http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        }
        
        // Add API Key filter if enabled
        if (properties.getSecurity().getApiKey().isEnabled() && apiKeyAuthenticationFilter != null) {
            http.addFilterBefore(apiKeyAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        }
        
        // Basic Auth configuration
        if (properties.getSecurity().getBasicAuth().isEnabled()) {
            http.httpBasic(httpBasic -> {});
        }
        
        // CORS configuration
        if (properties.getSecurity().getCors().isEnabled()) {
            http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
        }
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        CommonCoreProperties.Security.Cors cors = properties.getSecurity().getCors();
        
        configuration.setAllowedOrigins(cors.getAllowedOrigins());
        configuration.setAllowedMethods(cors.getAllowedMethods());
        configuration.setAllowedHeaders(cors.getAllowedHeaders());
        configuration.setAllowCredentials(cors.isAllowCredentials());
        configuration.setMaxAge(cors.getMaxAge());
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    private List<String> getExcludePaths() {
        List<String> excludePaths = new ArrayList<>();
        
        if (properties.getSecurity().getJwt().isEnabled()) {
            excludePaths.addAll(properties.getSecurity().getJwt().getExcludePaths());
        }
        if (properties.getSecurity().getApiKey().isEnabled()) {
            excludePaths.addAll(properties.getSecurity().getApiKey().getExcludePaths());
        }
        if (properties.getSecurity().getBasicAuth().isEnabled()) {
            excludePaths.addAll(properties.getSecurity().getBasicAuth().getExcludePaths());
        }
        
        // Default exclude paths
        excludePaths.addAll(List.of("/h2-console/**", "/actuator/**", "/error", "/swagger-ui/**", "/v3/api-docs/**"));
        
        return excludePaths;
    }
}
