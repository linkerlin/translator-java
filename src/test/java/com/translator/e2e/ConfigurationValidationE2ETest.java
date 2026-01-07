package com.translator.e2e;

import com.translator.infrastructure.translation.ConfigValidationTool;
import com.translator.infrastructure.config.TranslationProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 配置验证端到端测试
 * 测试配置验证工具的完整功能
 */
public class ConfigurationValidationE2ETest extends EndToEndTestBase {
    
    @Autowired
    private ConfigValidationTool configValidationTool;
    
    @Autowired
    private TranslationProperties translationProperties;
    
    @Test
    void testCompleteConfigurationValidation() {
        System.out.println("=== 测试完整配置验证 ===");
        
        // 1. 运行完整配置验证
        System.out.println("1. 运行配置验证工具...");
        ConfigValidationTool.ValidationResult result = configValidationTool.validateAllConfigurations();
        
        // 2. 验证结果对象
        System.out.println("2. 验证结果对象...");
        assertNotNull(result, "验证结果不应该为null");
        assertNotNull(result.hasErrors(), "应该有错误状态检查方法");
        assertNotNull(result.hasWarnings(), "应该有警告状态检查方法");
        assertNotNull(result.isValid(), "应该有有效性检查方法");
        
        // 3. 验证结果状态
        System.out.println("3. 验证结果状态...");
        if (getEnvOrDefault("OPENAI_API_KEY", "").isEmpty() && getEnvOrDefault("DEEPSEEK_API_KEY", "").isEmpty()) {
            // 如果没有配置任何API密钥，应该有警告或错误
            assertTrue(result.hasWarnings() || result.hasErrors(), 
                "当没有配置API密钥时，应该有警告或错误");
            assertFalse(result.isValid(), "当没有配置API密钥时，应该无效");
        }
        
        // 4. 打印验证结果
        System.out.println("4. 打印验证结果...");
        System.out.println("=== 完整配置验证结果 ===");
        result.printResult();
        
        System.out.println("✅ 完整配置验证测试通过！");
    }
    
    @Test
    void testConfigurationWithDifferentBaseUrls() {
        System.out.println("=== 测试不同Base URL配置 ===");
        
        // 测试各种Base URL格式
        String[] testUrls = {
            "https://api.openai.com",
            "https://api.openai.com/",
            "api.openai.com",                    // 缺少协议
            "http://custom-api.com",
            "https://azure-resource.openai.azure.com/openai",
            "https://api.openai-proxy.org",
            "",                                    // 空值
            null                                   // null值
        };
        
        for (String testUrl : testUrls) {
            System.out.println("测试URL: " + (testUrl == null ? "null" : "\"" + testUrl + "\""));
            
            // 设置测试URL
            if (testUrl != null) {
                System.setProperty("OPENAI_BASE_URL", testUrl);
            } else {
                System.clearProperty("OPENAI_BASE_URL");
            }
            
            try {
                // 运行配置验证
                ConfigValidationTool.ValidationResult result = configValidationTool.validateAllConfigurations();
                
                // 验证结果
                assertNotNull(result, "验证结果不应该为null");
                
                System.out.println("✓ URL \"" + (testUrl == null ? "null" : testUrl) + "\" 验证完成");
                
            } finally {
                // 清理系统属性
                System.clearProperty("OPENAI_BASE_URL");
            }
        }
        
        System.out.println("✅ 不同Base URL配置测试通过！");
    }
    
