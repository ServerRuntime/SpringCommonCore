package io.commoncore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Pagination response DTO
 * Sayfalanmış veri response'unu tutar
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    
    /**
     * Sayfa içeriği (veri listesi)
     */
    private List<T> content;
    
    /**
     * Toplam kayıt sayısı
     */
    private long totalElements;
    
    /**
     * Toplam sayfa sayısı
     */
    private int totalPages;
    
    /**
     * Mevcut sayfa numarası (0-indexed)
     */
    private int page;
    
    /**
     * Sayfa başına kayıt sayısı
     */
    private int size;
    
    /**
     * İlk sayfa mı?
     */
    private boolean first;
    
    /**
     * Son sayfa mı?
     */
    private boolean last;
    
    /**
     * Boş mu?
     */
    private boolean empty;
    
    /**
     * Toplam sayfa sayısı
     */
    private int numberOfElements;
    
    /**
     * Spring Data JPA Page'den PageResponse oluşturur
     */
    public static <T> PageResponse<T> of(org.springframework.data.domain.Page<T> page) {
        PageResponse<T> response = new PageResponse<>();
        response.setContent(page.getContent());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setPage(page.getNumber());
        response.setSize(page.getSize());
        response.setFirst(page.isFirst());
        response.setLast(page.isLast());
        response.setEmpty(page.isEmpty());
        response.setNumberOfElements(page.getNumberOfElements());
        return response;
    }
    
    /**
     * List'ten PageResponse oluşturur (tüm veri tek sayfada)
     */
    public static <T> PageResponse<T> of(List<T> content) {
        PageResponse<T> response = new PageResponse<>();
        response.setContent(content);
        response.setTotalElements(content.size());
        response.setTotalPages(1);
        response.setPage(0);
        response.setSize(content.size());
        response.setFirst(true);
        response.setLast(true);
        response.setEmpty(content.isEmpty());
        response.setNumberOfElements(content.size());
        return response;
    }
    
    /**
     * List ve PageRequest'ten PageResponse oluşturur
     */
    public static <T> PageResponse<T> of(List<T> allContent, PageRequest pageRequest) {
        int totalElements = allContent.size();
        int page = pageRequest.getPage();
        int size = pageRequest.getSize();
        int start = page * size;
        int end = Math.min(start + size, totalElements);
        
        List<T> pageContent = start < totalElements 
            ? allContent.subList(start, end)
            : List.of();
        
        int totalPages = (int) Math.ceil((double) totalElements / size);
        
        PageResponse<T> response = new PageResponse<>();
        response.setContent(pageContent);
        response.setTotalElements(totalElements);
        response.setTotalPages(totalPages);
        response.setPage(page);
        response.setSize(size);
        response.setFirst(page == 0);
        response.setLast(page >= totalPages - 1);
        response.setEmpty(pageContent.isEmpty());
        response.setNumberOfElements(pageContent.size());
        return response;
    }
}
