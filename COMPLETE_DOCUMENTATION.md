# CommonCore - Kapsamlı Dokümantasyon

## İçindekiler

1. [Genel Bakış](#genel-bakış)
2. [Kurulum](#kurulum)
3. [Yeni Projede Kullanım ve Özelleştirme](#yeni-projede-kullanım-ve-özelleştirme)
4. [Özellikler](#özellikler)
   - [CustomResponse](#customresponse)
   - [Exception Handling](#exception-handling)
   - [Validation](#validation)
   - [Logging Interceptor](#logging-interceptor)
   - [Rate Limiting](#rate-limiting)
   - [Security & Authentication](#security--authentication)
     - [JWT Authentication](#jwt-authentication)
     - [API Key Authentication](#api-key-authentication)
     - [Basic Authentication](#basic-authentication)
     - [CORS Configuration](#cors-configuration)
   - [Spring Actuator](#spring-actuator)
4. [Yapılandırma Parametreleri](#yapılandırma-parametreleri)
5. [Kullanım Örnekleri](#kullanım-örnekleri)
6. [Proje Yapısı](#proje-yapısı)
7. [Sorun Giderme](#sorun-giderme)

---

## Genel Bakış

**CommonCore**, Spring Boot projelerinde kullanılabilecek ortak bileşenleri içeren bir kütüphanedir. Otomatik yapılandırma (auto-configuration) ile çalışır ve minimum yapılandırma gerektirir.

### Temel Özellikler

- ✅ **Generic API Response Wrapper** (`CustomResponse`)
- ✅ **Global Exception Handling** (Otomatik exception yakalama ve formatlama)
- ✅ **Request/Response Logging** (Interceptor tabanlı)
- ✅ **Rate Limiting** (IP bazlı veya global)
- ✅ **Security & Authentication** (JWT, API Key, Basic Auth)
- ✅ **CORS Configuration** (Yapılandırılabilir CORS ayarları)
- ✅ **Bean Validation** (Jakarta Validation desteği)
- ✅ **Custom Validators** (`@StrongPassword` gibi)
- ✅ **Spring Actuator** (Monitoring ve health check)

---

## Kurulum

### 1. Maven Dependency Ekleme

Projenizin `pom.xml` dosyasına CommonCore dependency'sini ekleyin:

```xml
<dependencies>
    <!-- Spring Boot Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Spring Boot Validation (Bean Validation için) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    
    <!-- Spring Boot Security (Security özellikleri için) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    
    <!-- Spring Boot Actuator (Actuator özellikleri için) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    
    <!-- CommonCore -->
    <dependency>
        <groupId>io.commoncore</groupId>
        <artifactId>CommonCore</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </dependency>
</dependencies>
```

### 2. CommonCore'u Local Repository'ye Yükleme

CommonCore'u kullanmadan önce local Maven repository'ye yüklemeniz gerekir:

```bash
cd /path/to/CommonCore
./mvnw clean install
```

### 3. Otomatik Yapılandırma

**Hiçbir ek yapılandırma gerekmez!** CommonCore Spring Boot'un auto-configuration mekanizmasını kullanır ve otomatik olarak yüklenir.

---

## Yeni Projede Kullanım ve Özelleştirme

Yeni bir projede CommonCore kullanırken, `application.properties` dosyanıza ekleyerek özelleştirebileceğiniz tüm parametreler ve örnek yapılandırmalar.

### Minimum Gereksinimler

Yeni bir projede CommonCore kullanmak için minimum şu dependency'ler gereklidir:

```xml
<dependencies>
    <!-- Zorunlu -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- CommonCore -->
    <dependency>
        <groupId>io.commoncore</groupId>
        <artifactId>CommonCore</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </dependency>
    
    <!-- Opsiyonel - Sadece kullanacağınız özellikler için -->
    
    <!-- Validation için -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    
    <!-- Security özellikleri için (JWT, API Key, Basic Auth, CORS) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    
    <!-- Actuator için -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
</dependencies>
```

### Özelleştirilebilir Parametreler

#### 1. Interceptor Yapılandırması

**Ne zaman özelleştirmeli:** Logging interceptor'ın hangi endpoint'leri loglayacağını değiştirmek istediğinizde.

```properties
# Interceptor Configuration
commoncore.interceptor.enabled=true                    # Interceptor'ı aç/kapat
commoncore.interceptor.include-patterns=/api/**,/v1/** # Loglanacak path'ler
commoncore.interceptor.exclude-patterns=/api/public/**,/health  # Loglanmayacak path'ler
```

**Örnek Senaryolar:**

```properties
# Sadece /api/** endpoint'lerini logla
commoncore.interceptor.include-patterns=/api/**

# /api/** ve /v1/** endpoint'lerini logla, /api/public/** hariç
commoncore.interceptor.include-patterns=/api/**,/v1/**
commoncore.interceptor.exclude-patterns=/api/public/**

# Interceptor'ı tamamen kapat
commoncore.interceptor.enabled=false
```

---

#### 2. Rate Limiting Yapılandırması

**Ne zaman özelleştirmeli:** API'nizin trafik yüküne göre rate limit ayarlarını değiştirmek istediğinizde.

```properties
# Rate Limiting Configuration
commoncore.rate-limit.enabled=true                    # Rate limiting'i aç/kapat
commoncore.rate-limit.max-requests=100                # Zaman penceresi içinde max istek sayısı
commoncore.rate-limit.window-size-in-seconds=60      # Zaman penceresi (saniye)
commoncore.rate-limit.per-ip=true                    # IP bazlı mı, global mi
commoncore.rate-limit.include-patterns=/api/**       # Rate limit uygulanacak path'ler
commoncore.rate-limit.exclude-patterns=/api/public/** # Rate limit'ten muaf path'ler
```

**Örnek Senaryolar:**

```properties
# Daha sıkı rate limiting (50 istek/dakika)
commoncore.rate-limit.max-requests=50
commoncore.rate-limit.window-size-in-seconds=60

# Daha gevşek rate limiting (200 istek/2 dakika)
commoncore.rate-limit.max-requests=200
commoncore.rate-limit.window-size-in-seconds=120

# Global rate limiting (IP bazlı değil)
commoncore.rate-limit.per-ip=false
commoncore.rate-limit.max-requests=1000

# Sadece belirli endpoint'lere rate limit uygula
commoncore.rate-limit.include-patterns=/api/users/**,/api/orders/**
commoncore.rate-limit.exclude-patterns=/api/public/**,/api/auth/**

# Rate limiting'i kapat
commoncore.rate-limit.enabled=false
```

---

#### 3. Security Yapılandırması

**Ne zaman özelleştirmeli:** Authentication ve authorization gerektiğinde.

##### 3.1. Genel Security Ayarları

```properties
# Security Configuration
commoncore.security.enabled=true  # Tüm security özelliklerini aç/kapat
```

##### 3.2. CORS Yapılandırması

**Ne zaman özelleştirmeli:** Frontend uygulamanız farklı bir origin'den API'nize erişecekse.

```properties
# CORS Configuration
commoncore.security.cors.enabled=true
commoncore.security.cors.allowed-origins=http://localhost:3000,https://myapp.com
commoncore.security.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
commoncore.security.cors.allowed-headers=*
commoncore.security.cors.allow-credentials=true
commoncore.security.cors.max-age=3600
```

**Örnek Senaryolar:**

```properties
# Development ortamı - Tüm origin'lere izin ver
commoncore.security.cors.enabled=true
commoncore.security.cors.allowed-origins=*
commoncore.security.cors.allow-credentials=false

# Production ortamı - Sadece belirli origin'lere izin ver
commoncore.security.cors.enabled=true
commoncore.security.cors.allowed-origins=https://myapp.com,https://www.myapp.com
commoncore.security.cors.allow-credentials=true
commoncore.security.cors.allowed-headers=Authorization,Content-Type,X-Requested-With

# CORS'u kapat
commoncore.security.cors.enabled=false
```

##### 3.3. JWT Authentication

**Ne zaman özelleştirmeli:** JWT token tabanlı authentication kullanmak istediğinizde.

```properties
# JWT Configuration
commoncore.security.jwt.enabled=true
commoncore.security.jwt.secret=your-secret-key-min-256-bits-long-for-security
commoncore.security.jwt.expiration-ms=86400000
commoncore.security.jwt.header-name=Authorization
commoncore.security.jwt.token-prefix=Bearer 
commoncore.security.jwt.exclude-paths=/api/auth/**,/api/public/**,/actuator/**
```

**Örnek Senaryolar:**

```properties
# Kısa süreli token (1 saat)
commoncore.security.jwt.enabled=true
commoncore.security.jwt.secret=your-production-secret-key-at-least-256-bits-long
commoncore.security.jwt.expiration-ms=3600000

# Uzun süreli token (7 gün)
commoncore.security.jwt.enabled=true
commoncore.security.jwt.expiration-ms=604800000

# Farklı header adı kullan
commoncore.security.jwt.header-name=X-Auth-Token
commoncore.security.jwt.token-prefix=Token 

# Public endpoint'leri exclude et
commoncore.security.jwt.exclude-paths=/api/auth/**,/api/public/**,/api/docs/**,/actuator/**
```

**Önemli Notlar:**
- `secret` en az 256 bit (32 karakter) uzunluğunda olmalıdır
- Production'da güçlü bir secret key kullanın
- Token expiration süresini güvenlik gereksinimlerinize göre ayarlayın

##### 3.4. API Key Authentication

**Ne zaman özelleştirmeli:** Basit API key tabanlı authentication kullanmak istediğinizde.

```properties
# API Key Configuration
commoncore.security.api-key.enabled=true
commoncore.security.api-key.header-name=X-API-Key
commoncore.security.api-key.api-key-value=your-secret-api-key-here
commoncore.security.api-key.exclude-paths=/api/public/**
```

**Örnek Senaryolar:**

```properties
# Farklı header adı kullan
commoncore.security.api-key.enabled=true
commoncore.security.api-key.header-name=X-MyApp-API-Key
commoncore.security.api-key.api-key-value=production-api-key-12345

# Public endpoint'leri exclude et
commoncore.security.api-key.exclude-paths=/api/public/**,/api/docs/**,/actuator/**
```

##### 3.5. Basic Authentication

**Ne zaman özelleştirmeli:** HTTP Basic Authentication kullanmak istediğinizde (genellikle internal API'ler için).

```properties
# Basic Auth Configuration
commoncore.security.basic-auth.enabled=true
commoncore.security.basic-auth.username=admin
commoncore.security.basic-auth.password=secure-password-here
commoncore.security.basic-auth.exclude-paths=/api/public/**
```

**Örnek Senaryolar:**

```properties
# Farklı kullanıcı adı ve şifre
commoncore.security.basic-auth.enabled=true
commoncore.security.basic-auth.username=api-user
commoncore.security.basic-auth.password=strong-password-123

# Public endpoint'leri exclude et
commoncore.security.basic-auth.exclude-paths=/api/public/**,/api/docs/**,/actuator/**
```

**Önemli Notlar:**
- Production'da güçlü şifreler kullanın
- Basic Auth, HTTPS üzerinden kullanılmalıdır

---

#### 4. Actuator Yapılandırması

**Ne zaman özelleştirmeli:** Monitoring ve health check endpoint'lerini kullanmak istediğinizde.

```properties
# Spring Actuator Configuration
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=when-authorized
management.endpoint.health.show-components=always
management.endpoints.web.base-path=/actuator

# CommonCore Actuator Configuration
commoncore.actuator.enabled=true
commoncore.actuator.exposed-endpoints=health,info,metrics
commoncore.actuator.health-show-details=when-authorized
```

**Örnek Senaryolar:**

```properties
# Tüm endpoint'leri aç (sadece development)
management.endpoints.web.exposure.include=*
management.endpoint.health.show-details=always

# Sadece health ve info endpoint'lerini aç (production)
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=never

# Prometheus metrics ekle
management.endpoints.web.exposure.include=health,info,metrics,prometheus

# Farklı base path kullan
management.endpoints.web.base-path=/monitoring
```

---

### Tam Örnek: Yeni Proje Yapılandırması

Yeni bir projede CommonCore kullanırken örnek `application.properties` dosyası:

```properties
# ============================================
# Application Configuration
# ============================================
spring.application.name=MyNewProject
server.port=8080

# ============================================
# CommonCore Configuration
# ============================================

# Interceptor Configuration
commoncore.interceptor.enabled=true
commoncore.interceptor.include-patterns=/api/**
commoncore.interceptor.exclude-patterns=/api/public/**,/health

# Rate Limiting Configuration
commoncore.rate-limit.enabled=true
commoncore.rate-limit.max-requests=100
commoncore.rate-limit.window-size-in-seconds=60
commoncore.rate-limit.per-ip=true
commoncore.rate-limit.include-patterns=/api/**
commoncore.rate-limit.exclude-patterns=/api/public/**,/api/auth/**

# Security Configuration
commoncore.security.enabled=true

# CORS Configuration
commoncore.security.cors.enabled=true
commoncore.security.cors.allowed-origins=http://localhost:3000,https://myapp.com
commoncore.security.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
commoncore.security.cors.allowed-headers=*
commoncore.security.cors.allow-credentials=true
commoncore.security.cors.max-age=3600

# JWT Configuration
commoncore.security.jwt.enabled=true
commoncore.security.jwt.secret=MySecretKeyForJWTTokenGenerationMustBeAtLeast256BitsLongForSecurity
commoncore.security.jwt.expiration-ms=86400000
commoncore.security.jwt.header-name=Authorization
commoncore.security.jwt.token-prefix=Bearer 
commoncore.security.jwt.exclude-paths=/api/auth/**,/api/public/**,/actuator/**,/h2-console/**

# API Key Configuration (disabled)
commoncore.security.api-key.enabled=false

# Basic Auth Configuration (disabled)
commoncore.security.basic-auth.enabled=false

# Actuator Configuration
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=when-authorized
management.endpoints.web.base-path=/actuator
commoncore.actuator.enabled=true
```

---

### Senaryo Bazlı Yapılandırma Örnekleri

#### Senaryo 1: Basit REST API (Authentication Yok)

```properties
# Sadece logging ve rate limiting
commoncore.interceptor.enabled=true
commoncore.rate-limit.enabled=true
commoncore.rate-limit.max-requests=200
commoncore.security.enabled=false
```

#### Senaryo 2: Public API (API Key ile)

```properties
# API Key authentication
commoncore.security.enabled=true
commoncore.security.api-key.enabled=true
commoncore.security.api-key.api-key-value=public-api-key-12345
commoncore.security.api-key.exclude-paths=/api/docs/**,/actuator/**
commoncore.security.jwt.enabled=false
commoncore.security.basic-auth.enabled=false
```

#### Senaryo 3: Internal API (Basic Auth ile)

```properties
# Basic Auth
commoncore.security.enabled=true
commoncore.security.basic-auth.enabled=true
commoncore.security.basic-auth.username=internal-user
commoncore.security.basic-auth.password=secure-password
commoncore.security.basic-auth.exclude-paths=/actuator/**
commoncore.security.jwt.enabled=false
commoncore.security.api-key.enabled=false
```

#### Senaryo 4: Modern Web App (JWT + CORS)

```properties
# JWT + CORS
commoncore.security.enabled=true
commoncore.security.jwt.enabled=true
commoncore.security.jwt.secret=your-secret-key-min-256-bits
commoncore.security.jwt.expiration-ms=3600000
commoncore.security.jwt.exclude-paths=/api/auth/**,/api/public/**
commoncore.security.cors.enabled=true
commoncore.security.cors.allowed-origins=https://myapp.com
commoncore.security.cors.allow-credentials=true
```

#### Senaryo 5: Microservice (Actuator + Rate Limiting)

```properties
# Monitoring + Rate Limiting
commoncore.rate-limit.enabled=true
commoncore.rate-limit.max-requests=1000
commoncore.rate-limit.per-ip=false
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=always
```

---

### Özelleştirme Kontrol Listesi

Yeni bir projede CommonCore kullanırken şu adımları takip edin:

- [ ] CommonCore dependency'sini `pom.xml`'e eklediniz mi?
- [ ] Gerekli Spring Boot starter'ları eklediniz mi? (validation, security, actuator)
- [ ] CommonCore'u local repository'ye yüklediniz mi? (`./mvnw clean install`)
- [ ] `application.properties` dosyasına yapılandırma eklediniz mi?
- [ ] Interceptor path pattern'lerini projenize göre ayarladınız mı?
- [ ] Rate limiting ayarlarını trafik yükünüze göre ayarladınız mı?
- [ ] Security özelliklerini (JWT/API Key/Basic Auth) yapılandırdınız mı?
- [ ] CORS ayarlarını frontend origin'lerinize göre yapılandırdınız mı?
- [ ] Exclude paths'leri doğru ayarladınız mı? (auth, public, actuator)
- [ ] Production için secret key'leri değiştirdiniz mi?

---

## Özellikler

### CustomResponse

Generic API response wrapper sınıfı. Tüm API response'larınızı standart formatta döndürmenizi sağlar.

#### Yapı

```java
public class CustomResponse<T> {
    private int statusCode;      // HTTP status code
    private T data;              // Response data (generic)
    private String message;       // Response message
}
```

#### Kullanım

```java
import io.commoncore.dto.CustomResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    // Başarılı response
    @GetMapping
    public ResponseEntity<CustomResponse<List<User>>> getAllUsers() {
        List<User> users = userService.findAll();
        return ResponseEntity.ok(
            CustomResponse.success(users, "Users retrieved successfully")
        );
    }
    
    // Oluşturma response'u
    @PostMapping
    public ResponseEntity<CustomResponse<User>> createUser(@RequestBody User user) {
        User created = userService.create(user);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(CustomResponse.created(created, "User created successfully"));
    }
    
    // Hata response'u
    @GetMapping("/{id}")
    public ResponseEntity<CustomResponse<User>> getUser(@PathVariable Long id) {
        User user = userService.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found"));
        return ResponseEntity.ok(CustomResponse.success(user));
    }
}
```

#### Static Factory Metodları

- `CustomResponse.success(T data)` - 200 OK
- `CustomResponse.success(T data, String message)` - 200 OK with message
- `CustomResponse.created(T data)` - 201 Created
- `CustomResponse.created(T data, String message)` - 201 Created with message
- `CustomResponse.error(int statusCode, String message)` - Error response

#### Örnek Response

```json
{
  "statusCode": 200,
  "data": {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com"
  },
  "message": "User retrieved successfully"
}
```

---

### Exception Handling

CommonCore, tüm exception'ları otomatik olarak yakalar ve standart formatta response döner.

#### Base Exception'lar

**BaseNotFoundException**
```java
package io.commoncore.exception;

public class BaseNotFoundException extends RuntimeException {
    public BaseNotFoundException(String message) {
        super(message);
    }
}
```

**BaseValidationException**
```java
package io.commoncore.exception;

public class BaseValidationException extends RuntimeException {
    public BaseValidationException(String message) {
        super(message);
    }
}
```

**RateLimitExceededException**
```java
package io.commoncore.exception;

public class RateLimitExceededException extends RuntimeException {
    private final long retryAfterSeconds;
    
    public RateLimitExceededException(String message, long retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }
}
```

#### Custom Exception Oluşturma

```java
package com.myproject.exception;

import io.commoncore.exception.BaseNotFoundException;

public class UserNotFoundException extends BaseNotFoundException {
    public UserNotFoundException(String message) {
        super(message);
    }
    
    public UserNotFoundException(Long id) {
        super("User not found with id: " + id);
    }
}
```

#### Global Exception Handler

CommonCore otomatik olarak şu exception'ları yakalar:

- `BaseNotFoundException` → 404 Not Found
- `BaseValidationException` → 400 Bad Request
- `RateLimitExceededException` → 429 Too Many Requests
- `MethodArgumentNotValidException` → 400 Bad Request (Bean Validation)
- `ConstraintViolationException` → 400 Bad Request (Method-level validation)
- `IllegalArgumentException` → 400 Bad Request
- `Exception` → 500 Internal Server Error

#### Örnek Exception Response

```json
{
  "statusCode": 404,
  "data": {
    "timestamp": "2024-01-10T12:00:00",
    "error": "Not Found",
    "message": "User not found with id: 123"
  },
  "message": "User not found with id: 123"
}
```

#### Validation Error Response

```json
{
  "statusCode": 400,
  "data": {
    "timestamp": "2024-01-10T12:00:00",
    "error": "Validation Failed",
    "message": "Request validation failed",
    "fieldErrors": {
      "email": "Email must be a valid email address",
      "password": "Password must be at least 8 characters long"
    }
  },
  "message": "Validation failed"
}
```

---

### Validation

CommonCore, Jakarta Bean Validation desteği sağlar ve custom validator'lar içerir.

#### Bean Validation Kullanımı

```java
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @PostMapping
    public ResponseEntity<CustomResponse<User>> createUser(
            @Valid @RequestBody UserDTO userDTO) {
        // @Valid annotation'ı ile otomatik validation yapılır
        User user = userService.create(userDTO);
        return ResponseEntity.ok(CustomResponse.created(user));
    }
}

// DTO sınıfı
public class UserDTO {
    @NotBlank(message = "Name is required")
    private String name;
    
    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is required")
    private String email;
    
    @NotNull(message = "Age is required")
    @Min(value = 18, message = "Age must be at least 18")
    @Max(value = 100, message = "Age must be at most 100")
    private Integer age;
    
    @StrongPassword  // CommonCore'dan custom validator
    private String password;
}
```

#### Custom Validator: @StrongPassword

CommonCore, güçlü şifre kontrolü için `@StrongPassword` annotation'ı sağlar.

**Gereksinimler:**
- Minimum 8 karakter
- En az bir büyük harf (A-Z)
- En az bir küçük harf (a-z)
- En az bir rakam (0-9)
- En az bir özel karakter (!@#$%^&*)

**Kullanım:**

```java
public class UserDTO {
    @StrongPassword
    private String password;
}
```

**Özel Mesaj:**

```java
@StrongPassword(message = "Şifre en az 8 karakter olmalı ve büyük harf, küçük harf, rakam ve özel karakter içermelidir")
private String password;
```

---

### Logging Interceptor

CommonCore, tüm HTTP request/response'ları otomatik olarak loglar.

#### Varsayılan Davranış

- Tüm `/api/**` endpoint'leri loglanır
- Request method, URI, IP adresi loglanır
- Response status code ve işlem süresi loglanır

#### Log Formatı

```
INFO - Incoming request: GET /api/users from 127.0.0.1
INFO - Request processed: GET /api/users - Status: 200 - Time: 15ms
```

#### Yapılandırma

```properties
# Interceptor Configuration
commoncore.interceptor.enabled=true
commoncore.interceptor.include-patterns=/api/**,/v1/**
commoncore.interceptor.exclude-patterns=/api/public/**,/health
```

**Parametreler:**
- `enabled`: Interceptor'ı aktif/pasif yapar (default: `true`)
- `include-patterns`: Loglanacak path pattern'leri (default: `/api/**`)
- `exclude-patterns`: Loglanmayacak path pattern'leri (default: boş)

---

### Rate Limiting

CommonCore, IP bazlı veya global rate limiting sağlar.

#### Varsayılan Ayarlar

- **Max Requests**: 100 istek
- **Window Size**: 60 saniye (1 dakika)
- **Per IP**: `true` (her IP için ayrı limit)
- **Include Patterns**: `/api/**`
- **Exclude Patterns**: `/h2-console/**`

#### Yapılandırma

```properties
# Rate Limiting Configuration
commoncore.rate-limit.enabled=true
commoncore.rate-limit.max-requests=100
commoncore.rate-limit.window-size-in-seconds=60
commoncore.rate-limit.per-ip=true
commoncore.rate-limit.include-patterns=/api/**
commoncore.rate-limit.exclude-patterns=/api/public/**,/health
```

**Parametreler:**
- `enabled`: Rate limiting'i aktif/pasif yapar (default: `true`)
- `max-requests`: Zaman penceresi içinde izin verilen maksimum istek sayısı (default: `100`)
- `window-size-in-seconds`: Zaman penceresi süresi saniye cinsinden (default: `60`)
- `per-ip`: IP bazlı mı yoksa global mi (default: `true`)
- `include-patterns`: Rate limiting uygulanacak path'ler (default: `/api/**`)
- `exclude-patterns`: Rate limiting'den muaf tutulacak path'ler

#### Rate Limit Aşıldığında

- HTTP **429 Too Many Requests** döner
- `Retry-After` header'ı ile ne kadar beklenmesi gerektiği belirtilir
- `X-RateLimit-Limit` ve `X-RateLimit-Window` header'ları eklenir

**Örnek Response:**

```json
{
  "statusCode": 429,
  "data": {
    "timestamp": "2024-01-10T12:00:00",
    "error": "Too Many Requests",
    "message": "Rate limit exceeded. Maximum 100 requests per 60 seconds",
    "retryAfterSeconds": 45
  },
  "message": "Rate limit exceeded. Maximum 100 requests per 60 seconds"
}
```

---

### Security & Authentication

CommonCore, çoklu authentication yöntemleri ve CORS yapılandırması sağlar.

#### Genel Security Yapılandırması

```properties
# Security Configuration
commoncore.security.enabled=true
```

**Not:** `commoncore.security.enabled=false` yaparsanız, tüm security özellikleri devre dışı kalır.

---

#### JWT Authentication

JWT (JSON Web Token) tabanlı authentication.

##### Yapılandırma

```properties
# JWT Configuration
commoncore.security.jwt.enabled=true
commoncore.security.jwt.secret=your-secret-key-change-in-production-min-256-bits
commoncore.security.jwt.expiration-ms=86400000
commoncore.security.jwt.header-name=Authorization
commoncore.security.jwt.token-prefix=Bearer 
commoncore.security.jwt.exclude-paths=/api/auth/**,/h2-console/**,/actuator/**
```

**Parametreler:**
- `enabled`: JWT authentication'ı aktif/pasif yapar (default: `false`)
- `secret`: JWT token imzalama için secret key (default: `your-secret-key-change-in-production`)
- `expiration-ms`: Token geçerlilik süresi milisaniye cinsinden (default: `86400000` = 24 saat)
- `header-name`: JWT token'ın gönderileceği header adı (default: `Authorization`)
- `token-prefix`: Token prefix'i (default: `Bearer `)
- `exclude-paths`: JWT kontrolünden muaf tutulacak path'ler

##### Kullanım

**1. Login Endpoint Oluşturma:**

```java
import io.commoncore.security.jwt.JwtUtil;
import io.commoncore.dto.CustomResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final JwtUtil jwtUtil;
    
    @PostMapping("/login")
    public ResponseEntity<CustomResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        
        // Authentication kontrolü (kendi logic'iniz)
        if (authenticate(request.getUsername(), request.getPassword())) {
            String token = jwtUtil.generateToken(request.getUsername());
            LoginResponse response = new LoginResponse(
                token,
                "Bearer",
                jwtUtil.getExpirationMs()
            );
            return ResponseEntity.ok(CustomResponse.success(response));
        }
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(CustomResponse.error(401, "Invalid credentials"));
    }
}
```

**2. Protected Endpoint'ler:**

JWT aktifken, exclude edilmemiş tüm endpoint'ler otomatik olarak korunur:

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    // Bu endpoint JWT token gerektirir
    @GetMapping
    public ResponseEntity<CustomResponse<List<User>>> getAllUsers() {
        // JWT token doğrulandıktan sonra buraya gelir
        List<User> users = userService.findAll();
        return ResponseEntity.ok(CustomResponse.success(users));
    }
}
```

**3. Token Kullanımı:**

```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}'

# Protected endpoint'e erişim
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

##### JwtUtil Metodları

```java
// Token oluşturma
String token = jwtUtil.generateToken("username");

// Token'dan username çıkarma
String username = jwtUtil.extractUsername(token);

// Token doğrulama
boolean isValid = jwtUtil.validateToken(token);

// Token'ın expire olup olmadığını kontrol etme
boolean isExpired = jwtUtil.isTokenExpired(token);
```

---

#### API Key Authentication

API Key tabanlı authentication.

##### Yapılandırma

```properties
# API Key Configuration
commoncore.security.api-key.enabled=true
commoncore.security.api-key.header-name=X-API-Key
commoncore.security.api-key.api-key-value=your-secret-api-key-here
commoncore.security.api-key.exclude-paths=/api/public/**
```

**Parametreler:**
- `enabled`: API Key authentication'ı aktif/pasif yapar (default: `false`)
- `header-name`: API Key'in gönderileceği header adı (default: `X-API-Key`)
- `api-key-value`: Beklenen API Key değeri (default: `change-me`)
- `exclude-paths`: API Key kontrolünden muaf tutulacak path'ler

##### Kullanım

```bash
# API Key ile istek
curl -X GET http://localhost:8080/api/users \
  -H "X-API-Key: your-secret-api-key-here"
```

---

#### Basic Authentication

HTTP Basic Authentication desteği.

##### Yapılandırma

```properties
# Basic Auth Configuration
commoncore.security.basic-auth.enabled=true
commoncore.security.basic-auth.username=admin
commoncore.security.basic-auth.password=password
commoncore.security.basic-auth.exclude-paths=/api/public/**
```

**Parametreler:**
- `enabled`: Basic Auth'u aktif/pasif yapar (default: `false`)
- `username`: Basic Auth kullanıcı adı (default: `admin`)
- `password`: Basic Auth şifresi (default: `password`)
- `exclude-paths`: Basic Auth kontrolünden muaf tutulacak path'ler

##### Kullanım

```bash
# Basic Auth ile istek
curl -X GET http://localhost:8080/api/users \
  -u admin:password
```

---

#### CORS Configuration

Cross-Origin Resource Sharing (CORS) yapılandırması.

##### Yapılandırma

```properties
# CORS Configuration
commoncore.security.cors.enabled=true
commoncore.security.cors.allowed-origins=http://localhost:3000,https://example.com
commoncore.security.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
commoncore.security.cors.allowed-headers=*
commoncore.security.cors.allow-credentials=true
commoncore.security.cors.max-age=3600
```

**Parametreler:**
- `enabled`: CORS'u aktif/pasif yapar (default: `true`)
- `allowed-origins`: İzin verilen origin'ler (default: `*` = tüm origin'ler)
- `allowed-methods`: İzin verilen HTTP metodları (default: `GET,POST,PUT,DELETE,OPTIONS`)
- `allowed-headers`: İzin verilen header'lar (default: `*` = tüm header'lar)
- `allow-credentials`: Credential gönderimine izin ver (default: `false`)
- `max-age`: Preflight request cache süresi saniye cinsinden (default: `3600`)

**Not:** `allowed-origins=*` ve `allow-credentials=true` birlikte kullanılamaz. Production'da spesifik origin'ler belirtin.

---

### Spring Actuator

CommonCore, Spring Actuator desteği sağlar.

#### Yapılandırma

**GoalSync veya diğer projelerde:**

```properties
# Spring Actuator Configuration
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=when-authorized
management.endpoint.health.show-components=always
management.endpoints.web.base-path=/actuator

# CommonCore Actuator Configuration
commoncore.actuator.enabled=true
commoncore.actuator.exposed-endpoints=health,info,metrics
commoncore.actuator.health-show-details=when-authorized
```

**Parametreler:**
- `enabled`: Actuator'ı aktif/pasif yapar (default: `true`)
- `exposed-endpoints`: Açık endpoint'ler listesi (default: `health,info`)
- `base-path`: Base path (default: `/actuator`)
- `health-show-details`: Health endpoint detay gösterimi (`never`, `when-authorized`, `always`)

#### Kullanım

```bash
# Health check
curl http://localhost:8080/actuator/health

# Info endpoint
curl http://localhost:8080/actuator/info

# Metrics listesi
curl http://localhost:8080/actuator/metrics

# Belirli bir metrik
curl http://localhost:8080/actuator/metrics/jvm.memory.used
```

**Not:** Actuator endpoint'leri otomatik olarak JWT kontrolünden muaf tutulur (`/actuator/**`).

---

## Yapılandırma Parametreleri

### Tüm Parametrelerin Özeti

```properties
# ============================================
# CommonCore Yapılandırma Parametreleri
# ============================================

# Interceptor Configuration
commoncore.interceptor.enabled=true
commoncore.interceptor.include-patterns=/api/**
commoncore.interceptor.exclude-patterns=/h2-console/**

# Rate Limiting Configuration
commoncore.rate-limit.enabled=true
commoncore.rate-limit.max-requests=100
commoncore.rate-limit.window-size-in-seconds=60
commoncore.rate-limit.per-ip=true
commoncore.rate-limit.include-patterns=/api/**
commoncore.rate-limit.exclude-patterns=/h2-console/**

# Security Configuration
commoncore.security.enabled=true

# CORS Configuration
commoncore.security.cors.enabled=true
commoncore.security.cors.allowed-origins=*
commoncore.security.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
commoncore.security.cors.allowed-headers=*
commoncore.security.cors.allow-credentials=false
commoncore.security.cors.max-age=3600

# JWT Configuration
commoncore.security.jwt.enabled=false
commoncore.security.jwt.secret=your-secret-key-change-in-production-min-256-bits
commoncore.security.jwt.expiration-ms=86400000
commoncore.security.jwt.header-name=Authorization
commoncore.security.jwt.token-prefix=Bearer 
commoncore.security.jwt.exclude-paths=/api/auth/**,/h2-console/**,/actuator/**

# API Key Configuration
commoncore.security.api-key.enabled=false
commoncore.security.api-key.header-name=X-API-Key
commoncore.security.api-key.api-key-value=change-me
commoncore.security.api-key.exclude-paths=/api/public/**

# Basic Auth Configuration
commoncore.security.basic-auth.enabled=false
commoncore.security.basic-auth.username=admin
commoncore.security.basic-auth.password=password
commoncore.security.basic-auth.exclude-paths=/api/public/**

# Actuator Configuration
commoncore.actuator.enabled=true
commoncore.actuator.exposed-endpoints=health,info
commoncore.actuator.base-path=/actuator
commoncore.actuator.health-show-details=never
```

---

## Kullanım Örnekleri

### Tam Örnek: User Management API

```java
package com.myproject.controller;

import com.myproject.dto.UserDTO;
import com.myproject.exception.UserNotFoundException;
import com.myproject.service.UserService;
import io.commoncore.dto.CustomResponse;
import io.commoncore.validation.StrongPassword;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private final UserService userService;
    
    @GetMapping
    public ResponseEntity<CustomResponse<List<UserDTO>>> getAllUsers() {
        List<UserDTO> users = userService.findAll();
        return ResponseEntity.ok(
            CustomResponse.success(users, "Users retrieved successfully")
        );
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CustomResponse<UserDTO>> getUser(@PathVariable Long id) {
        UserDTO user = userService.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
        return ResponseEntity.ok(CustomResponse.success(user));
    }
    
    @PostMapping
    public ResponseEntity<CustomResponse<UserDTO>> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        UserDTO created = userService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(CustomResponse.created(created, "User created successfully"));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<CustomResponse<UserDTO>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        UserDTO updated = userService.update(id, request)
            .orElseThrow(() -> new UserNotFoundException(id));
        return ResponseEntity.ok(
            CustomResponse.success(updated, "User updated successfully")
        );
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<CustomResponse<Void>> deleteUser(@PathVariable Long id) {
        if (!userService.exists(id)) {
            throw new UserNotFoundException(id);
        }
        userService.delete(id);
        return ResponseEntity.ok(
            CustomResponse.success(null, "User deleted successfully")
        );
    }
}

// Request DTO
@Data
class CreateUserRequest {
    @NotBlank(message = "Name is required")
    private String name;
    
    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is required")
    private String email;
    
    @NotNull(message = "Age is required")
    @Min(value = 18, message = "Age must be at least 18")
    @Max(value = 100, message = "Age must be at most 100")
    private Integer age;
    
    @StrongPassword(message = "Password must be strong")
    private String password;
}

// Exception
package com.myproject.exception;

import io.commoncore.exception.BaseNotFoundException;

public class UserNotFoundException extends BaseNotFoundException {
    public UserNotFoundException(Long id) {
        super("User not found with id: " + id);
    }
}
```

---

## Proje Yapısı

### CommonCore Proje Yapısı

```
CommonCore/
├── src/main/java/io/commoncore/
│   ├── dto/
│   │   └── CustomResponse.java              # Generic API response wrapper
│   ├── exception/
│   │   ├── BaseNotFoundException.java       # Base not found exception
│   │   ├── BaseValidationException.java    # Base validation exception
│   │   └── RateLimitExceededException.java  # Rate limit exception
│   ├── advice/
│   │   └── GlobalExceptionHandler.java      # Global exception handler
│   ├── interceptor/
│   │   ├── LoggingInterceptor.java          # Request/Response logging
│   │   └── RateLimitingInterceptor.java     # Rate limiting
│   ├── ratelimit/
│   │   └── RateLimiter.java                 # Rate limiter implementation
│   ├── security/
│   │   ├── jwt/
│   │   │   ├── JwtUtil.java                 # JWT utility
│   │   │   └── JwtAuthenticationFilter.java # JWT filter
│   │   ├── apikey/
│   │   │   └── ApiKeyAuthenticationFilter.java # API Key filter
│   │   └── basic/
│   │       └── BasicAuthConfig.java        # Basic Auth config
│   ├── validation/
│   │   └── StrongPassword.java               # Custom password validator
│   └── config/
│       ├── CommonCoreAutoConfiguration.java # Auto-configuration
│       ├── CommonCoreProperties.java       # Configuration properties
│       ├── SecurityConfig.java             # Security configuration
│       └── WebConfig.java                  # Web configuration
└── src/main/resources/
    └── application-commoncore.properties   # Default configuration
```

### Kullanıcı Projesi Yapısı

```
MyProject/
├── src/main/java/com/myproject/
│   ├── controller/
│   │   └── UserController.java             # CustomResponse kullanır
│   ├── service/
│   │   └── UserService.java
│   ├── repository/
│   │   └── UserRepository.java
│   ├── dto/
│   │   └── UserDTO.java                    # Validation annotations içerir
│   ├── exception/
│   │   └── UserNotFoundException.java      # BaseNotFoundException'dan extend
│   └── MyApplication.java
├── src/main/resources/
│   └── application.properties              # CommonCore yapılandırması
└── pom.xml                                 # CommonCore dependency içerir
```

---

## Sorun Giderme

### 1. CommonCore Yüklenmiyor

**Sorun:** `ClassNotFoundException` veya dependency bulunamıyor hatası.

**Çözüm:**
```bash
cd /path/to/CommonCore
./mvnw clean install
```

### 2. Interceptor Çalışmıyor

**Sorun:** Request'ler loglanmıyor.

**Kontrol Listesi:**
- CommonCore'un local repository'ye yüklendiğinden emin olun
- `application.properties`'de `commoncore.interceptor.enabled=true` olduğundan emin olun
- Path pattern'lerin doğru olduğundan emin olun

### 3. Exception Handler Çalışmıyor

**Sorun:** Exception'lar yakalanmıyor.

**Kontrol Listesi:**
- Custom exception'larınızın base exception'lardan extend ettiğinden emin olun
- `@RestControllerAdvice` annotation'ının CommonCore'da olduğunu kontrol edin
- Exception'ın `@RestController` içinde fırlatıldığından emin olun

### 4. JWT Authentication Çalışmıyor

**Sorun:** JWT token kabul edilmiyor.

**Kontrol Listesi:**
- `commoncore.security.jwt.enabled=true` olduğundan emin olun
- Secret key'in en az 256 bit olduğundan emin olun
- Token'ın `Authorization: Bearer TOKEN` formatında gönderildiğinden emin olun
- Exclude paths'in doğru yapılandırıldığından emin olun

### 5. Rate Limiting Çalışmıyor

**Sorun:** Rate limit uygulanmıyor.

**Kontrol Listesi:**
- `commoncore.rate-limit.enabled=true` olduğundan emin olun
- Include/exclude pattern'lerin doğru olduğundan emin olun
- Max requests ve window size'ın mantıklı değerler olduğundan emin olun

### 6. Actuator Endpoint'leri Erişilemiyor

**Sorun:** `/actuator/health` çalışmıyor.

**Kontrol Listesi:**
- Projenizin `pom.xml`'inde `spring-boot-starter-actuator` dependency'sinin olduğundan emin olun
- `management.endpoints.web.exposure.include` yapılandırmasının doğru olduğundan emin olun
- Actuator endpoint'lerinin exclude paths'te olduğundan emin olun (`/actuator/**`)

---

## Versiyonlama

CommonCore'u güncellediğinizde:

1. CommonCore'u rebuild edin: `./mvnw clean install`
2. Yeni projelerde dependency'yi güncelleyin
3. Mevcut projelerde dependency'yi güncelleyip rebuild edin

---

## Gereksinimler

- **Java**: 17+
- **Spring Boot**: 3.3.5+
- **Maven**: 3.6+

---

## Ek Kaynaklar

- [QUICK_START.md](./QUICK_START.md) - Hızlı başlangıç kılavuzu
- [INTERCEPTOR_USAGE.md](./INTERCEPTOR_USAGE.md) - Interceptor kullanım detayları
- [RATE_LIMITING.md](./RATE_LIMITING.md) - Rate limiting detayları
- [README.md](./README.md) - Genel bakış

---

**Son Güncelleme:** 2024-01-10
