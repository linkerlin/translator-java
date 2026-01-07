package com.translator.infrastructure.translation;

import com.translator.domain.valueobject.TranslationProvider;
import com.translator.infrastructure.config.TranslationProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * OpenAI翻译服务配置测试
 * 测试自定义Base URL配置功能
 */
@ExtendWith(MockitoExtension.class)
public class OpenAITranslationServiceConfigTest {
    
    @Mock
    private TranslationProperties properties;
    
    @Mock
    private TranslationProperties.ApiConfig apiConfig;
    
    @Mock
    private TranslationProperties.ProviderConfig openaiConfig;
    
    @Mock
    private TranslationProperties.ProviderConfig deepseekConfig;
    
    private OpenAITranslationService translationService;
    
    @BeforeEach
    void setUp() {
        when(properties.getApi()).thenReturn(apiConfig);
        when(apiConfig.getOpenai()).thenReturn(openaiConfig);
        when(apiConfig.getDeepseek()).thenReturn(deepseekConfig);
        
        translationService = new OpenAITranslationService(properties);
    }
    
    @Test
    void testNormalizeBaseUrl() throws Exception {
        // 使用反射访问私有方法
        Method normalizeMethod = OpenAITranslationService.class.getDeclaredMethod("normalizeBaseUrl", String.class);
        normalizeMethod.setAccessible(true);
        
        // 测试标准URL
        assertEquals("https://api.openai.com", normalizeMethod.invoke(translationService, "https://api.openai.com"));
        assertEquals("https://api.openai.com", normalizeMethod.invoke(translationService, "https://api.openai.com/"));
        
        // 测试缺少协议的URL
        assertEquals("https://api.openai.com", normalizeMethod.invoke(translationService, "api.openai.com"));
        assertEquals("http://api.openai.com", normalizeMethod.invoke(translationService, "http://api.openai.com"));
        
        // 测试自定义URL
        assertEquals("https://custom-api.com", normalizeMethod.invoke(translationService, "https://custom-api.com"));
        assertEquals("https://custom-api.com", normalizeMethod.invoke(translationService, "https://custom-api.com/"));
        assertEquals("https://custom-api.com", normalizeMethod.invoke(translationService, "custom-api.com"));
        
        // 测试空值
        assertEquals("https://api.openai.com", normalizeMethod.invoke(translationService, ""));
        assertEquals("https://api.openai.com", normalizeMethod.invoke(translationService, (String) null));
    }
    
    @Test
    void testValidateApiConfig() throws Exception {
        // 使用反射访问私有方法
        Method validateMethod = OpenAITranslationService.class.getDeclaredMethod("validateApiConfig", 
            TranslationProperties.ProviderConfig.class, TranslationProvider.class);
        validateMethod.setAccessible(true);
        
        // 测试有效配置
        when(openaiConfig.getBaseUrl()).thenReturn("https://api.openai.com");
        when(openaiConfig.getApiKey()).thenReturn("sk-test-api-key");
        when(openaiConfig.getModel()).thenReturn("gpt-3.5-turbo");
        
        // 不应该抛出异常
        assertDoesNotThrow(() -> {
            validateMethod.invoke(translationService, openaiConfig, TranslationProvider.OPENAI);
        });
        
        // 测试无效配置 - 空API密钥
        when(openaiConfig.getApiKey()).thenReturn("");
        Exception exception = assertThrows(Exception.class, () -> {
            validateMethod.invoke(translationService, openaiConfig, TranslationProvider.OPENAI);
        });
        assertTrue(exception.getCause().getMessage().contains("API密钥未配置"));
        
        // 测试无效配置 - 空Base URL
        when(openaiConfig.getApiKey()).thenReturn("sk-test-api-key");
        when(openaiConfig.getBaseUrl()).thenReturn("");
        exception = assertThrows(Exception.class, () -> {
            validateMethod.invoke(translationService, openaiConfig, TranslationProvider.OPENAI);
        });
        assertTrue(exception.getCause().getMessage().contains("Base URL未配置"));
        
        // 测试无效配置 - 空模型
        when(openaiConfig.getBaseUrl()).thenReturn("https://api.openai.com");
        when(openaiConfig.getModel()).thenReturn("");
        exception = assertThrows(Exception.class, () -> {
            validateMethod.invoke(translationService, openaiConfig, TranslationProvider.OPENAI);
        });
        assertTrue(exception.getCause().getMessage().contains("模型未配置"));
    }
    
    @Test
    void testCustomBaseUrlConfiguration() throws Exception {
        // 测试自定义Base URL配置
        when(openaiConfig.getBaseUrl()).thenReturn("https://custom-api.example.com");
        when(openaiConfig.getApiKey()).thenReturn("custom-api-key");
        when(openaiConfig.getModel()).thenReturn("custom-model");
        
        // 验证配置可以通过验证
        Method validateMethod = OpenAITranslationService.class.getDeclaredMethod("validateApiConfig", 
            TranslationProperties.ProviderConfig.class, TranslationProvider.class);
        validateMethod.setAccessible(true);
        
        assertDoesNotThrow(() -> {
            validateMethod.invoke(translationService, openaiConfig, TranslationProvider.OPENAI);
        });
        
        // 测试URL规范化
        Method normalizeMethod = OpenAITranslationService.class.getDeclaredMethod("normalizeBaseUrl", String.class);
        normalizeMethod.setAccessible(true);
        
        String normalizedUrl = (String) normalizeMethod.invoke(translationService, "https://custom-api.example.com");
        assertEquals("https://custom-api.example.com", normalizedUrl);
    }
    
    @Test
    void testAzureOpenAiConfiguration() throws Exception {
        // 测试Azure OpenAI配置
        when(openaiConfig.getBaseUrl()).thenReturn("https://your-resource.openai.azure.com/openai");
        when(openaiConfig.getApiKey()).thenReturn("your-azure-api-key");
        when(openaiConfig.getModel()).thenReturn("gpt-35-turbo"); // Azure使用gpt-35-turbo
        
        // 验证配置可以通过验证
        Method validateMethod = OpenAITranslationService.class.getDeclaredMethod("validateApiConfig", 
            TranslationProperties.ProviderConfig.class, TranslationProvider.class);
        validateMethod.setAccessible(true);
        
        assertDoesNotThrow(() -> {
            validateMethod.invoke(translationService, openaiConfig, TranslationProvider.OPENAI);
        });
    }
    
    @Test
    void testConfigurationLogging() {
        // 测试配置日志输出
        when(openaiConfig.getBaseUrl()).thenReturn("https://custom-api.example.com");
        when(openaiConfig.getModel()).thenReturn("custom-model");
        when(openaiConfig.getMaxTokens()).thenReturn(3000);
        when(openaiConfig.getTemperature()).thenReturn(0.5);
        when(openaiConfig.getApiKey()).thenReturn("sk-test-api-key");
        
        when(deepseekConfig.getBaseUrl()).thenReturn("https://api.deepseek.com");
        when(deepseekConfig.getModel()).thenReturn("deepseek-chat");
        when(deepseekConfig.getApiKey()).thenReturn("deepseek-test-key");
        
        // 重新创建服务实例以触发配置日志
        OpenAITranslationService serviceWithConfig = new OpenAITranslationService(properties);
        
        // 验证服务创建成功（配置日志会在构造函数中输出）
        assertNotNull(serviceWithConfig);
    }
}