package com.translator.infrastructure.translation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.translator.domain.model.Page;
import com.translator.domain.service.TranslationService;
import com.translator.domain.valueobject.TranslationProvider;
import com.translator.domain.valueobject.TranslationRequest;
import com.translator.domain.exception.TranslationException;
import com.translator.infrastructure.config.TranslationProperties;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * OpenAI翻译服务实现
 */
@Service
public class OpenAITranslationService implements TranslationService {
    
    private static final Logger logger = LoggerFactory.getLogger(OpenAITranslationService.class);
    
    private final TranslationProperties properties;
    private final ObjectMapper objectMapper;
    private final CloseableHttpClient httpClient;
    private final PromptService promptService;
    
    public OpenAITranslationService(TranslationProperties properties, PromptService promptService) {
        this.properties = properties;
        this.promptService = promptService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_COMMENTS, true);
        this.objectMapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        this.httpClient = HttpClients.createDefault();
        
        // 记录配置信息
        logConfiguration();
    }
    
    /**
     * 规范化Base URL
     * 移除尾部斜杠，确保格式正确
     */
    private String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            logger.warn("Base URL为空，使用默认值");
            return "https://api.openai.com";
        }
        
        String normalized = baseUrl.trim();
        
        // 移除尾部斜杠
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        
        // 验证URL格式
        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            logger.warn("Base URL缺少协议前缀，自动添加https://");
            normalized = "https://" + normalized;
        }
        
        logger.debug("规范化Base URL: {} -> {}", baseUrl, normalized);
        return normalized;
    }
    
    /**
     * 验证API配置
     */
    private void validateApiConfig(TranslationProperties.ProviderConfig config, TranslationProvider provider) throws TranslationException {
        if (config == null) {
            throw new TranslationException(provider.getName() + " 配置为空");
        }
        
        if (config.getBaseUrl() == null || config.getBaseUrl().trim().isEmpty()) {
            throw new TranslationException(provider.getName() + " Base URL未配置");
        }
        
        if (config.getApiKey() == null || config.getApiKey().trim().isEmpty()) {
            throw new TranslationException(provider.getName() + " API密钥未配置");
        }
        
        if (config.getModel() == null || config.getModel().trim().isEmpty()) {
            throw new TranslationException(provider.getName() + " 模型未配置");
        }
        
        logger.debug("{} 配置验证通过", provider.getName());
    }
    
    private void logConfiguration() {
        try {
            TranslationProperties.ProviderConfig openaiConfig = properties.getApi().getOpenai();
            if (openaiConfig != null && openaiConfig.getBaseUrl() != null) {
                logger.info("OpenAI配置 - Base URL: {}", openaiConfig.getBaseUrl());
                logger.info("OpenAI配置 - Model: {}", openaiConfig.getModel());
                logger.info("OpenAI配置 - Max Tokens: {}", openaiConfig.getMaxTokens());
                logger.info("OpenAI配置 - Temperature: {}", openaiConfig.getTemperature());
                logger.info("OpenAI配置 - API Key: {}", openaiConfig.getApiKey() != null && !openaiConfig.getApiKey().isEmpty() ? "已配置" : "未配置");
            }
            
            TranslationProperties.ProviderConfig deepseekConfig = properties.getApi().getDeepseek();
            if (deepseekConfig != null && deepseekConfig.getBaseUrl() != null) {
                logger.info("DeepSeek配置 - Base URL: {}", deepseekConfig.getBaseUrl());
                logger.info("DeepSeek配置 - Model: {}", deepseekConfig.getModel());
                logger.info("DeepSeek配置 - API Key: {}", deepseekConfig.getApiKey() != null && !deepseekConfig.getApiKey().isEmpty() ? "已配置" : "未配置");
            }
        } catch (Exception e) {
            logger.warn("记录配置信息时出错", e);
        }
    }
    
    @Override
    public void translateBook(com.translator.domain.model.Book book, TranslationProvider provider) throws TranslationException {
        logger.info("开始翻译书籍: {}，共{}页", book.getOriginalFileName(), book.getTotalPages());
        
        List<Page> pages = book.getPages();
        int batchSize = properties.getBatchSize();
        
        for (int i = 0; i < pages.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, pages.size());
            List<Page> batch = pages.subList(i, endIndex);
            
            logger.info("翻译批次: {}-{}/{}", i + 1, endIndex, pages.size());
            translatePages(batch, provider);
            
            // 简单的进度报告
            double progress = (double) (i + batch.size()) / pages.size() * 100;
            logger.info("翻译进度: {:.1f}%", progress);
        }
        
        logger.info("书籍翻译完成: {}", book.getOriginalFileName());
    }
    
    @Override
    public void translatePage(Page page, TranslationProvider provider) throws TranslationException {
        if (!page.hasContent()) {
            logger.debug("页面无内容，跳过翻译: {}", page.getId());
            return;
        }
        
        String translatedText = translateText(page.getOriginalContent(), provider);
        page.translate(translatedText);
        
        logger.debug("页面翻译完成: {}", page.getId());
    }
    
    @Override
    public void translatePages(List<Page> pages, TranslationProvider provider) throws TranslationException {
        if (pages.isEmpty()) {
            return;
        }
        
        // 合并页面内容以提高翻译效率
        StringBuilder combinedContent = new StringBuilder();
        List<Integer> pageBoundaries = new ArrayList<>();
        
        for (Page page : pages) {
            if (pageBoundaries.size() > 0) {
                combinedContent.append("\n\n--- PAGE BREAK ---\n\n");
            }
            pageBoundaries.add(combinedContent.length());
            combinedContent.append(page.getOriginalContent());
        }
        pageBoundaries.add(combinedContent.length());
        
        // 翻译合并后的内容
        String translatedContent = translateText(combinedContent.toString(), provider);
        
        // 使用分隔符分割翻译结果
        // 注意：大模型可能会在分隔符前后添加额外的空白字符，或者稍微改变分隔符格式
        // 这里使用正则表达式来稍微放宽匹配条件
        String[] segments = translatedContent.split("\\s*--- PAGE BREAK ---\\s*");
        
        if (segments.length != pages.size()) {
            logger.warn("翻译结果的分段数量({})与页面数量({})不匹配。尝试按顺序分配，但这可能导致内容错位。", 
                segments.length, pages.size());
        }
        
        // 分配翻译结果
        for (int i = 0; i < Math.min(pages.size(), segments.length); i++) {
            String pageTranslation = segments[i].trim();
            pages.get(i).translate(pageTranslation);
        }
        
        // 如果分段少于页面数，剩下的页面可能没有被翻译
        if (segments.length < pages.size()) {
            logger.warn("有 {} 个页面未能在批量翻译中获得结果，保持原文。", pages.size() - segments.length);
        }
    }
    
    @Override
    public String detectLanguage(String text) throws TranslationException {
        // 简化实现，假设英文内容需要翻译成中文
        // 实际项目中应该使用语言检测API
        return "en";
    }
    
    @Override
    public boolean isServiceAvailable(TranslationProvider provider) {
        if (provider != TranslationProvider.OPENAI && provider != TranslationProvider.DEEPSEEK) {
            return false;
        }
        
        try {
            // 尝试进行简单的API调用来检查服务可用性
            String testTranslation = translateText("Hello", provider);
            return testTranslation != null && !testTranslation.isEmpty();
        } catch (Exception e) {
            logger.warn("翻译服务不可用: {}", provider.getName(), e);
            return false;
        }
    }
    
    /**
     * 测试翻译功能 - 用于配置验证
     */
    public String testTranslation(String text, TranslationProvider provider) throws TranslationException {
        return translateText(text, provider);
    }
    
    private String translateText(String text, TranslationProvider provider) throws TranslationException {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        
        TranslationProperties.ProviderConfig config = properties.getProviderConfig(provider);
        
        // 验证配置
        validateApiConfig(config, provider);
        
        logger.debug("翻译文本 - 长度: {}, 提供商: {}", text.length(), provider.getName());
        
        int retryCount = properties.getRetryCount();
        for (int attempt = 1; attempt <= retryCount; attempt++) {
            try {
                return callTranslationAPI(text, provider, config);
            } catch (Exception e) {
                logger.warn("LLM API调用失败 (尝试 {}/{}): {}", attempt, retryCount, e.getMessage());
                
                if (attempt < retryCount) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(properties.getRetryDelay());
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new TranslationException("翻译被中断", ie);
                    }
                } else {
                    throw new TranslationException("LLM API调用失败，已重试" + retryCount + "次: " + e.getMessage(), e);
                }
            }
        }
        
        throw new TranslationException("翻译失败");
    }
    
    private String callTranslationAPI(String text, TranslationProvider provider, TranslationProperties.ProviderConfig config) throws Exception {
        String baseUrl = normalizeBaseUrl(config.getBaseUrl());
        String apiUrl;
        
        // 如果baseUrl已经包含/v1，则不要重复添加
        if (baseUrl.endsWith("/v1")) {
            apiUrl = baseUrl + "/chat/completions";
        } else {
            apiUrl = baseUrl + "/v1/chat/completions";
        }
        
        logger.debug("调用 LLM API - URL: {}, Provider: {}, Model: {}", apiUrl, provider.getName(), config.getModel());
        
        // 构建OpenAI API请求
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", config.getModel());
        requestBody.put("temperature", config.getTemperature());
        requestBody.put("max_tokens", config.getMaxTokens());
        
        List<Map<String, String>> messages = new ArrayList<>();
        
        // 系统消息
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", promptService.getSystemPrompt());
        messages.add(systemMessage);
        
        // 用户消息
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", text);
        messages.add(userMessage);
        
        requestBody.put("messages", messages);
        
        HttpPost httpPost = new HttpPost(apiUrl);
        httpPost.setHeader("Authorization", "Bearer " + config.getApiKey());
        httpPost.setHeader("Content-Type", "application/json");
        
        String jsonBody = objectMapper.writeValueAsString(requestBody);
        httpPost.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
        
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            int statusCode = response.getCode();
            String responseBody = new String(response.getEntity().getContent().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            
            if (statusCode != 200) {
                throw new TranslationException("API调用失败: HTTP " + statusCode + " - " + responseBody);
            }
            
            // 解析响应
            Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
            
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> choice = choices.get(0);
                Map<String, String> message = (Map<String, String>) choice.get("message");
                if (message != null && message.containsKey("content")) {
                    return message.get("content").trim();
                }
            }
            
            throw new TranslationException("无法解析API响应");
        }
    }
}