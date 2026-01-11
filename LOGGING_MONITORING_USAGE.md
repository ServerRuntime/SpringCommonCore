# Logging & Monitoring Kullanım Kılavuzu

Bu dokümantasyon, CommonCore'un Logging & Monitoring özelliklerinin detaylı kullanımını açıklar.

## İçindekiler

1. [Structured Logging (JSON Format)](#structured-logging-json-format)
2. [Request/Response Body Logging](#requestresponse-body-logging)
3. [Sensitive Data Masking](#sensitive-data-masking)
4. [Performance Monitoring](#performance-monitoring)
5. [Micrometer Metrics](#micrometer-metrics)
6. [Prometheus Export](#prometheus-export)
7. [Custom Metrics](#custom-metrics)
8. [Kullanım Örnekleri](#kullanım-örnekleri)

---

## Structured Logging (JSON Format)

### Ne İşe Yarar?

Structured logging, logları JSON formatında yazarak log aggregation sistemleri (ELK Stack, Splunk, etc.) ile entegrasyonu kolaylaştırır.

### Aktif Etme

```properties
commoncore.logging.structured-logging=true
```

### Örnek Log Çıktısı

**Önceki Format (Plain Text):**
```
INFO - Incoming request: GET /api/scores from 127.0.0.1
INFO - Request processed: GET /api/scores - Status: 200 - Time: 15ms
```

**Yeni Format (JSON):**
```json
{
  "timestamp": "2024-01-11T15:30:00.123Z",
  "level": "INFO",
  "type": "request",
  "requestId": "abc-123-def-456",
  "method": "GET",
  "uri": "/api/scores",
  "remoteAddr": "127.0.0.1",
  "userAgent": "Mozilla/5.0..."
}
```

### Avantajlar

- **Machine-readable**: Log parsing ve analiz kolaylaşır
- **Log Aggregation**: ELK Stack, Splunk gibi sistemlerle entegrasyon
- **Structured Data**: Log'larda arama ve filtreleme kolaylaşır
- **Consistency**: Tüm loglar aynı formatta

---

## Request/Response Body Logging

### Ne İşe Yarar?

HTTP request ve response body'lerini loglar, debugging ve troubleshooting için kullanışlıdır.

### Aktif Etme

```properties
# Request body'lerini logla
commoncore.logging.log-request-body=true

# Response body'lerini logla
commoncore.logging.log-response-body=true

# Header'ları logla
commoncore.logging.log-headers=true
```

### Güvenlik Notları

⚠️ **Dikkat**: Production'da hassas verileri loglamamaya dikkat edin!

- Password, token, credit card gibi verileri loglamayın
- Sensitive data masking'i aktif edin
- Max body size limit'i kullanın

### Yapılandırma

```properties
# Maksimum body boyutu (bytes)
commoncore.logging.max-body-size=10000

# Body loglaması yapılacak content type'lar
commoncore.logging.loggable-content-types=application/json,application/xml
```

### Örnek Log Çıktısı

```json
{
  "timestamp": "2024-01-11T15:30:00.123Z",
  "type": "request",
  "method": "POST",
  "uri": "/api/scores",
  "body": {
    "homeTeam": "Barcelona",
    "awayTeam": "Real Madrid",
    "homeScore": 3,
    "awayScore": 2,
    "status": "FINISHED"
  }
}
```

---

## Sensitive Data Masking

### Ne İşe Yarar?

Password, token, credit card gibi hassas verileri otomatik olarak maskeler, güvenlik ve compliance için kritiktir.

### Yapılandırma

```properties
# Maskelenecek hassas alanlar
commoncore.logging.sensitive-fields=password,token,authorization,creditCard,cvv,ssn,secret

# Mask pattern
commoncore.logging.mask-pattern=****
```

### Örnek

**Önceki Log (Maskelenmemiş):**
```json
{
  "body": {
    "username": "admin",
    "password": "super-secret-password-123",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

**Yeni Log (Maskelenmiş):**
```json
{
  "body": {
    "username": "admin",
    "password": "****",
    "token": "****"
  }
}
```

### Özelleştirme

```properties
# Custom mask pattern
commoncore.logging.mask-pattern=[REDACTED]

# Daha fazla hassas alan ekle
commoncore.logging.sensitive-fields=password,token,authorization,creditCard,cvv,ssn,secret,apiKey,privateKey
```

---

## Performance Monitoring

### Ne İşe Yarar?

Uygulama performansını izler: memory kullanımı, CPU kullanımı, execution time ve slow query detection.

### Aktif Etme

```properties
commoncore.monitoring.enabled=true
commoncore.monitoring.monitor-memory=true
commoncore.monitoring.monitor-cpu=true
commoncore.monitoring.monitor-db-query-time=true
commoncore.monitoring.slow-query-threshold=1000
```

### Örnek Performance Log

```json
{
  "timestamp": "2024-01-11T15:30:00.456Z",
  "method": "GET",
  "uri": "/api/scores",
  "status": 200,
  "executionTime": 45,
  "executionTimeUnit": "ms",
  "memoryUsed": 2621440,
  "memoryUsedUnit": "bytes",
  "memoryUsedMB": 2.5,
  "totalMemory": 536870912,
  "maxMemory": 2147483648,
  "cpuTimeUsed": 1000000,
  "cpuTimeUsedUnit": "ns"
}
```

### Slow Query Detection

Eğer execution time threshold'u aşarsa:

```
WARN - Slow request detected: GET /api/scores took 1200ms (threshold: 1000ms)
```

### Kullanım Senaryoları

1. **Performance Optimization**: Yavaş endpoint'leri tespit etme
2. **Memory Leak Detection**: Memory kullanımını izleme
3. **Capacity Planning**: Resource gereksinimlerini planlama
4. **Troubleshooting**: Performance sorunlarını hızlıca tespit etme

---

## Micrometer Metrics

### Ne İşe Yarar?

HTTP request metrics, custom business metrics ve Prometheus export desteği sağlar.

### Aktif Etme

```properties
commoncore.monitoring.enable-metrics=true
```

### Otomatik Toplanan Metrics

1. **`http.requests.total`** - HTTP request counter
   - Tags: `method`, `uri`, `status`

2. **`http.request.duration`** - Request duration timer
   - Tags: `method`, `uri`, `status`

3. **`http.request.memory.used`** - Memory usage gauge
   - Tags: `method`, `uri`

4. **`http.request.execution.time`** - Execution time gauge
   - Tags: `method`, `uri`

### Metrics Endpoint'lerini Kontrol Etme

```bash
# Tüm metrics listesi
curl http://localhost:8080/actuator/metrics

# HTTP request counter
curl http://localhost:8080/actuator/metrics/http.requests.total

# HTTP request duration
curl http://localhost:8080/actuator/metrics/http.request.duration

# Memory kullanımı
curl http://localhost:8080/actuator/metrics/http.request.memory.used
```

### Örnek Metrics Response

```json
{
  "name": "http.requests.total",
  "description": "Total HTTP requests",
  "measurements": [
    {
      "statistic": "COUNT",
      "value": 150
    }
  ],
  "availableTags": [
    {
      "tag": "method",
      "values": ["GET", "POST", "PUT", "DELETE"]
    },
    {
      "tag": "status",
      "values": ["200", "201", "404", "500"]
    }
  ]
}
```

---

## Prometheus Export

### Ne İşe Yarar?

Prometheus formatında metrics export, Grafana ile görselleştirme ve alerting için kullanılır.

### Aktif Etme

```properties
commoncore.monitoring.enable-prometheus=true

# Actuator endpoint'lerine prometheus ekle
management.endpoints.web.exposure.include=health,info,metrics,prometheus
```

### Prometheus Metrics'i Alma

```bash
curl http://localhost:8080/actuator/prometheus
```

### Örnek Prometheus Format

```
# HELP http_requests_total Total HTTP requests
# TYPE http_requests_total counter
http_requests_total{method="GET",status="200",uri="/api/scores"} 50.0
http_requests_total{method="POST",status="201",uri="/api/scores"} 25.0

# HELP http_request_duration_seconds HTTP request duration
# TYPE http_request_duration_seconds summary
http_request_duration_seconds{method="GET",uri="/api/scores",quantile="0.5"} 0.045
http_request_duration_seconds{method="GET",uri="/api/scores",quantile="0.99"} 0.120
```

### Prometheus + Grafana Entegrasyonu

1. Prometheus'u yapılandırın:
```yaml
scrape_configs:
  - job_name: 'goalsync'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8080']
```

2. Grafana'da dashboard oluşturun
3. Alerting kuralları tanımlayın

---

## Custom Metrics

### Ne İşe Yarar?

Business-specific metrics toplamak için kullanılır: custom counter, timer ve gauge metrics.

### CustomMetricsService Kullanımı

```java
@Service
@RequiredArgsConstructor
public class ScoreServiceImpl implements ScoreService {
    
    private final ScoreRepository scoreRepository;
    private final CustomMetricsService customMetricsService;
    
    @Override
    public ScoreDTO createScore(ScoreDTO scoreDTO) {
        long startTime = System.currentTimeMillis();
        
        // ... score oluşturma işlemi ...
        Score savedScore = scoreRepository.save(score);
        
        long duration = System.currentTimeMillis() - startTime;
        
        // Custom metrics kaydet
        customMetricsService.recordTimer(
            "score.creation.time", 
            duration, 
            TimeUnit.MILLISECONDS
        );
        
        customMetricsService.incrementCounter(
            "score.created", 
            "status", 
            scoreDTO.getStatus().name()
        );
        
        customMetricsService.recordBusinessMetric(
            "total.scores", 
            scoreRepository.count()
        );
        
        return convertToDTO(savedScore);
    }
}
```

### CustomMetricsService Metodları

#### 1. `incrementCounter(String name, String... tags)`

Counter metric'i artırır.

```java
customMetricsService.incrementCounter("score.created", "status", "FINISHED");
customMetricsService.incrementCounter("user.login", "provider", "google");
```

#### 2. `recordTimer(String name, long duration, TimeUnit unit, String... tags)`

Timer metric kaydeder.

```java
customMetricsService.recordTimer(
    "score.creation.time", 
    45, 
    TimeUnit.MILLISECONDS
);
```

#### 3. `recordGauge(String name, double value, String... tags)`

Gauge metric kaydeder.

```java
customMetricsService.recordGauge("active.users", 150.0);
customMetricsService.recordGauge("cache.size", 1024.0, "type", "redis");
```

#### 4. `recordBusinessMetric(String metricName, double value, String... tags)`

Business-specific metric kaydeder.

```java
customMetricsService.recordBusinessMetric("total.scores", 100.0);
customMetricsService.recordBusinessMetric("revenue", 50000.0, "currency", "USD");
```

---

## Kullanım Örnekleri

### Senaryo 1: Development Ortamında Detaylı Logging

```properties
# Development için detaylı logging
commoncore.logging.structured-logging=false
commoncore.logging.log-request-body=true
commoncore.logging.log-response-body=true
commoncore.logging.log-headers=true
commoncore.logging.sensitive-fields=password,token
```

### Senaryo 2: Production Ortamında Structured Logging

```properties
# Production için structured logging (ELK Stack entegrasyonu)
commoncore.logging.structured-logging=true
commoncore.logging.log-request-body=false
commoncore.logging.log-response-body=false
commoncore.logging.log-headers=false
commoncore.logging.sensitive-fields=password,token,authorization,creditCard,cvv,ssn,secret
```

### Senaryo 3: Performance Monitoring + Prometheus

```properties
# Performance monitoring aktif
commoncore.monitoring.enabled=true
commoncore.monitoring.monitor-memory=true
commoncore.monitoring.monitor-cpu=true
commoncore.monitoring.slow-query-threshold=500

# Prometheus export aktif
commoncore.monitoring.enable-prometheus=true
management.endpoints.web.exposure.include=health,info,metrics,prometheus
```

### Senaryo 4: Tam Özellikli Logging & Monitoring

```properties
# Structured logging
commoncore.logging.structured-logging=true
commoncore.logging.log-request-body=true
commoncore.logging.log-response-body=true
commoncore.logging.log-headers=true

# Sensitive data masking
commoncore.logging.sensitive-fields=password,token,authorization,creditCard,cvv,ssn,secret,apiKey

# Performance monitoring
commoncore.monitoring.enabled=true
commoncore.monitoring.monitor-memory=true
commoncore.monitoring.monitor-cpu=true
commoncore.monitoring.slow-query-threshold=1000

# Metrics
commoncore.monitoring.enable-metrics=true
commoncore.monitoring.enable-prometheus=true
```

---

## Test Senaryoları

### Test 1: Structured Logging Testi

```bash
# Request gönder
curl -X POST http://localhost:8080/api/scores \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer test-token" \
  -d '{
    "homeTeam": "Team A",
    "awayTeam": "Team B",
    "password": "secret123"
  }'

# Log'da JSON formatında ve password maskelenmiş olmalı
```

### Test 2: Performance Monitoring Testi

```bash
# Request gönder
curl -X GET http://localhost:8080/api/scores

# Log'da performance metrics görünmeli:
# - executionTime
# - memoryUsedMB
# - cpuTimeUsed
```

### Test 3: Metrics Testi

```bash
# Birkaç request gönder
curl -X GET http://localhost:8080/api/scores
curl -X POST http://localhost:8080/api/scores -d '{"homeTeam":"A","awayTeam":"B",...}'

# Metrics kontrol et
curl http://localhost:8080/actuator/metrics/http.requests.total
# Counter değeri artmış olmalı
```

### Test 4: Prometheus Export Testi

```bash
# Prometheus metrics'i al
curl http://localhost:8080/actuator/prometheus | grep http_requests_total

# Prometheus formatında metrics görünmeli
```

---

## Sorun Giderme

### Sorun 1: Structured Logging Çalışmıyor

**Çözüm:**
- `commoncore.logging.structured-logging=true` olduğundan emin olun
- Log output'unu kontrol edin (console veya log dosyası)

### Sorun 2: Request Body Loglanmıyor

**Çözüm:**
- `commoncore.logging.log-request-body=true` olduğundan emin olun
- `ContentCachingFilter` bean'inin yüklendiğinden emin olun
- Content-type'ın `loggable-content-types` listesinde olduğundan emin olun

### Sorun 3: Sensitive Data Maskelenmiyor

**Çözüm:**
- `commoncore.logging.sensitive-fields` listesinde field adının olduğundan emin olun
- Field adı case-insensitive kontrol edilir
- JSON içindeki nested field'lar da maskelenir

### Sorun 4: Metrics Görünmüyor

**Çözüm:**
- `commoncore.monitoring.enable-metrics=true` olduğundan emin olun
- `/actuator/metrics` endpoint'inin expose edildiğinden emin olun
- Uygulamaya birkaç request gönderin (metrics oluşması için)

### Sorun 5: Prometheus Export Çalışmıyor

**Çözüm:**
- `commoncore.monitoring.enable-prometheus=true` olduğundan emin olun
- `management.endpoints.web.exposure.include` listesinde `prometheus` olduğundan emin olun
- `/actuator/prometheus` endpoint'ine erişim izni olduğundan emin olun

---

## Best Practices

1. **Production'da Structured Logging Kullanın**
   - Log aggregation sistemleri ile entegrasyon için ideal

2. **Sensitive Data Masking'i Her Zaman Aktif Edin**
   - Password, token gibi verileri asla loglamayın

3. **Request Body Logging'i Dikkatli Kullanın**
   - Production'da sadece gerekli durumlarda aktif edin
   - Max body size limit'i kullanın

4. **Performance Monitoring'i Aktif Tutun**
   - Slow query detection için yararlı
   - Capacity planning için gerekli

5. **Prometheus Export'u Production'da Kullanın**
   - Grafana ile görselleştirme
   - Alerting kuralları tanımlama

6. **Custom Metrics'i Business Logic'e Ekleyin**
   - Business-specific metrics toplayın
   - KPI tracking için kullanın

---

## Ek Kaynaklar

- [COMPLETE_DOCUMENTATION.md](./COMPLETE_DOCUMENTATION.md) - Tüm özelliklerin kapsamlı dokümantasyonu
- [Micrometer Documentation](https://micrometer.io/docs) - Micrometer resmi dokümantasyonu
- [Prometheus Documentation](https://prometheus.io/docs/) - Prometheus resmi dokümantasyonu
