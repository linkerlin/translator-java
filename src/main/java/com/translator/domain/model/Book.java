package com.translator.domain.model;

import com.translator.domain.valueobject.BookMetadata;
import com.translator.domain.valueobject.TranslationStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 书籍聚合根
 * 表示一本EPUB书籍，包含元数据和页面内容
 */
public class Book {
    private final String id;
    private final String originalFileName;
    private String translatedFileName;
    private BookMetadata metadata;
    private List<Page> pages;
    private TranslationStatus translationStatus;

    public Book(String originalFileName) {
        this.id = UUID.randomUUID().toString();
        this.originalFileName = originalFileName;
        this.pages = new ArrayList<>();
        this.translationStatus = TranslationStatus.PENDING;
        generateTranslatedFileName();
    }

    private void generateTranslatedFileName() {
        if (originalFileName.endsWith(".epub")) {
            String baseName = originalFileName.substring(0, originalFileName.length() - 5);
            this.translatedFileName = baseName + " 中文版.epub";
        } else {
            this.translatedFileName = originalFileName + " 中文版";
        }
    }

    public void addPage(Page page) {
        this.pages.add(page);
    }

    public void updateMetadata(BookMetadata metadata) {
        this.metadata = metadata;
    }

    public void markTranslationStarted() {
        this.translationStatus = TranslationStatus.IN_PROGRESS;
    }

    public void markTranslationCompleted() {
        this.translationStatus = TranslationStatus.COMPLETED;
    }

    public void markTranslationFailed(String errorMessage) {
        this.translationStatus = TranslationStatus.FAILED;
    }

    public boolean isTranslationCompleted() {
        return translationStatus == TranslationStatus.COMPLETED;
    }

    public int getTotalPages() {
        return pages.size();
    }

    public int getTranslatedPages() {
        return (int) pages.stream().filter(Page::isTranslated).count();
    }

    public double getTranslationProgress() {
        if (pages.isEmpty()) return 0.0;
        return (double) getTranslatedPages() / getTotalPages() * 100;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public String getTranslatedFileName() {
        return translatedFileName;
    }

    public BookMetadata getMetadata() {
        return metadata;
    }

    public List<Page> getPages() {
        return new ArrayList<>(pages);
    }

    public TranslationStatus getTranslationStatus() {
        return translationStatus;
    }
}