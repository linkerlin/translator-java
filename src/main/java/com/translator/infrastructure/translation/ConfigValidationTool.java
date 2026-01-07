package com.translator.infrastructure.translation;

import com.translator.domain.valueobject.TranslationProvider;
import com.translator.infrastructure.config.TranslationProperties;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * 配置验证工具
 * 用于验证OpenAI和DeepSeek的API配置是否正确
 */
public class ConfigValidationTool {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigValidationTool.class);
    
    private final TranslationProperties properties;
    private final CloseableHttpClient httpClient;
    
    public ConfigValidationTool(TranslationProperties properties) {
        this.properties = properties;
        this.httpClient = HttpClients.createDefault();
    }
    
    /**
     * 验证所有配置
     */
    public ValidationResult validateAllConfigurations() {
        logger.info("开始验证所有翻译服务配置...");
        
        ValidationResult result = new ValidationResult();
        
        // 验证OpenAI配置
        try {
            validateOpenAIConfiguration(result);
        } catch (Exception e) {
            logger.error("验证OpenAI配置时出错", e);
            result.addError("OpenAI", "验证配置时出错: " + e.getMessage());
        }
        
        // 验证DeepSeek配置
        try {
            validateDeepSeekConfiguration(result);
        } catch (Exception e) {
            logger.error("验证DeepSeek配置时出错", e);
            result.addError("DeepSeek", "验证配置时出错: " + e.getMessage());
        }
        
        logger.info("配置验证完成");
        return result;
    }
    
    /**
     * 验证OpenAI配置
     */
    private void validateOpenAIConfiguration(ValidationResult result) {
        TranslationProperties.ProviderConfig config = properties.getApi().getOpenai();
        
        if (config == null || config.getApiKey() == null || config.getApiKey().isEmpty()) {
            result.addWarning("OpenAI", "API密钥未配置");
            return;
        }
        
        String baseUrl = config.getBaseUrl();
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            baseUrl = "https://api.openai.com";
        }
        
        logger.info("验证OpenAI配置 - Base URL: {}, Model: {}", baseUrl, config.getModel());
        
        // 验证URL格式
        if (!isValidUrl(baseUrl)) {
            result.addError("OpenAI", "Base URL格式无效: " + baseUrl);
            return;
        }
        
        // 测试网络连接
        boolean canConnect = testConnection(baseUrl, "OpenAI");
        if (canConnect) {
            result.addSuccess("OpenAI", "网络连接正常");
        } else {
            result.addWarning("OpenAI", "无法连接到API服务，请检查网络或URL配置");
        }
        
        // 验证API密钥格式（基本检查）
        if (config.getApiKey().startsWith("sk-") || config.getApiKey().length() > 20) {
            result.addSuccess("OpenAI", "API密钥格式正确");
        } else {
            result.addWarning("OpenAI", "API密钥格式可能不正确");
        }
        
        // 验证模型配置
        if (config.getModel() != null && !config.getModel().isEmpty()) {
            result.addSuccess("OpenAI", "模型已配置: " + config.getModel());
        } else {
            result.addWarning("OpenAI", "模型未配置，将使用默认值");
        }
    }
    
    /**
     * 验证DeepSeek配置
     */
    private void validateDeepSeekConfiguration(ValidationResult result) {
        TranslationProperties.ProviderConfig config = properties.getApi().getDeepseek();
        
        if (config == null || config.getApiKey() == null || config.getApiKey().isEmpty()) {
            result.addWarning("DeepSeek", "API密钥未配置");
            return;
        }
        
        String baseUrl = config.getBaseUrl();
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            baseUrl = "https://api.deepseek.com";
        }
        
        logger.info("验证DeepSeek配置 - Base URL: {}, Model: {}", baseUrl, config.getModel());
        
        // 验证URL格式
        if (!isValidUrl(baseUrl)) {
            result.addError("DeepSeek", "Base URL格式无效: " + baseUrl);
            return;
        }
        
        // 测试网络连接
        boolean canConnect = testConnection(baseUrl, "DeepSeek");
        if (canConnect) {
            result.addSuccess("DeepSeek", "网络连接正常");
        } else {
            result.addWarning("DeepSeek", "无法连接到API服务，请检查网络或URL配置");
        }
        
        // 验证API密钥格式
        if (config.getApiKey().length() > 10) {
            result.addSuccess("DeepSeek", "API密钥已配置");
        } else {
            result.addWarning("DeepSeek", "API密钥格式可能不正确");
        }
        
        // 验证模型配置
        if (config.getModel() != null && !config.getModel().isEmpty()) {
            result.addSuccess("DeepSeek", "模型已配置: " + config.getModel());
        } else {
            result.addWarning("DeepSeek", "模型未配置，将使用默认值");
        }
    }
    
    /**
     * 测试网络连接
     */
    private boolean testConnection(String baseUrl, String serviceName) {
        try {
            // 规范化Base URL：去掉尾部斜杠
            String normalizedBaseUrl = baseUrl != null ? baseUrl.trim() : "";
            if (normalizedBaseUrl.endsWith("/")) {
                normalizedBaseUrl = normalizedBaseUrl.substring(0, normalizedBaseUrl.length() - 1);
            }
            
            // 尝试访问根路径或API基础路径
            String testUrl = normalizedBaseUrl;
            if (!testUrl.endsWith("/v1") && !testUrl.endsWith("/openai")) {
                testUrl = testUrl + "/v1";
            }
            
            logger.debug("测试{}连接 - URL: {}", serviceName, testUrl);
            
            HttpGet httpGet = new HttpGet(URI.create(testUrl));
            httpGet.setHeader("User-Agent", "EPUB-Translator/1.0");
            
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                int statusCode = response.getCode();
                logger.debug("{}连接测试 - 状态码: {}", serviceName, statusCode);
                
                // 200表示正常，401表示需要认证（也是正常的）
                return statusCode == 200 || statusCode == 401;
            }
        } catch (Exception e) {
            logger.warn("{}连接测试失败: {}", serviceName, e.getMessage());
            return false;
        }
    }
    
    /**
     * 验证URL格式
     */
    private boolean isValidUrl(String url) {
        try {
            URI uri = URI.create(url);
            return uri.getScheme() != null && (uri.getScheme().equals("http") || uri.getScheme().equals("https"));
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private final java.util.List<String> successes = new java.util.ArrayList<>();
        private final java.util.List<String> warnings = new java.util.ArrayList<>();
        private final java.util.List<String> errors = new java.util.ArrayList<>();
        
        public void addSuccess(String service, String message) {
            successes.add("[" + service + "] " + message);
        }
        
        public void addWarning(String service, String message) {
            warnings.add("[" + service + "] " + message);
        }
        
        public void addError(String service, String message) {
            errors.add("[" + service + "] " + message);
        }
        
        public boolean hasErrors() {
            return !errors.isEmpty();
        }
        
        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }
        
        public boolean isValid() {
            return errors.isEmpty();
        }
        
        public void printResult() {
            Logger logger = LoggerFactory.getLogger(ValidationResult.class);
            
            logger.info("=== 配置验证结果 ===");
            
            if (!successes.isEmpty()) {
                logger.info("成功:");
                for (String success : successes) {
                    logger.info("  ✓ {}", success);
                }
            }
            
            if (!warnings.isEmpty()) {
                logger.warn("警告:");
                for (String warning : warnings) {
                    logger.warn("  ⚠ {}", warning);
                }
            }
            
            if (!errors.isEmpty()) {
                logger.error("错误:");
                for (String error : errors) {
                    logger.error("  ✗ {}", error);
                }
            }
            
            if (isValid()) {
                logger.info("配置验证通过！可以正常使用翻译服务。");
            } else {
                logger.error("配置验证失败！请修复错误后重新运行。");
            }
        }
    }
}