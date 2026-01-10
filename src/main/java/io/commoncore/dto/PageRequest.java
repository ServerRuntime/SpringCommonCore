package io.commoncore.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Pagination request DTO
 * Kullanıcıdan gelen pagination parametrelerini tutar
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageRequest {
    
    /**
     * Sayfa numarası (0-indexed)
     * Default: 0
     */
    @Min(value = 0, message = "Page number must be >= 0")
    private int page = 0;
    
    /**
     * Sayfa başına kayıt sayısı
     * Default: 10
     */
    @Min(value = 1, message = "Page size must be >= 1")
    @Max(value = 100, message = "Page size must be <= 100")
    private int size = 10;
    
    /**
     * Sıralama bilgileri
     * Format: "field,direction" (örn: "id,asc" veya "name,desc")
     * Default: boş liste
     */
    private List<String> sort = new ArrayList<>();
    
    /**
     * Sıralama için kısa yol
     * Tek bir field için kullanılabilir
     * Format: "field:direction" (örn: "id:asc" veya "name:desc")
     */
    private String sortBy;
    
    /**
     * Sıralama yönü (sortBy kullanıldığında)
     * asc veya desc
     */
    private String sortDirection = "asc";
    
    /**
     * Spring Data JPA Pageable'a dönüştürür
     */
    public org.springframework.data.domain.Pageable toPageable() {
        List<org.springframework.data.domain.Sort.Order> orders = new ArrayList<>();
        
        // sortBy varsa onu kullan
        if (sortBy != null && !sortBy.trim().isEmpty()) {
            org.springframework.data.domain.Sort.Direction direction = 
                "desc".equalsIgnoreCase(sortDirection) 
                    ? org.springframework.data.domain.Sort.Direction.DESC 
                    : org.springframework.data.domain.Sort.Direction.ASC;
            orders.add(new org.springframework.data.domain.Sort.Order(direction, sortBy));
        }
        
        // sort listesi varsa onları kullan
        if (sort != null && !sort.isEmpty()) {
            for (String sortItem : sort) {
                if (sortItem != null && !sortItem.trim().isEmpty()) {
                    String[] parts = sortItem.split(",");
                    if (parts.length >= 1) {
                        String field = parts[0].trim();
                        org.springframework.data.domain.Sort.Direction direction = 
                            (parts.length >= 2 && "desc".equalsIgnoreCase(parts[1].trim()))
                                ? org.springframework.data.domain.Sort.Direction.DESC
                                : org.springframework.data.domain.Sort.Direction.ASC;
                        orders.add(new org.springframework.data.domain.Sort.Order(direction, field));
                    }
                }
            }
        }
        
        org.springframework.data.domain.Sort sortObj = orders.isEmpty() 
            ? org.springframework.data.domain.Sort.unsorted()
            : org.springframework.data.domain.Sort.by(orders);
        
        return org.springframework.data.domain.PageRequest.of(page, size, sortObj);
    }
    
    /**
     * Varsayılan PageRequest oluşturur
     */
    public static PageRequest of(int page, int size) {
        PageRequest pageRequest = new PageRequest();
        pageRequest.setPage(page);
        pageRequest.setSize(size);
        return pageRequest;
    }
    
    /**
     * Sıralama ile PageRequest oluşturur
     */
    public static PageRequest of(int page, int size, String sortBy, String sortDirection) {
        PageRequest pageRequest = new PageRequest();
        pageRequest.setPage(page);
        pageRequest.setSize(size);
        pageRequest.setSortBy(sortBy);
        pageRequest.setSortDirection(sortDirection);
        return pageRequest;
    }
}
