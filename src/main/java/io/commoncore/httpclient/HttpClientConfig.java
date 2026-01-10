package io.commoncore.httpclient;

import io.commoncore.config.CommonCoreProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configuration for HTTP Client (RestTemplate)
 */
@Configuration
@ConditionalOnProperty(name = "commoncore.http-client.enabled", havingValue = "true", matchIfMissing = true)
public class HttpClientConfig {

    @Autowired
    private CommonCoreProperties properties;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        CommonCoreProperties.HttpClient config = properties.getHttpClient();

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(config.getConnectTimeout());
        factory.setReadTimeout(config.getReadTimeout());

        return builder
                .setConnectTimeout(Duration.ofMillis(config.getConnectTimeout()))
                .setReadTimeout(Duration.ofMillis(config.getReadTimeout()))
                .requestFactory(() -> factory)
                .build();
    }
}
