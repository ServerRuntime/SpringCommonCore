# CommonCore Hızlı Başlangıç Kılavuzu

## Yeni Projede CommonCore Kullanımı (3 Adım)

### Adım 1: CommonCore'u Local Repository'ye Yükle

```bash
cd /Volumes/Sandisk/Java_Projects/CommonCore
./mvnw clean install
```

### Adım 2: Yeni Projenize Dependency Ekleyin

Yeni projenizin `pom.xml` dosyasına ekleyin:

```xml
<dependency>
    <groupId>io.commoncore</groupId>
    <artifactId>CommonCore</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### Adım 3: Kullanmaya Başlayın!

**Hiçbir ek yapılandırma gerekmez!** CommonCore otomatik olarak:

- ✅ Tüm `/api/**` endpoint'lerini loglar
- ✅ Exception'ları otomatik yakalar ve formatlar
- ✅ CustomResponse kullanımını sağlar

## Örnek Controller

```java
package com.myproject.controller;

import io.commoncore.dto.CustomResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping
    public ResponseEntity<CustomResponse<List<User>>> getAllUsers() {
        // CommonCore'dan CustomResponse kullanılıyor
        return ResponseEntity.ok(CustomResponse.success(users, "Users retrieved"));
    }
}
```

## Örnek Exception

```java
package com.myproject.exception;

import io.commoncore.exception.BaseNotFoundException;

public class UserNotFoundException extends BaseNotFoundException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
```

## Otomatik Özellikler

1. **Logging**: Tüm `/api/**` istekleri otomatik loglanır
2. **Exception Handling**: Tüm exception'lar otomatik yakalanır
3. **Response Format**: Standart CustomResponse formatı

**Hepsi bu kadar!** Başka bir şey yapmanıza gerek yok. 🚀
