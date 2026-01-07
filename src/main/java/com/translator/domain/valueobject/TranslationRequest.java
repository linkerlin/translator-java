package com.translator.domain.valueobject;

/**
 * 翻译请求值对象
 * 封装翻译请求的参数
 */
public class TranslationRequest {
    private final String text;
    private final String sourceLanguage;
    private final String targetLanguage;
    private final TranslationProvider provider;

    public TranslationRequest(String text, String sourceLanguage, 
                            String targetLanguage, TranslationProvider provider) {
        this.text = text;
        this.sourceLanguage = sourceLanguage;
        this.targetLanguage = targetLanguage;
        this.provider = provider;
    }

    // Getters
    public String getText() {
        return text;
    }

    public String getSourceLanguage() {
        return sourceLanguage;
    }

    public String getTargetLanguage() {
        return targetLanguage;
    }

    public TranslationProvider getProvider() {
        return provider;
    }
}