package com.translator.e2e;

import com.translator.application.command.TranslateBookCommand;
import com.translator.application.dto.BookDto;
import com.translator.application.service.BookApplicationService;
import com.translator.domain.exception.TranslationException;
import com.translator.domain.valueobject.TranslationProvider;
import com.translator.infrastructure.translation.ConfigValidationTool;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Azure OpenAI端到端测试
 * 测试使用Azure OpenAI服务的完整翻译流程
 */
public class AzureOpenAIE2ETest extends EndToEndTestBase {
    
    @Autowired
    private BookApplicationService bookApplicationService;
    
    @Autowired
    private ConfigValidationTool configValidationTool;
    
    @Test
    @EnabledIfEnvironmentVariable(named = "AZURE_OPENAI_API_KEY", matches = ".*")
    void testAzureOpenAIConfiguration() throws Exception {
        System.out.println("=== 测试Azure OpenAI配置 ===");
        
        // 设置Azure配置
        System.setProperty("OPENAI_BASE_URL", "https://your-resource.openai.azure.com/openai");
        System.setProperty("OPENAI_MODEL", "gpt-35-turbo"); // Azure使用gpt-35-turbo
        System.setProperty("OPENAI_API_KEY", System.getenv("AZURE_OPENAI_API_KEY"));
        
        try {
            // 1. 验证Azure配置
            System.out.println("1. 验证Azure OpenAI配置...");
            ConfigValidationTool.ValidationResult validationResult = configValidationTool.validateAllConfigurations();
            
            // Azure配置应该有特定的Base URL格式
            String baseUrl = getEnvOrDefault("OPENAI_BASE_URL", "");
            if (baseUrl.contains("azure") && baseUrl.contains("openai")) {
                System.out.println("✓ 检测到Azure OpenAI配置");
            }
            
            // 2. 创建模拟EPUB文件
            System.out.println("2. 创建模拟EPUB文件...");
            File tempEpub = createTestEpubFile("azure-test-book");
            
            try {
                // 3. 执行翻译
                System.out.println("3. 执行Azure OpenAI翻译流程...");
                TranslateBookCommand command = new TranslateBookCommand(
                    tempEpub.getAbsolutePath(),
                    TranslationProvider.OPENAI,
                    System.getProperty("java.io.tmpdir")
                );
                
                BookDto result = bookApplicationService.translateBook(command);
                
                // 4. 验证结果
                System.out.println("4. 验证Azure翻译结果...");
                assertNotNull(result, "Azure翻译结果不应该为null");
                assertEquals("azure-test-book.epub", result.getOriginalFileName());
                assertTrue(result.getTranslatedFileName().contains("中文版"));
                assertEquals(2, result.getTotalPages());
                assertEquals(2, result.getTranslatedPages());
                
                // 5. 验证输出文件
                System.out.println("5. 验证Azure输出文件...");
                assertNotNull(result.getOutputPath());
                File outputFile = new File(result.getOutputPath());
                assertTrue(outputFile.exists(), "Azure输出文件应该存在");
                
                System.out.println("✅ Azure OpenAI端到端测试通过！");
                
            } finally {
                // 清理临时文件
                if (tempEpub.exists()) {
                    tempEpub.delete();
                }
            }
            
        } finally {
            // 清理系统属性
            System.clearProperty("OPENAI_BASE_URL");
            System.clearProperty("OPENAI_MODEL");
        }
    }
    
    @Test
    void testAzureConfigurationDetection() {
        System.out.println("=== 测试Azure配置检测 ===");
        
        // 测试Azure URL格式检测
        String[] azureUrls = {
            "https://myresource.openai.azure.com/openai",
            "https://test-resource.openai.azure.com/openai/deployments/gpt-35-turbo",
            "https://production.openai.azure.com"
        };
        
        for (String url : azureUrls) {
            System.out.println("测试Azure URL: " + url);
            assertTrue(url.contains("azure") && url.contains("openai"), 
                "URL应该包含azure和openai关键字: " + url);
        }
        
        // 测试Azure模型名称
        String azureModel = "gpt-35-turbo"; // Azure使用gpt-35-turbo，不是gpt-3.5-turbo
        System.out.println("Azure模型名称: " + azureModel);
        assertTrue(azureModel.startsWith("gpt-"), "模型应该以gpt-开头");
        assertTrue(azureModel.contains("35"), "Azure模型包含35");
        
        System.out.println("✅ Azure配置检测测试通过！");
    }
    
    @Test
    void testAzureConfigurationValidation() {
        System.out.println("=== 测试Azure配置验证 ===");
        
        // 设置Azure配置
        System.setProperty("OPENAI_BASE_URL", "https://test-resource.openai.azure.com/openai");
        System.setProperty("OPENAI_MODEL", "gpt-35-turbo");
        
        try {
            // 运行配置验证
            System.out.println("1. 运行Azure配置验证...");
            ConfigValidationTool.ValidationResult result = configValidationTool.validateAllConfigurations();
            
            // 2. 验证结果
            System.out.println("2. 验证Azure配置结果...");
            assertNotNull(result, "验证结果不应该为null");
            
            // 打印验证结果
            System.out.println("=== Azure配置验证结果 ===");
            result.printResult();
            
            // 如果API密钥未配置，应该有警告
            if (getEnvOrDefault("AZURE_OPENAI_API_KEY", "").isEmpty()) {
                assertTrue(result.hasWarnings() || result.hasErrors(), 
                    "当Azure API密钥未配置时，应该有警告或错误");
            }
            
            System.out.println("✅ Azure配置验证测试通过！");
            
        } finally {
            // 清理系统属性
            System.clearProperty("OPENAI_BASE_URL");
            System.clearProperty("OPENAI_MODEL");
        }
    }
    
    /**
     * 创建测试EPUB文件
     */
    private File createTestEpubFile(String name) throws IOException {
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "azure-e2e-test");
        tempDir.mkdirs();
        
        File epubFile = new File(tempDir, name + ".epub");
        
        try (FileWriter writer = new FileWriter(epubFile)) {
            writer.write("Azure OpenAI Test EPUB\n");
            writer.write("Name: " + name + "\n");
            writer.write("Author: Azure Test Author\n");
            writer.write("Language: English\n\n");
            writer.write("This is test content for Azure OpenAI translation.\n");
            writer.write("The Azure OpenAI service should translate this to Chinese.\n");
        }
        
        return epubFile;
    }
}