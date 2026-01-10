# CommonCore Interceptor Kullanımı

## Otomatik Yapılandırma

CommonCore, Spring Boot'un **Auto-Configuration** mekanizmasını kullanarak interceptor'ları otomatik olarak yükler.

## Nasıl Çalışır?

1. **Auto-Configuration Dosyası**: 
   - `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
   - Bu dosya Spring Boot'a `CommonCoreAutoConfiguration` sınıfını otomatik yüklemesini söyler

2. **CommonCoreAutoConfiguration**:
   - `@AutoConfiguration` annotation'ı ile işaretlenmiş
   - `@ComponentScan(basePackages = "io.commoncore")` ile CommonCore paketlerini tarar
   - `LoggingInterceptor` bean'ini bulur ve interceptor registry'ye ekler

3. **LoggingInterceptor**:
   - `@Component` annotation'ı ile Spring bean olarak tanımlanmış
   - Tüm `/api/**` path'lerine otomatik olarak uygulanır

## GoalSync'te Kullanım

GoalSync projesinde **hiçbir ek yapılandırma gerekmez**. CommonCore dependency'si eklendiğinde:

1. Spring Boot başlatıldığında auto-configuration dosyasını okur
2. `CommonCoreAutoConfiguration` otomatik olarak yüklenir
3. `LoggingInterceptor` otomatik olarak kaydedilir
4. Tüm `/api/**` endpoint'leri otomatik olarak loglanır

## Özelleştirme

Eğer GoalSync'te farklı path pattern'ler eklemek isterseniz, kendi `WebMvcConfigurer` sınıfınızı oluşturabilirsiniz:

```java
@Configuration
public class GoalSyncWebConfig implements WebMvcConfigurer {
    
    private final LoggingInterceptor loggingInterceptor;
    
    public GoalSyncWebConfig(LoggingInterceptor loggingInterceptor) {
        this.loggingInterceptor = loggingInterceptor;
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // CommonCore'un default interceptor'ı
        registry.addInterceptor(loggingInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/ertan", "/h2-console/**");
    }
}
```

**Not**: Bu durumda `CommonCoreAutoConfiguration`'daki interceptor kaydı ile çakışma olabilir. Bu durumda `CommonCoreAutoConfiguration`'ı exclude edebilirsiniz:

```java
@SpringBootApplication(exclude = {CommonCoreAutoConfiguration.class})
public class GoalSyncApplication {
    // ...
}
```

## Test Etme

Uygulamayı başlattığınızda loglarda şunları göreceksiniz:

```
INFO  - Incoming request: GET /api/scores from 127.0.0.1
INFO  - Request processed: GET /api/scores - Status: 200 - Time: 15ms
```
