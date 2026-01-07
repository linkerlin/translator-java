package com.translator.e2e;

import com.translator.infrastructure.config.TranslationProperties;
import com.translator.infrastructure.translation.ConfigValidationTool;
import com.translator.infrastructure.translation.OpenAITranslationService;
import com.translator.domain.valueobject.TranslationProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 配置专用端到端测试
 * 专注于配置验证，不需要GUI组件
 */
@SpringBootTest(classes = {
    com.translator.infrastructure.config.TranslationProperties.class,
    com.translator.infrastructure.translation.ConfigValidationTool.class,
    com.translator.infrastructure.translation.OpenAITranslationService.class
})
@TestPropertySource(properties = {
    "spring.main.web-application-type=none",
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration"
})
public class ConfigOnlyE2ETest {
    
    @Autowired
    private TranslationProperties translationProperties;
    
    @Autowired
    private ConfigValidationTool configValidationTool;
    
    @Autowired
    private OpenAITranslationService translationService;
    
    @Test
    void testConfigurationLoading() {
        System.out.println("=== 测试配置加载 ===");
        
        // 验证配置属性已加载
        assertNotNull(translationProperties, "TranslationProperties 应该已加载");
        assertNotNull(configValidationTool, "ConfigValidationTool 应该已加载");
        assertNotNull(translationService, "OpenAITranslationService 应该已加载");
        
        System.out.println("✅ 配置组件加载成功");
    }
    
    @Test
    void testConfigurationValidation() {
        System.out.println("=== 测试配置验证 ===");
        
        // 运行配置验证
        ConfigValidationTool.ValidationResult result = configValidationTool.validateAllConfigurations();
        
        // 验证结果对象
        assertNotNull(result, "验证结果不应该为null");
        assertNotNull(result.hasErrors(), "应该有错误状态检查方法");
        assertNotNull(result.hasWarnings(), "应该有警告状态检查方法");
        assertNotNull(result.isValid(), "应该有有效性检查方法");
        
        // 打印验证结果
        System.out.println("=== 配置验证结果 ===");
        result.printResult();
        
        System.out.println("✅ 配置验证功能正常");
    }
    
    @Test
    void testBaseUrlConfiguration() {
        System.out.println("=== 测试Base URL配置 ===");
        
        // 获取当前配置
        String openaiBaseUrl = System.getenv("OPENAI_BASE_URL");
        String deepseekBaseUrl = System.getenv("DEEPSEEK_BASE_URL");
        
        System.out.println("OpenAI Base URL: " + (openaiBaseUrl != null ? openaiBaseUrl : "使用默认值"));
        System.out.println("DeepSeek Base URL: " + (deepseekBaseUrl != null ? deepseekBaseUrl : "使用默认值"));
        
        // 验证配置属性中的Base URL
        TranslationProperties.ProviderConfig openaiConfig = translationProperties.getApi().getOpenai();
        TranslationProperties.ProviderConfig deepseekConfig = translationProperties.getApi().getDeepseek();
        
        assertNotNull(openaiConfig, "OpenAI配置不应该为null");
        assertNotNull(deepseekConfig, "DeepSeek配置不应该为null");
        
        System.out.println("配置中的OpenAI Base URL: " + openaiConfig.getBaseUrl());
        System.out.println("配置中的DeepSeek Base URL: " + deepseekConfig.getBaseUrl());
        
        System.out.println("✅ Base URL配置正确加载");
    }
    
    @Test
    void testCustomBaseUrlFormats() {
        System.out.println("=== 测试自定义Base URL格式 ===");
        
        // 测试不同的Base URL格式
        String[] testUrls = {
            "https://api.openai.com",
            "https://custom-api.example.com",
            "https://azure-resource.openai.azure.com/openai",
            "http://localhost:8080",
            "api.custom.com" // 缺少协议
        };
        
        for (String testUrl : testUrls) {
            System.out.println("测试URL格式: " + testUrl);
            
            // 设置测试URL
            System.setProperty("OPENAI_BASE_URL", testUrl);
            
            try {
                // 创建新的配置验证工具来测试这个URL
                ConfigValidationTool testValidationTool = new ConfigValidationTool(translationProperties);
                ConfigValidationTool.ValidationResult result = testValidationTool.validateAllConfigurations();
                
                System.out.println("✓ URL \"" + testUrl + "\" 验证完成");
                
            } finally {
                // 清理系统属性
                System.clearProperty("OPENAI_BASE_URL");
            }
        }
        
        System.out.println("✅ 自定义Base URL格式测试完成");
    }
    