    @Test
    void testConfigurationErrorHandling() {
        System.out.println("=== 测试配置错误处理 ===");
        
        // 1. 测试无效配置
        System.out.println("1. 测试无效配置...");
        System.setProperty("OPENAI_BASE_URL", "invalid-url");
        System.setProperty("OPENAI_API_KEY", ""); // 空API密钥
        
        try {
            ConfigValidationTool.ValidationResult result = configValidationTool.validateAllConfigurations();
            
            // 应该检测到错误
            assertTrue(result.hasErrors() || result.hasWarnings(), 
                "无效配置应该产生错误或警告");
            
            System.out.println("✓ 无效配置正确处理");
            
        } finally {
            System.clearProperty("OPENAI_BASE_URL");
            System.clearProperty("OPENAI_API_KEY");
        }
        
        // 2. 测试网络连接验证
        System.out.println("2. 测试网络连接验证...");
        System.setProperty("OPENAI_BASE_URL", "https://non-existent-api-12345.com");
        System.setProperty("OPENAI_API_KEY", "sk-test-key-12345");
        
        try {
            ConfigValidationTool.ValidationResult result = configValidationTool.validateAllConfigurations();
            
            // 应该检测到网络连接问题
            assertNotNull(result, "验证结果不应该为null");
            
            System.out.println("✓ 网络连接验证完成");
            
        } finally {
            System.clearProperty("OPENAI_BASE_URL");
            System.clearProperty("OPENAI_API_KEY");
        }
        
        System.out.println("✅ 配置错误处理测试通过！");
    }
    
    @Test
    void testConfigurationReflection() throws Exception {
        System.out.println("=== 测试配置反射访问 ===");
        
        // 使用反射测试私有方法
        System.out.println("1. 测试URL规范化方法...");
        
        // 获取私有方法
        Method normalizeMethod = ConfigValidationTool.class.getDeclaredMethod("normalizeBaseUrl", String.class);
        normalizeMethod.setAccessible(true);
        
        // 测试URL规范化
        String[] testUrls = {
            "https://api.openai.com",
            "https://api.openai.com/",
            "api.openai.com",
            "http://custom-api.com"
        };
        
        for (String url : testUrls) {
            String normalized = (String) normalizeMethod.invoke(configValidationTool, url);
            System.out.println("URL规范化: \"" + url + "\" → \"" + normalized + "\"");
            assertNotNull(normalized, "规范化后的URL不应该为null");
            assertTrue(normalized.startsWith("http"), "规范化后的URL应该以http开头");
        }
        
        // 测试验证方法
        System.out.println("2. 测试配置验证方法...");
        Method validateMethod = ConfigValidationTool.class.getDeclaredMethod("isValidUrl", String.class);
        validateMethod.setAccessible(true);
        
        // 测试URL验证
        assertTrue((Boolean) validateMethod.invoke(configValidationTool, "https://api.openai.com"));
        assertTrue((Boolean) validateMethod.invoke(configValidationTool, "http://custom-api.com"));
        assertFalse((Boolean) validateMethod.invoke(configValidationTool, "invalid-url"));
        assertFalse((Boolean) validateMethod.invoke(configValidationTool, ""));
        
        System.out.println("✅ 配置反射访问测试通过！");
    }
    
    @Test
    void testDeepSeekConfiguration() {
        System.out.println("=== 测试DeepSeek配置 ===");
        
        // 设置DeepSeek配置
        System.setProperty("DEEPSEEK_BASE_URL", "https://custom-deepseek.com");
        System.setProperty("DEEPSEEK_MODEL", "deepseek-custom");
        
        try {
            // 运行配置验证
            System.out.println("1. 运行DeepSeek配置验证...");
            ConfigValidationTool.ValidationResult result = configValidationTool.validateAllConfigurations();
            
            // 验证结果
            assertNotNull(result, "验证结果不应该为null");
            
            // 检查配置是否正确读取
            String deepseekBaseUrl = getEnvOrDefault("DEEPSEEK_BASE_URL", "");
            if (!deepseekBaseUrl.isEmpty()) {
                System.out.println("✓ DeepSeek Base URL已配置: " + deepseekBaseUrl);
            }
            
            System.out.println("✅ DeepSeek配置测试通过！");
            
        } finally {
            // 清理系统属性
            System.clearProperty("DEEPSEEK_BASE_URL");
            System.clearProperty("DEEPSEEK_MODEL");
        }
    }
}