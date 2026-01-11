package io.commoncore.interceptor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.commoncore.config.CommonCoreProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Utility class for masking sensitive data in logs
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SensitiveDataMasker {

    private final CommonCoreProperties properties;
    private final ObjectMapper objectMapper;

    /**
     * Mask sensitive data in JSON string
     */
    public String maskSensitiveData(String jsonString) {
        if (jsonString == null || jsonString.isEmpty()) {
            return jsonString;
        }

        try {
            JsonNode jsonNode = objectMapper.readTree(jsonString);
            maskJsonNode(jsonNode);
            return objectMapper.writeValueAsString(jsonNode);
        } catch (Exception e) {
            // If not valid JSON, try regex-based masking
            return maskString(jsonString);
        }
    }

    /**
     * Mask sensitive fields in JSON node recursively
     */
    private void maskJsonNode(JsonNode node) {
        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                String key = entry.getKey().toLowerCase();
                JsonNode value = entry.getValue();

                // Check if field name matches sensitive field patterns
                if (isSensitiveField(key)) {
                    if (value.isTextual()) {
                        ((com.fasterxml.jackson.databind.node.ObjectNode) node)
                                .put(entry.getKey(), properties.getLogging().getMaskPattern());
                    } else if (value.isArray() || value.isObject()) {
                        ((com.fasterxml.jackson.databind.node.ObjectNode) node)
                                .put(entry.getKey(), properties.getLogging().getMaskPattern());
                    }
                } else if (value.isObject() || value.isArray()) {
                    maskJsonNode(value);
                }
            });
        } else if (node.isArray()) {
            node.forEach(this::maskJsonNode);
        }
    }

    /**
     * Mask sensitive data in plain string using regex
     */
    private String maskString(String text) {
        String masked = text;
        for (String sensitiveField : properties.getLogging().getSensitiveFields()) {
            // Pattern: "fieldName": "value" or fieldName=value
            Pattern pattern = Pattern.compile(
                    "(?i)(\"?" + Pattern.quote(sensitiveField) + "\"?\\s*[:=]\\s*\"?)([^\",\\s}]+)(\"?)",
                    Pattern.CASE_INSENSITIVE
            );
            masked = pattern.matcher(masked).replaceAll(
                    "$1" + properties.getLogging().getMaskPattern() + "$3"
            );
        }
        return masked;
    }

    /**
     * Mask sensitive headers
     */
    public Map<String, String> maskHeaders(Map<String, String> headers) {
        Map<String, String> maskedHeaders = new HashMap<>();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String key = entry.getKey().toLowerCase();
            if (isSensitiveField(key)) {
                maskedHeaders.put(entry.getKey(), properties.getLogging().getMaskPattern());
            } else {
                maskedHeaders.put(entry.getKey(), entry.getValue());
            }
        }
        return maskedHeaders;
    }

    /**
     * Check if field name matches sensitive field patterns
     */
    private boolean isSensitiveField(String fieldName) {
        String lowerFieldName = fieldName.toLowerCase();
        return properties.getLogging().getSensitiveFields().stream()
                .anyMatch(sensitive -> lowerFieldName.contains(sensitive.toLowerCase()));
    }
}
