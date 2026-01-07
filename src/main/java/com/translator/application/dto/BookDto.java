package com.translator.application.dto;

import com.translator.domain.model.Book;
import com.translator.domain.valueobject.BookMetadata;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 书籍数据传输对象
 */
public class BookDto {
    private String id;
    private String originalFileName;
    private String translatedFileName;
    private BookMetadata metadata;
    private String status;
    private int totalPages;
    private int translatedPages;
    private double translationProgress;
    private String outputPath;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    
    public static BookDto fromDomain(Book book, String outputPath) {
        BookDto dto = new BookDto();
        dto.id = book.getId();
        dto.originalFileName = book.getOriginalFileName();
        dto.translatedFileName = book.getTranslatedFileName();
        dto.metadata = book.getMetadata();
        dto.status = book.getTranslationStatus().getDescription();
        dto.totalPages = book.getTotalPages();
        dto.translatedPages = book.getTranslatedPages();
        dto.translationProgress = book.getTranslationProgress();
        dto.outputPath = outputPath;
        dto.createdAt = LocalDateTime.now(); // 简化实现
        if (book.isTranslationCompleted()) {
            dto.completedAt = LocalDateTime.now();
        }
        return dto;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getOriginalFileName() {
        return originalFileName;
    }
    
    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }
    
    public String getTranslatedFileName() {
        return translatedFileName;
    }
    
    public void setTranslatedFileName(String translatedFileName) {
        this.translatedFileName = translatedFileName;
    }
    
    public BookMetadata getMetadata() {
        return metadata;
    }
    
    public void setMetadata(BookMetadata metadata) {
        this.metadata = metadata;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public int getTotalPages() {
        return totalPages;
    }
    
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
    
    public int getTranslatedPages() {
        return translatedPages;
    }
    
    public void setTranslatedPages(int translatedPages) {
        this.translatedPages = translatedPages;
    }
    
    public double getTranslationProgress() {
        return translationProgress;
    }
    
    public void setTranslationProgress(double translationProgress) {
        this.translationProgress = translationProgress;
    }
    
    public String getOutputPath() {
        return outputPath;
    }
    
    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}