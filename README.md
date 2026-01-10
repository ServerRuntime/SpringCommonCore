# CommonCore - Shared Library

CommonCore, Spring Boot projelerinde kullanılabilecek ortak bileşenleri içeren bir kütüphanedir.

## Özellikler

- ✅ **CustomResponse**: Generic API response wrapper
- ✅ **Base Exceptions**: BaseNotFoundException, BaseValidationException, RateLimitExceededException
- ✅ **Global Exception Handler**: Otomatik exception handling (Bean Validation dahil)
- ✅ **Logging Interceptor**: Request/Response logging
- ✅ **Rate Limiting**: IP bazlı veya global rate limiting
- ✅ **Security & Authentication**: 
  - JWT Authentication
  - API Key Authentication
  - Basic Authentication
  - CORS Configuration
- ✅ **Bean Validation**: Jakarta Validation desteği
- ✅ **Custom Validators**: @StrongPassword gibi custom validator'lar
- ✅ **Spring Actuator**: Monitoring ve health check desteği
- ✅ **HTTP Client**: Farklı servislere HTTP request atmak için hazır utility
- ✅ **Pagination & Sorting**: Sayfalama ve sıralama mimarisi
- ✅ **Auto-Configuration**: Otomatik yapılandırma

## Kurulum

### 1. Maven Dependency Ekleme

Yeni projenizin `pom.xml` dosyasına CommonCore dependency'sini ekleyin:

```xml
<dependencies>
    <!-- Diğer dependency'ler -->
    
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

**Hiçbir ek yapılandırma gerekmez!** CommonCore Spring Boot'un auto-configuration mekanizmasını kullanır.

## Kullanım

### CustomResponse Kullanımı

```java
import io.commoncore.dto.CustomResponse;

@RestController
public class MyController {
    
    @GetMapping("/api/data")
    public ResponseEntity<CustomResponse<List<Data>>> getData() {
        List<Data> data = service.getAll();
        return ResponseEntity.ok(CustomResponse.success(data, "Data retrieved successfully"));
    }
    
    @PostMapping("/api/data")
    public ResponseEntity<CustomResponse<Data>> createData(@RequestBody Data data) {
        Data created = service.create(data);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CustomResponse.created(created, "Data created successfully"));
    }
}
```

### Custom Exception Kullanımı

```java
import io.commoncore.exception.BaseNotFoundException;
import io.commoncore.exception.BaseValidationException;

// Domain-specific exception'larınızı base exception'lardan extend edin
public class MyEntityNotFoundException extends BaseNotFoundException {
    public MyEntityNotFoundException(String message) {
        super(message);
    }
}

