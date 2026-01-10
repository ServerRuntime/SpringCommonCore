# Rate Limiting Kullanım Kılavuzu

## Genel Bakış

CommonCore, tüm projelerde otomatik olarak çalışan bir rate limiting mekanizması sağlar. Bu özellik, API'nizi aşırı isteklerden korur ve adil kullanım sağlar.

## Nasıl Çalışır?

Rate limiting, **Token Bucket Algorithm** kullanarak çalışır:

1. Her IP adresi (veya global) için bir "token bucket" oluşturulur
2. Her istek bir token tüketir
3. Belirli bir süre sonra (window size) bucket yeniden dolar
4. Bucket boşsa, istek reddedilir (HTTP 429)

## Varsayılan Ayarlar

```properties
commoncore.rate-limit.enabled=true
commoncore.rate-limit.max-requests=100
commoncore.rate-limit.window-size-in-seconds=60
commoncore.rate-limit.per-ip=true
commoncore.rate-limit.include-patterns=/api/**
```

Bu ayarlar şu anlama gelir:
- **100 istek** / **60 saniye** (1 dakika)
- Her **IP adresi** için ayrı limit
- Sadece `/api/**` endpoint'lerine uygulanır

## Özelleştirme

### Farklı Limitler

```properties
# Daha yüksek limit
commoncore.rate-limit.max-requests=500
commoncore.rate-limit.window-size-in-seconds=300  # 5 dakika

# Daha düşük limit (daha sıkı)
commoncore.rate-limit.max-requests=10
commoncore.rate-limit.window-size-in-seconds=60
```

### Global Rate Limiting

Tüm kullanıcılar için tek bir limit:

```properties
commoncore.rate-limit.per-ip=false
commoncore.rate-limit.max-requests=1000
```

### Farklı Path Pattern'ler

```properties
# Sadece belirli endpoint'lere uygula
commoncore.rate-limit.include-patterns=/api/users/**,/api/orders/**

# Bazı endpoint'leri hariç tut
commoncore.rate-limit.exclude-patterns=/api/public/**,/api/health
```

### Rate Limiting'i Devre Dışı Bırakma

```properties
commoncore.rate-limit.enabled=false
```

## Response Formatı

Rate limit aşıldığında:

**HTTP Status:** `429 Too Many Requests`

**Headers:**
```
Retry-After: 45
X-RateLimit-Limit: 100
X-RateLimit-Window: 60
```

**Response Body:**
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

## Örnek Senaryolar

### Senaryo 1: Public API için Sıkı Limit

```properties
commoncore.rate-limit.max-requests=10
commoncore.rate-limit.window-size-in-seconds=60
commoncore.rate-limit.per-ip=true
```

### Senaryo 2: Internal API için Yüksek Limit

```properties
commoncore.rate-limit.max-requests=1000
commoncore.rate-limit.window-size-in-seconds=60
commoncore.rate-limit.per-ip=false  # Global limit
```

### Senaryo 3: Farklı Endpoint'ler için Farklı Limitler

Bu durumda kendi `WebMvcConfigurer` sınıfınızı oluşturmanız gerekir:

```java
@Configuration
public class CustomRateLimitConfig implements WebMvcConfigurer {
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Sıkı limit için bir interceptor
        RateLimiter strictLimiter = new RateLimiter(10, 60);
        RateLimitingInterceptor strictInterceptor = 
            new RateLimitingInterceptor(/* strict config */);
        registry.addInterceptor(strictInterceptor)
                .addPathPatterns("/api/public/**");
        
        // Normal limit için başka bir interceptor
        // ...
    }
}
```

## Monitoring ve Logging

Rate limit aşıldığında loglarda şunu göreceksiniz:

```
WARN - Rate limit exceeded for key: 192.168.1.1 - Retry after: 45 seconds
```

## Best Practices

1. **Production'da**: Rate limiting'i mutlaka aktif tutun
2. **Development'da**: Daha yüksek limitler veya devre dışı bırakabilirsiniz
3. **Public API'ler**: Daha sıkı limitler kullanın
4. **Internal API'ler**: Daha yüksek limitler veya global limit kullanın
5. **Health Check Endpoint'leri**: Rate limiting'den muaf tutun

## Notlar

- Rate limiting **in-memory** çalışır (her uygulama instance'ı kendi limitini tutar)
- Distributed sistemlerde her instance için ayrı limit uygulanır
- Production'da Redis gibi distributed cache kullanmak için Bucket4j gibi kütüphaneler kullanılabilir (gelecekte eklenebilir)
