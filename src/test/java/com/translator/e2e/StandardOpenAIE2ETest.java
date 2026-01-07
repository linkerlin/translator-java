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
 * 标准OpenAI端到端测试
 * 测试使用标准OpenAI API的完整翻译流程
 */
public class StandardOpenAIE2ETest extends EndToEndTestBase {
    
    @Autowired
    private BookApplicationService bookApplicationService;
    
    @Autowired
    private ConfigValidationTool configValidationTool;
    
    @Test
    @EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".*")
    void testStandardOpenAIConfiguration() throws Exception {
        System.out.println("=== 测试标准OpenAI配置 ===");
        
        // 1. 验证配置
        System.out.println("1. 验证OpenAI配置...");
        ConfigValidationTool.ValidationResult validationResult = configValidationTool.validateAllConfigurations();
        assertTrue(validationResult.isValid(), "OpenAI配置应该有效");
        
        // 2. 创建模拟EPUB文件
        System.out.println("2. 创建模拟EPUB文件...");
        File tempEpub = createMockEpubFile("test-book", "Test Author", 3);
        
        try {
            // 3. 执行翻译
            System.out.println("3. 执行翻译流程...");
            TranslateBookCommand command = new TranslateBookCommand(
                tempEpub.getAbsolutePath(),
                TranslationProvider.OPENAI,
                System.getProperty("java.io.tmpdir")
            );
            
            BookDto result = bookApplicationService.translateBook(command);
            
            // 4. 验证结果
            System.out.println("4. 验证翻译结果...");
            assertNotNull(result, "翻译结果不应该为null");
            assertEquals("test-book.epub", result.getOriginalFileName());
            assertTrue(result.getTranslatedFileName().contains("中文版"));
            assertEquals(3, result.getTotalPages());
            assertEquals(3, result.getTranslatedPages());
            assertEquals(100.0, result.getTranslationProgress(), 0.1);
            
            // 5. 验证输出文件
            System.out.println("5. 验证输出文件...");
            assertNotNull(result.getOutputPath());
            File outputFile = new File(result.getOutputPath());
            assertTrue(outputFile.exists(), "输出文件应该存在");
            assertTrue(outputFile.length() > 0, "输出文件应该有内容");
            
            System.out.println("✅ 标准OpenAI端到端测试通过！");
            
        } finally {
            // 清理临时文件
            if (tempEpub.exists()) {
                tempEpub.delete();
            }
        }
    }
    
    @Test
    void testOpenAIConfigurationWithCustomBaseUrl() throws Exception {
        System.out.println("=== 测试自定义Base URL配置 ===");
        
        // 设置自定义Base URL
        String customBaseUrl = "https://custom-api.example.com";
        System.setProperty("OPENAI_BASE_URL", customBaseUrl);
        
        try {
            // 验证配置读取
            System.out.println("1. 验证自定义Base URL配置...");
            String actualBaseUrl = getEnvOrDefault("OPENAI_BASE_URL", "https://api.openai.com");
            assertEquals(customBaseUrl, actualBaseUrl);
            
            // 2. 创建模拟EPUB文件
            System.out.println("2. 创建模拟EPUB文件...");
            File tempEpub = createMockEpubFile("custom-test-book", "Custom Test Author", 2);
            
            try {
                // 3. 尝试执行翻译（如果没有有效API密钥，应该抛出异常）
                System.out.println("3. 测试翻译流程...");
                TranslateBookCommand command = new TranslateBookCommand(
                    tempEpub.getAbsolutePath(),
                    TranslationProvider.OPENAI,
                    System.getProperty("java.io.tmpdir")
                );
                
                // 如果API密钥无效，这里应该抛出异常
                assertThrows(TranslationException.class, () -> {
                    bookApplicationService.translateBook(command);
                }, "使用无效的自定义API应该抛出异常");
                
                System.out.println("✅ 自定义Base URL配置测试通过！");
                
            } finally {
                if (tempEpub.exists()) {
                    tempEpub.delete();
                }
            }
            
        } finally {
            // 清理系统属性
            System.clearProperty("OPENAI_BASE_URL");
        }
    }
    
    @Test
    void testConfigurationValidation() {
        System.out.println("=== 测试配置验证功能 ===");
        
        // 1. 验证配置工具
        System.out.println("1. 运行配置验证...");
        ConfigValidationTool.ValidationResult result = configValidationTool.validateAllConfigurations();
        
        // 2. 验证结果格式
        System.out.println("2. 验证结果格式...");
        assertNotNull(result, "验证结果不应该为null");
        
        // 打印验证结果供查看
        System.out.println("=== 配置验证结果 ===");
        result.printResult();
        
        // 3. 验证结果状态
        System.out.println("3. 验证结果状态...");
        if (getEnvOrDefault("OPENAI_API_KEY", "").isEmpty()) {
            assertTrue(result.hasWarnings() || result.hasErrors(), 
                "当API密钥未配置时，应该有警告或错误");
        } else {
            assertTrue(result.isValid() || result.hasWarnings(), 
                "当API密钥配置时，应该通过验证或有警告");
        }
        
        System.out.println("✅ 配置验证功能测试通过！");
    }
    
    /**
     * 创建模拟EPUB文件
     */
    private File createMockEpubFile(String title, String author, int pageCount) throws IOException {
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "epub-translator-test");
        tempDir.mkdirs();
        
        File epubFile = new File(tempDir, title.replace(" ", "-") + ".epub");
        
        // 创建模拟EPUB内容
        try (FileWriter writer = new FileWriter(epubFile)) {
            writer.write("模拟EPUB文件内容\n");
            writer.write("标题: " + title + "\n");
            writer.write("作者: " + author + "\n");
            writer.write("页数: " + pageCount + "\n");
            writer.write("语言: en\n");
            writer.write("这是用于端到端测试的模拟EPUB文件。\n");
        }
        
        return epubFile;
    }
}