# HTTP Client Kullanım Kılavuzu

CommonCore, farklı servislere HTTP request atmak için hazır bir HTTP Client utility sağlar.

## Özellikler

- ✅ **GET, POST, PUT, DELETE** metodları
- ✅ **Otomatik Error Handling**
- ✅ **Request/Response Logging**
- ✅ **Retry Mekanizması** (opsiyonel)
- ✅ **Timeout Yapılandırması**
- ✅ **Custom Header Desteği**
- ✅ **Path Variable Desteği**

## Kurulum

CommonCore dependency'si zaten projenizde varsa, HTTP Client otomatik olarak kullanılabilir.

## Yapılandırma

`application.properties` dosyanıza ekleyin:

```properties
# HTTP Client Configuration
commoncore.http-client.enabled=true
commoncore.http-client.connect-timeout=5000
commoncore.http-client.read-timeout=10000
commoncore.http-client.enable-logging=true
commoncore.http-client.enable-retry=false
commoncore.http-client.max-retry-attempts=3
commoncore.http-client.retry-delay-ms=1000
```

**Parametreler:**
- `enabled`: HTTP Client'ı aktif/pasif yapar (default: `true`)
- `connect-timeout`: Bağlantı timeout'u milisaniye cinsinden (default: `5000`)
- `read-timeout`: Okuma timeout'u milisaniye cinsinden (default: `10000`)
- `enable-logging`: Request/Response loglamayı aç/kapat (default: `true`)
- `enable-retry`: Retry mekanizmasını aç/kapat (default: `false`)
- `max-retry-attempts`: Maksimum retry denemesi (default: `3`)
- `retry-delay-ms`: Retry arası bekleme süresi milisaniye cinsinden (default: `1000`)

## Kullanım

### 1. Service Sınıfında Inject Etme

```java
import io.commoncore.httpclient.HttpClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExternalServiceClient {
    
    @Autowired
    private HttpClientService httpClient;
    
    // ... metodlar
}
```

### 2. GET Request

```java
// Basit GET request
String response = httpClient.get("https://api.example.com/users", String.class);

// GET request with headers
HttpHeaders headers = new HttpHeaders();
headers.set("Authorization", "Bearer token123");
UserDTO user = httpClient.get("https://api.example.com/users/1", UserDTO.class, headers);

// GET request with path variables
Map<String, String> pathVariables = Map.of("id", "123");
UserDTO user = httpClient.get(
    "https://api.example.com/users/{id}", 
    UserDTO.class, 
    null, 
    pathVariables
);
```

### 3. POST Request

```java
// Basit POST request
CreateUserRequest request = new CreateUserRequest("John", "john@example.com");
UserDTO createdUser = httpClient.post(
    "https://api.example.com/users", 
    request, 
    UserDTO.class
);

// POST request with headers
HttpHeaders headers = new HttpHeaders();
headers.set("Authorization", "Bearer token123");
headers.set("X-Custom-Header", "value");
UserDTO createdUser = httpClient.post(
    "https://api.example.com/users", 
    request, 
    UserDTO.class, 
    headers
);
```

### 4. PUT Request

```java
UpdateUserRequest request = new UpdateUserRequest("Jane", "jane@example.com");
UserDTO updatedUser = httpClient.put(
    "https://api.example.com/users/1", 
    request, 
    UserDTO.class
);
```

### 5. DELETE Request

```java
// DELETE request
httpClient.delete("https://api.example.com/users/1", Void.class);

// DELETE request with headers
HttpHeaders headers = new HttpHeaders();
headers.set("Authorization", "Bearer token123");
httpClient.delete("https://api.example.com/users/1", Void.class, headers);
```

## Tam Örnek