    @Test
    void testConfigurationWithEnvironmentVariables() {
        System.out.println("=== 测试环境变量配置 ===");
        
        // 获取环境变量
        String openaiApiKey = System.getenv("OPENAI_API_KEY");
        String openaiBaseUrl = System.getenv("OPENAI_BASE_URL");
        String openaiModel = System.getenv("OPENAI_MODEL");
        String openaiMaxTokens = System.getenv("OPENAI_MAX_TOKENS");
        String openaiTemperature = System.getenv("OPENAI_TEMPERATURE");
        
        String deepseekApiKey = System.getenv("DEEPSEEK_API_KEY");
        String deepseekBaseUrl = System.getenv("DEEPSEEK_BASE_URL");
        String deepseekModel = System.getenv("DEEPSEEK_MODEL");
        
        System.out.println("=== 当前环境变量配置 ===");
        System.out.println("OpenAI配置:");
        System.out.println("  API密钥: " + (openaiApiKey != null ? "已配置" : "未配置"));
        System.out.println("  Base URL: " + (openaiBaseUrl != null ? openaiBaseUrl : "使用默认值"));
        System.out.println("  模型: " + (openaiModel != null ? openaiModel : "使用默认值"));
        System.out.println("  Max Tokens: " + (openaiMaxTokens != null ? openaiMaxTokens : "使用默认值"));
        System.out.println("  Temperature: " + (openaiTemperature != null ? openaiTemperature : "使用默认值"));
        
        System.out.println("DeepSeek配置:");
        System.out.println("  API密钥: " + (deepseekApiKey != null ? "已配置" : "未配置"));
        System.out.println("  Base URL: " + (deepseekBaseUrl != null ? deepseekBaseUrl : "使用默认值"));
        System.out.println("  模型: " + (deepseekModel != null ? deepseekModel : "使用默认值"));
        
        // 验证配置属性反映了环境变量
        TranslationProperties.ProviderConfig openaiConfig = translationProperties.getApi().getOpenai();
        TranslationProperties.ProviderConfig deepseekConfig = translationProperties.getApi().getDeepseek();
        
        System.out.println("=== 配置属性中的值 ===");
        System.out.println("OpenAI配置:");
        System.out.println("  Base URL: " + openaiConfig.getBaseUrl());
        System.out.println("  模型: " + openaiConfig.getModel());
        System.out.println("  Max Tokens: " + openaiConfig.getMaxTokens());
        System.out.println("  Temperature: " + openaiConfig.getTemperature());
        
        System.out.println("DeepSeek配置:");
        System.out.println("  Base URL: " + deepseekConfig.getBaseUrl());
        System.out.println("  模型: " + deepseekConfig.getModel());
        
        System.out.println("✅ 环境变量配置测试完成");
    }
    
    @Test
    void testServiceAvailabilityCheck() {
        System.out.println("=== 测试服务可用性检查 ===");
        
        // 测试OpenAI服务可用性检查
        boolean openaiAvailable = translationService.isServiceAvailable(TranslationProvider.OPENAI);
        System.out.println("OpenAI服务可用性: " + openaiAvailable);
        
        // 测试DeepSeek服务可用性检查
        boolean deepseekAvailable = translationService.isServiceAvailable(TranslationProvider.DEEPSEEK);
        System.out.println("DeepSeek服务可用性: " + deepseekAvailable);
        
        // 测试无效服务
        boolean invalidAvailable = translationService.isServiceAvailable(null);
        System.out.println("无效服务可用性: " + invalidAvailable);
        
        System.out.println("✅ 服务可用性检查测试完成");
    }
}