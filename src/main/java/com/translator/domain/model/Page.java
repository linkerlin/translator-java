package com.translator.domain.model;

/**
 * 页面实体
 * 表示书籍中的一个页面或章节
 */
public class Page {
    private final String id;
    private final int order;
    private final String title;
    private String originalContent;
    private String translatedContent;
    private boolean isTranslated;

    public Page(String id, int order, String title, String originalContent) {
        this.id = id;
        this.order = order;
        this.title = title;
        this.originalContent = originalContent;
        this.translatedContent = "";
        this.isTranslated = false;
    }

    public void translate(String translatedContent) {
        this.translatedContent = translatedContent;
        this.isTranslated = true;
    }

    public boolean hasContent() {
        return originalContent != null && !originalContent.trim().isEmpty();
    }

    public int getContentLength() {
        return originalContent != null ? originalContent.length() : 0;
    }

    // Getters
    public String getId() {
        return id;
    }

    public int getOrder() {
        return order;
    }

    public String getTitle() {
        return title;
    }

    public String getOriginalContent() {
        return originalContent;
    }

    public String getTranslatedContent() {
        return translatedContent;
    }

    public boolean isTranslated() {
        return isTranslated;
    }
}