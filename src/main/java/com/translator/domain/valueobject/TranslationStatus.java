package com.translator.domain.valueobject;

/**
 * 翻译状态枚举
 */
public enum TranslationStatus {
    PENDING("待翻译"),
    IN_PROGRESS("翻译中"),
    COMPLETED("翻译完成"),
    FAILED("翻译失败");

    private final String description;

    TranslationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}