```java
package com.myproject.service;

import com.myproject.dto.UserDTO;
import io.commoncore.httpclient.HttpClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExternalApiService {
    
    private final HttpClientService httpClient;
    
    private static final String API_BASE_URL = "https://api.example.com";
    
    public List<UserDTO> getAllUsers(String authToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);
        
        return httpClient.get(
            API_BASE_URL + "/users", 
            List.class, 
            headers
        );
    }
    
    public UserDTO getUserById(Long id, String authToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);
        
        Map<String, String> pathVariables = Map.of("id", id.toString());
        return httpClient.get(
            API_BASE_URL + "/users/{id}", 
            UserDTO.class, 
            headers, 
            pathVariables
        );
    }
    
    public UserDTO createUser(CreateUserRequest request, String authToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);
        
        return httpClient.post(
            API_BASE_URL + "/users", 
            request, 
            UserDTO.class, 
            headers
        );
    }
    
    public UserDTO updateUser(Long id, UpdateUserRequest request, String authToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);
        
        Map<String, String> pathVariables = Map.of("id", id.toString());
        return httpClient.put(
            API_BASE_URL + "/users/{id}", 
            request, 
            UserDTO.class, 
            headers
        );
    }
    
    public void deleteUser(Long id, String authToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);
        
        Map<String, String> pathVariables = Map.of("id", id.toString());
        httpClient.delete(
            API_BASE_URL + "/users/{id}", 
            Void.class, 
            headers
        );
    }
}
```

## Error Handling

HTTP Client otomatik olarak hataları yakalar ve uygun exception'ları fırlatır:

- **4xx (Client Error)**: `BaseValidationException` fırlatılır
- **5xx (Server Error)**: `RuntimeException` fırlatılır
- **Connection Timeout**: `RuntimeException` fırlatılır

```java
try {
    UserDTO user = httpClient.get("https://api.example.com/users/1", UserDTO.class);
} catch (BaseValidationException e) {
    // 4xx hataları için
    log.error("Client error: {}", e.getMessage());
} catch (RuntimeException e) {
    // 5xx veya connection hataları için
    log.error("Server error: {}", e.getMessage());
}
```

## Retry Mekanizması

Retry mekanizmasını aktif etmek için:

```properties
commoncore.http-client.enable-retry=true
commoncore.http-client.max-retry-attempts=3
commoncore.http-client.retry-delay-ms=1000
```

Retry aktifken, başarısız istekler otomatik olarak belirtilen sayıda tekrar denenir.

## Logging

HTTP Client, varsayılan olarak tüm request ve response'ları loglar:

```
INFO - HTTP GET Request: https://api.example.com/users
DEBUG - Request Body: null
INFO - HTTP GET Response: https://api.example.com/users - Status: 200 OK
DEBUG - Response Body: [{"id":1,"name":"John"}]
```

Logging'i kapatmak için:

```properties
commoncore.http-client.enable-logging=false
```

## Önemli Notlar

1. **Response Type**: Generic type kullanırken (`List<UserDTO>` gibi), TypeReference kullanmanız gerekebilir. Basit tipler için (`String`, `Integer` vb.) direkt kullanabilirsiniz.

2. **Path Variables**: URL'de `{variableName}` formatında path variable kullanabilirsiniz.

3. **Headers**: Her request için custom header'lar ekleyebilirsiniz. `Content-Type: application/json` otomatik olarak eklenir.

4. **Timeout**: Connection ve read timeout'larını yapılandırabilirsiniz.

5. **Error Handling**: Tüm HTTP hataları otomatik olarak yakalanır ve uygun exception'lar fırlatılır.

## Sorun Giderme

### HTTP Client çalışmıyor

- `commoncore.http-client.enabled=true` olduğundan emin olun
- `HttpClientService` bean'inin oluşturulduğunu kontrol edin
- Loglarda HTTP request loglarını kontrol edin

### Timeout hatası alıyorum

- `connect-timeout` ve `read-timeout` değerlerini artırın
- External servisin yanıt süresini kontrol edin

### Retry çalışmıyor

- `enable-retry=true` olduğundan emin olun
- `max-retry-attempts` değerini kontrol edin
