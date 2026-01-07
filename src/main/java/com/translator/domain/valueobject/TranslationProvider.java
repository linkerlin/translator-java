package com.translator.domain.valueobject;

/**
 * 翻译提供商枚举
 */
public enum TranslationProvider {
    OPENAI("OpenAI", "GPT-3.5-Turbo"),
    DEEPSEEK("DeepSeek", "DeepSeek-Chat");

    private final String name;
    private final String defaultModel;

    TranslationProvider(String name, String defaultModel) {
        this.name = name;
        this.defaultModel = defaultModel;
    }

    public String getName() {
        return name;
    }

    public String getDefaultModel() {
        return defaultModel;
    }
}