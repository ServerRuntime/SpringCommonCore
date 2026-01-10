# Pagination & Sorting Kullanım Kılavuzu

CommonCore, sayfalama ve sıralama için hazır bir mimari sağlar.

## Özellikler

- ✅ **PageRequest** - Request için pagination parametreleri
- ✅ **PageResponse** - Response için pagination bilgileri
- ✅ **Spring Data JPA Entegrasyonu** - Otomatik Pageable dönüşümü
- ✅ **Çoklu Sıralama** - Birden fazla field'a göre sıralama
- ✅ **Yapılandırılabilir** - Default sayfa boyutu ve maksimum limit

## Kullanım

### 1. Controller'da Kullanım

```java
import io.commoncore.dto.PageRequest;
import io.commoncore.dto.PageResponse;

@RestController
@RequestMapping("/api/scores")
public class ScoreController {
    
    private final ScoreService scoreService;
    
    @GetMapping
    public ResponseEntity<CustomResponse<PageResponse<ScoreDTO>>> getAllScores(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDirection) {
        
        PageRequest pageRequest = PageRequest.of(page, size, sortBy, sortDirection);
        PageResponse<ScoreDTO> scores = scoreService.getAllScores(pageRequest);
        return ResponseEntity.ok(CustomResponse.success(scores));
    }
}
```

### 2. Service'de Kullanım

```java
@Service
public class ScoreServiceImpl implements ScoreService {
    
    private final ScoreRepository scoreRepository;
    
    @Override
    public PageResponse<ScoreDTO> getAllScores(PageRequest pageRequest) {
        Page<Score> page = scoreRepository.findAll(pageRequest.toPageable());
        return PageResponse.of(page.map(this::convertToDTO));
    }
}
```

### 3. Repository'de Kullanım

```java
@Repository
public interface ScoreRepository extends JpaRepository<Score, Long> {
    
    // Pagination ile metod
    Page<Score> findByStatus(MatchStatus status, Pageable pageable);
    
    // Çoklu field'a göre sıralama ile
    Page<Score> findByHomeTeamContainingIgnoreCaseOrAwayTeamContainingIgnoreCase(
            String homeTeam, String awayTeam, Pageable pageable);
}
```

## API Kullanımı

### Basit Pagination

```bash
# İlk sayfa, 10 kayıt
GET /api/scores?page=0&size=10

# İkinci sayfa, 20 kayıt
GET /api/scores?page=1&size=20
```

### Sıralama ile Pagination

```bash
# ID'ye göre artan sıralama
GET /api/scores?page=0&size=10&sortBy=id&sortDirection=asc

# İsme göre azalan sıralama
GET /api/scores?page=0&size=10&sortBy=homeTeam&sortDirection=desc

# Tarihe göre sıralama
GET /api/scores?page=0&size=10&sortBy=matchDate&sortDirection=desc
```

### Çoklu Sıralama

```bash
# Birden fazla field'a göre sıralama
GET /api/scores?page=0&size=10&sort=status,asc&sort=matchDate,desc
```

## Response Formatı

```json
{
  "statusCode": 200,
  "data": {
    "content": [
      {
        "id": 1,
        "homeTeam": "Team A",
        "awayTeam": "Team B",
        "homeScore": 2,
        "awayScore": 1,
        "status": "FINISHED"
      }
    ],
    "totalElements": 50,
    "totalPages": 5,
    "page": 0,
    "size": 10,
    "first": true,
    "last": false,
    "empty": false,
    "numberOfElements": 10
  },
  "message": "Scores retrieved successfully"
}
```

## Yapılandırma

`application.properties`:

```properties
# Pagination Configuration
commoncore.pagination.enabled=true
commoncore.pagination.default-page-size=10
commoncore.pagination.max-page-size=100
commoncore.pagination.default-page=0
```

**Parametreler:**
- `enabled`: Pagination'ı aktif/pasif yapar (default: `true`)
- `default-page-size`: Varsayılan sayfa boyutu (default: `10`)
- `max-page-size`: Maksimum sayfa boyutu (default: `100`)
- `default-page`: Varsayılan sayfa numarası (default: `0`)

## PageRequest Metodları

### Factory Metodları

```java
// Basit pagination
PageRequest pageRequest = PageRequest.of(0, 10);

// Sıralama ile
PageRequest pageRequest = PageRequest.of(0, 10, "id", "asc");

// Manuel oluşturma
PageRequest pageRequest = new PageRequest();
pageRequest.setPage(0);
pageRequest.setSize(10);
pageRequest.setSortBy("id");
pageRequest.setSortDirection("desc");
```

### Spring Data Pageable'a Dönüştürme

```java
PageRequest pageRequest = PageRequest.of(0, 10, "id", "asc");
Pageable pageable = pageRequest.toPageable();
```

## PageResponse Metodları

### Factory Metodları

```java
// Spring Data Page'den
Page<Score> page = scoreRepository.findAll(pageable);
PageResponse<ScoreDTO> response = PageResponse.of(page.map(this::convertToDTO));

// List'ten (tüm veri tek sayfada)
List<ScoreDTO> scores = getAllScores();
PageResponse<ScoreDTO> response = PageResponse.of(scores);

// List ve PageRequest'ten
List<ScoreDTO> allScores = getAllScores();
PageRequest pageRequest = PageRequest.of(0, 10);
PageResponse<ScoreDTO> response = PageResponse.of(allScores, pageRequest);
```

## Örnek: Tam Entegrasyon

### Controller

```java
@GetMapping("/status/{status}")
public ResponseEntity<CustomResponse<PageResponse<ScoreDTO>>> getScoresByStatus(
        @PathVariable MatchStatus status,
        @RequestParam(required = false, defaultValue = "0") int page,
        @RequestParam(required = false, defaultValue = "10") int size,
        @RequestParam(required = false) String sortBy,
        @RequestParam(required = false, defaultValue = "asc") String sortDirection) {
    
    PageRequest pageRequest = PageRequest.of(page, size, sortBy, sortDirection);
    PageResponse<ScoreDTO> scores = scoreService.getScoresByStatus(status, pageRequest);
    return ResponseEntity.ok(CustomResponse.success(scores));
}
```

### Service

```java
@Override
public PageResponse<ScoreDTO> getScoresByStatus(MatchStatus status, PageRequest pageRequest) {
    Page<Score> page = scoreRepository.findByStatus(status, pageRequest.toPageable());
    return PageResponse.of(page.map(this::convertToDTO));
}
```

### Repository

```java
Page<Score> findByStatus(MatchStatus status, Pageable pageable);
```

## Sıralama Yönleri

- `asc` - Artan sıralama (A-Z, 0-9)
- `desc` - Azalan sıralama (Z-A, 9-0)

## Önemli Notlar

1. **Page numarası 0-indexed'dir** (0 = ilk sayfa)
2. **Maksimum sayfa boyutu** yapılandırmadan kontrol edilir
3. **Sıralama field'ları** entity field isimleriyle eşleşmeli
4. **Çoklu sıralama** için `sort` parametresi kullanılabilir

## Sorun Giderme

### Sıralama çalışmıyor

- Field isminin doğru olduğundan emin olun
- Repository metodunda `Pageable` parametresi olduğundan emin olun
- Entity'de field'ın var olduğundan emin olun

### Pagination çalışmıyor

- `commoncore.pagination.enabled=true` olduğundan emin olun
- Repository metodunun `Page` döndürdüğünden emin olun
- Service metodunun `PageResponse` döndürdüğünden emin olun
