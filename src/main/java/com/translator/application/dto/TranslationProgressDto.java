package com.translator.application.dto;

import com.translator.domain.model.Book;

/**
 * 翻译进度数据传输对象
 */
public class TranslationProgressDto {
    private String bookId;
    private String bookName;
    private String status;
    private int totalPages;
    private int translatedPages;
    private double progressPercentage;
    private String currentPage;
    private String estimatedTimeRemaining;
    
    public static TranslationProgressDto fromDomain(Book book) {
        TranslationProgressDto dto = new TranslationProgressDto();
        dto.bookId = book.getId();
        dto.bookName = book.getOriginalFileName();
        dto.status = book.getTranslationStatus().getDescription();
        dto.totalPages = book.getTotalPages();
        dto.translatedPages = book.getTranslatedPages();
        dto.progressPercentage = book.getTranslationProgress();
        
        // 计算当前页面和预计剩余时间（简化实现）
        if (book.getTotalPages() > 0) {
            int currentPageIndex = book.getTranslatedPages();
            if (currentPageIndex < book.getTotalPages()) {
                dto.currentPage = "第 " + (currentPageIndex + 1) + " 页，共 " + book.getTotalPages() + " 页";
                
                // 简单的剩余时间估算（假设每页30秒）
                int remainingPages = book.getTotalPages() - book.getTranslatedPages();
                int remainingSeconds = remainingPages * 30;
                dto.estimatedTimeRemaining = formatTimeRemaining(remainingSeconds);
            } else {
                dto.currentPage = "翻译完成";
                dto.estimatedTimeRemaining = "已完成";
            }
        }
        
        return dto;
    }
    
    private static String formatTimeRemaining(int seconds) {
        if (seconds < 60) {
            return seconds + " 秒";
        } else if (seconds < 3600) {
            int minutes = seconds / 60;
            int remainingSeconds = seconds % 60;
            return minutes + " 分 " + remainingSeconds + " 秒";
        } else {
            int hours = seconds / 3600;
            int minutes = (seconds % 3600) / 60;
            return hours + " 小时 " + minutes + " 分";
        }
    }
    
    // Getters and Setters
    public String getBookId() {
        return bookId;
    }
    
    public void setBookId(String bookId) {
        this.bookId = bookId;
    }
    
    public String getBookName() {
        return bookName;
    }
    
    public void setBookName(String bookName) {
        this.bookName = bookName;
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
    
    public double getProgressPercentage() {
        return progressPercentage;
    }
    
    public void setProgressPercentage(double progressPercentage) {
        this.progressPercentage = progressPercentage;
    }
    
    public String getCurrentPage() {
        return currentPage;
    }
    
    public void setCurrentPage(String currentPage) {
        this.currentPage = currentPage;
    }
    
    public String getEstimatedTimeRemaining() {
        return estimatedTimeRemaining;
    }
    
    public void setEstimatedTimeRemaining(String estimatedTimeRemaining) {
        this.estimatedTimeRemaining = estimatedTimeRemaining;
    }
}