public class MyEntityValidationException extends BaseValidationException {
    public MyEntityValidationException(String message) {
        super(message);
    }
}
```

### Logging Interceptor

Logging interceptor otomatik olarak tüm `/api/**` endpoint'lerini loglar:

```
INFO - Incoming request: GET /api/data from 127.0.0.1
INFO - Request processed: GET /api/data - Status: 200 - Time: 15ms
```

### Rate Limiting

Rate limiting otomatik olarak tüm `/api/**` endpoint'lerine uygulanır. Varsayılan ayarlar:
- **Max Requests**: 100 istek
- **Window Size**: 60 saniye (1 dakika)
- **Per IP**: true (her IP için ayrı limit)

Rate limit aşıldığında:
- HTTP 429 (Too Many Requests) döner
- `Retry-After` header'ı ile ne kadar beklenmesi gerektiği belirtilir
- `X-RateLimit-Limit` ve `X-RateLimit-Window` header'ları eklenir

**Örnek Response:**
```json
{
  "statusCode": 429,
  "data": {
    "timestamp": "2024-01-10T01:00:00",
    "error": "Too Many Requests",
    "message": "Rate limit exceeded. Maximum 100 requests per 60 seconds",
    "retryAfterSeconds": 45
  },
  "message": "Rate limit exceeded. Maximum 100 requests per 60 seconds"
}
```

### Global Exception Handler

Global exception handler otomatik olarak tüm exception'ları yakalar ve standart format'ta response döner:

```json
{
  "statusCode": 404,
  "data": {
    "timestamp": "2024-01-10T00:00:00",
    "error": "Not Found",
    "message": "Entity not found with id: 123"
  },
  "message": "Entity not found with id: 123"
}
```

## Yapılandırma (Opsiyonel)

### Application Properties ile Özelleştirme

Yeni projenizin `application.properties` dosyasına ekleyerek interceptor ve rate limiting'i özelleştirebilirsiniz:

```properties
# Interceptor Configuration
commoncore.interceptor.enabled=true
commoncore.interceptor.include-patterns=/api/**,/v1/**
commoncore.interceptor.exclude-patterns=/api/public/**,/health

# Rate Limiting Configuration
commoncore.rate-limit.enabled=true
commoncore.rate-limit.max-requests=200
commoncore.rate-limit.window-size-in-seconds=120
commoncore.rate-limit.per-ip=true
commoncore.rate-limit.include-patterns=/api/**
commoncore.rate-limit.exclude-patterns=/api/public/**,/health
```

**Rate Limiting Ayarları:**
- `enabled`: Rate limiting'i aktif/pasif yapar (default: true)
- `max-requests`: Zaman penceresi içinde izin verilen maksimum istek sayısı (default: 100)
- `window-size-in-seconds`: Zaman penceresi süresi saniye cinsinden (default: 60)
- `per-ip`: IP bazlı mı yoksa global mi (default: true)
- `include-patterns`: Rate limiting uygulanacak path'ler (default: /api/**)
- `exclude-patterns`: Rate limiting'den muaf tutulacak path'ler

### Programatik Özelleştirme

Eğer daha fazla kontrol istiyorsanız, kendi `WebMvcConfigurer` sınıfınızı oluşturun:

```java
@Configuration
public class MyProjectWebConfig implements WebMvcConfigurer {
    
    private final LoggingInterceptor loggingInterceptor;
    
    public MyProjectWebConfig(LoggingInterceptor loggingInterceptor) {
        this.loggingInterceptor = loggingInterceptor;
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor)
                .addPathPatterns("/api/**", "/v1/**")
                .excludePathPatterns("/api/public/**", "/health");
    }
}
```

**Not**: Bu durumda `CommonCoreAutoConfiguration`'ı exclude etmeniz gerekebilir:

```java
@SpringBootApplication(exclude = {CommonCoreAutoConfiguration.class})
public class MyApplication {
    // ...
}
```

## Proje Yapısı

```
CommonCore/
├── dto/
│   └── CustomResponse.java        # Generic API response wrapper
├── exception/
│   ├── BaseNotFoundException.java
│   └── BaseValidationException.java
├── advice/
│   └── GlobalExceptionHandler.java
├── interceptor/
│   └── LoggingInterceptor.java
└── config/
    └── CommonCoreAutoConfiguration.java  # Auto-configuration
```

## Örnek Proje Yapısı

Yeni projenizde şu şekilde kullanabilirsiniz:

```
MyNewProject/
├── src/main/java/com/myproject/
│   ├── controller/
│   │   └── MyController.java     # CustomResponse kullanır
│   ├── exception/
│   │   ├── MyEntityNotFoundException.java  # BaseNotFoundException'dan extend
│   │   └── MyEntityValidationException.java # BaseValidationException'dan extend
│   └── MyApplication.java
└── pom.xml                        # CommonCore dependency içerir
```

## Gereksinimler

- Java 17+
- Spring Boot 3.5.9+
- Maven 3.6+

## Versiyonlama

CommonCore'u güncellediğinizde:

1. CommonCore'u rebuild edin: `./mvnw clean install`
2. Yeni projelerde dependency'yi güncelleyin
3. Mevcut projelerde dependency'yi güncelleyip rebuild edin

## Sorun Giderme

### Interceptor çalışmıyor

- CommonCore'un local repository'ye yüklendiğinden emin olun
- `@SpringBootApplication` annotation'ının olduğundan emin olun
- Loglarda `CommonCoreAutoConfiguration` yüklendiğini kontrol edin

### Exception handler çalışmıyor

- Custom exception'larınızın base exception'lardan extend ettiğinden emin olun
- `@RestControllerAdvice` annotation'ının CommonCore'da olduğunu kontrol edin

## Ek Kaynaklar

- **[COMPLETE_DOCUMENTATION.md](./COMPLETE_DOCUMENTATION.md)** - 📚 Kapsamlı dokümantasyon (Tüm özellikler, parametreler ve örnekler)
- [QUICK_START.md](./QUICK_START.md) - Hızlı başlangıç kılavuzu
- [HTTP_CLIENT_USAGE.md](./HTTP_CLIENT_USAGE.md) - HTTP Client kullanım kılavuzu
- [PAGINATION_USAGE.md](./PAGINATION_USAGE.md) - Pagination & Sorting kullanım kılavuzu
- [INTERCEPTOR_USAGE.md](./INTERCEPTOR_USAGE.md) - Interceptor kullanım detayları
- [RATE_LIMITING.md](./RATE_LIMITING.md) - Rate limiting detayları

## Katkıda Bulunma

CommonCore'u geliştirmek için:

1. Yeni özellikler ekleyin
2. Test edin
3. `./mvnw clean install` ile rebuild edin
4. Tüm projelerde test edin
5. Dokümantasyonu güncelleyin
