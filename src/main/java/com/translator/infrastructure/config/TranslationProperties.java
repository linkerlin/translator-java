package com.translator.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 翻译配置属性
 */
@ConfigurationProperties(prefix = "translation")
public class TranslationProperties {
    
    private ApiConfig api = new ApiConfig();
    private TranslationSettings settings = new TranslationSettings();
    
    public static class ApiConfig {
        private ProviderConfig openai = new ProviderConfig();
        private ProviderConfig deepseek = new ProviderConfig();
        
        // Getters and Setters
        public ProviderConfig getOpenai() {
            return openai;
        }
        
        public void setOpenai(ProviderConfig openai) {
            this.openai = openai;
        }
        
        public ProviderConfig getDeepseek() {
            return deepseek;
        }
        
        public void setDeepseek(ProviderConfig deepseek) {
            this.deepseek = deepseek;
        }
    }
    
    public static class ProviderConfig {
        private String baseUrl;
        private String apiKey;
        private String model;
        private int maxTokens = 2000;
        private double temperature = 0.3;
        
        // Getters and Setters
        public String getBaseUrl() {
            return baseUrl;
        }
        
        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
        
        public String getApiKey() {
            return apiKey;
        }
        
        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
        
        public String getModel() {
            return model;
        }
        
        public void setModel(String model) {
            this.model = model;
        }
        
        public int getMaxTokens() {
            return maxTokens;
        }
        
        public void setMaxTokens(int maxTokens) {
            this.maxTokens = maxTokens;
        }
        
        public double getTemperature() {
            return temperature;
        }
        
        public void setTemperature(double temperature) {
            this.temperature = temperature;
        }
    }
    
    public static class TranslationSettings {
        private String defaultProvider = "openai";
        private int batchSize = 1;
        private int retryCount = 3;
        private long retryDelay = 1000;
        
        // Getters and Setters
        public String getDefaultProvider() {
            return defaultProvider;
        }
        
        public void setDefaultProvider(String defaultProvider) {
            this.defaultProvider = defaultProvider;
        }
        
        public int getBatchSize() {
            return batchSize;
        }
        
        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }
        
        public int getRetryCount() {
            return retryCount;
        }
        
        public void setRetryCount(int retryCount) {
            this.retryCount = retryCount;
        }
        
        public long getRetryDelay() {
            return retryDelay;
        }
        
        public void setRetryDelay(long retryDelay) {
            this.retryDelay = retryDelay;
        }
    }
    
    // Getters and Setters
    public ApiConfig getApi() {
        return api;
    }
    
    public void setApi(ApiConfig api) {
        this.api = api;
    }
    
    public TranslationSettings getSettings() {
        return settings;
    }
    
    public void setSettings(TranslationSettings settings) {
        this.settings = settings;
    }
    
    // 辅助方法
    public ProviderConfig getProviderConfig(com.translator.domain.valueobject.TranslationProvider provider) {
        switch (provider) {
            case OPENAI:
                return api.getOpenai();
            case DEEPSEEK:
                return api.getDeepseek();
            default:
                return null;
        }
    }
    
    public int getBatchSize() {
        return settings.getBatchSize();
    }
    
    public int getRetryCount() {
        return settings.getRetryCount();
    }
    
    public long getRetryDelay() {
        return settings.getRetryDelay();
    }
}