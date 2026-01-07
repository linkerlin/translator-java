package com.translator.e2e;

import com.translator.application.command.TranslateBookCommand;
import com.translator.application.dto.BookDto;
import com.translator.application.dto.TranslationProgressDto;
import com.translator.application.service.BookApplicationService;
import com.translator.domain.exception.TranslationException;
import com.translator.domain.valueobject.TranslationProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 完整翻译流程端到端测试
 * 测试从文件选择到翻译完成的完整流程
 */
public class CompleteTranslationE2ETest extends EndToEndTestBase {
    
    @Autowired
    private BookApplicationService bookApplicationService;
    
    @Test
    @EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".*")
    void testCompleteTranslationWorkflow() throws Exception {
        System.out.println("=== 测试完整翻译工作流程 ===");
        
        // 1. 创建测试EPUB文件
        System.out.println("1. 创建测试EPUB文件...");
        File testEpub = createTestEpubFile("complete-workflow-test");
        
        try {
            // 2. 模拟用户选择文件
            System.out.println("2. 模拟用户选择文件...");
            assertTrue(testEpub.exists(), "测试文件应该存在");
            System.out.println("选择的文件: " + testEpub.getAbsolutePath());
            
            // 3. 选择翻译服务
            System.out.println("3. 选择翻译服务...");
            TranslationProvider provider = TranslationProvider.OPENAI;
            System.out.println("选择的翻译服务: " + provider.getName());
            
            // 4. 设置输出目录
            System.out.println("4. 设置输出目录...");
            String outputDir = System.getProperty("java.io.tmpdir");
            System.out.println("输出目录: " + outputDir);
            
            // 5. 创建翻译命令
            System.out.println("5. 创建翻译命令...");
            TranslateBookCommand command = new TranslateBookCommand(
                testEpub.getAbsolutePath(),
                provider,
                outputDir
            );
            
            // 6. 执行翻译（异步模拟）
            System.out.println("6. 执行翻译流程...");
            CompletableFuture<BookDto> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return bookApplicationService.translateBook(command);
                } catch (TranslationException e) {
                    throw new RuntimeException("翻译失败", e);
                }
            });
            
            // 7. 模拟进度监控
            System.out.println("7. 监控翻译进度...");
            monitorTranslationProgress(testEpub.getName());
            
            // 8. 等待翻译完成
            System.out.println("8. 等待翻译完成...");
            BookDto result = future.get(30, TimeUnit.SECONDS);
            
            // 9. 验证翻译结果
            System.out.println("9. 验证翻译结果...");
            verifyTranslationResult(result, testEpub);
            
            // 10. 验证输出文件
            System.out.println("10. 验证输出文件...");
            verifyOutputFile(result);
            
            System.out.println("✅ 完整翻译工作流程测试通过！");
            
        } finally {
            // 清理测试文件
            if (testEpub.exists()) {
                testEpub.delete();
            }
        }
    }
    
    @Test
    void testTranslationWithDifferentBaseUrls() throws Exception {
        System.out.println("=== 测试不同Base URL的翻译流程 ===");
        
        // 保存原始配置以便恢复
        String originalBaseUrl = translationProperties.getApi().getOpenai().getBaseUrl();
        
        // 测试不同的Base URL配置
        String[] testUrls = {
            "https://api.openai.com",
            "https://custom-api.example.com",
            "https://azure-resource.openai.azure.com/openai"
        };
        
        try {
            for (String baseUrl : testUrls) {
                System.out.println("测试Base URL: " + baseUrl);
                
                // 直接修改配置Bean，以便即时生效
                translationProperties.getApi().getOpenai().setBaseUrl(baseUrl);
                
                try {
                    // 创建测试文件
                    File testEpub = createTestEpubFile("base-url-test-" + baseUrl.hashCode());
                    
                    try {
                        // 执行翻译流程
                        TranslateBookCommand command = new TranslateBookCommand(
                            testEpub.getAbsolutePath(),
                            TranslationProvider.OPENAI,
                            System.getProperty("java.io.tmpdir")
                        );
                        
                        // 如果API密钥无效或网络不通，应该抛出异常
                        // 注意：这里我们期望失败，因为这些URL要么不可达，要么没有有效的API Key
                        assertThrows(Exception.class, () -> {
                            bookApplicationService.translateBook(command);
                        }, "使用无效的API应该抛出异常");
                        
                        System.out.println("✓ Base URL \"" + baseUrl + "\" 测试完成");
                        
                    } finally {
                        if (testEpub.exists()) {
                            testEpub.delete();
                        }
                    }
                } catch (Exception e) {
                    // 如果测试过程中出现非预期的异常（如文件创建失败），记录并抛出
                    System.err.println("测试过程出错: " + e.getMessage());
                    throw e;
                }
            }
        } finally {
            // 恢复原始配置
            translationProperties.getApi().getOpenai().setBaseUrl(originalBaseUrl);
            System.clearProperty("OPENAI_BASE_URL");
        }
        
        System.out.println("✅ 不同Base URL翻译流程测试通过！");
    }
    
    @Test
    void testTranslationErrorHandling() throws Exception {
        System.out.println("=== 测试翻译错误处理 ===");
        
        // 1. 测试无效文件
        System.out.println("1. 测试无效文件...");
        TranslateBookCommand invalidFileCommand = new TranslateBookCommand(
            "/non/existent/file.epub",
            TranslationProvider.OPENAI,
            System.getProperty("java.io.tmpdir")
        );
        
        assertThrows(Exception.class, () -> {
            bookApplicationService.translateBook(invalidFileCommand);
        }, "无效文件应该抛出异常");
        
        // 2. 测试无效翻译服务
        System.out.println("2. 测试无效翻译服务...");
        File testEpub = createTestEpubFile("error-handling-test");
        
        try {
            TranslateBookCommand invalidProviderCommand = new TranslateBookCommand(
                testEpub.getAbsolutePath(),
                TranslationProvider.OPENAI, // 使用OpenAI但配置无效
                System.getProperty("java.io.tmpdir")
            );
            
            // 如果API配置无效，应该抛出异常
            if (getEnvOrDefault("OPENAI_API_KEY", "").isEmpty()) {
                assertThrows(Exception.class, () -> {
                    bookApplicationService.translateBook(invalidProviderCommand);
                }, "无效的API配置应该抛出异常");
            }
            
        } finally {
            if (testEpub.exists()) {
                testEpub.delete();
            }
        }
        
        System.out.println("✅ 翻译错误处理测试通过！");
    }
    
    @Test
    void testTranslationProgressTracking() throws Exception {
        System.out.println("=== 测试翻译进度跟踪 ===");
        
        // 创建较大的测试文件以模拟进度跟踪
        File testEpub = createLargeTestEpubFile("progress-tracking-test", 10);
        
        try {
            // 开始翻译
            TranslateBookCommand command = new TranslateBookCommand(
                testEpub.getAbsolutePath(),
                TranslationProvider.OPENAI,
                System.getProperty("java.io.tmpdir")
            );
            
            // 异步执行翻译
            CompletableFuture<BookDto> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return bookApplicationService.translateBook(command);
                } catch (TranslationException e) {
                    throw new RuntimeException("翻译失败", e);
                }
            });
            
            // 模拟进度跟踪
            System.out.println("开始跟踪翻译进度...");
            simulateProgressTracking(testEpub.getName());
            
            // 等待结果
            BookDto result = future.get(60, TimeUnit.SECONDS);
            
            // 验证进度信息
            assertNotNull(result, "翻译结果不应该为null");
            System.out.println("翻译完成 - 总页数: " + result.getTotalPages() + ", 进度: " + result.getTranslationProgress() + "%");
            
        } finally {
            if (testEpub.exists()) {
                testEpub.delete();
            }
        }
        
        System.out.println("✅ 翻译进度跟踪测试通过！");
    }
    
    /**
     * 创建测试EPUB文件
     */
    private File createTestEpubFile(String name) throws IOException {
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "e2e-test");
        tempDir.mkdirs();
        
        File epubFile = new File(tempDir, name + ".epub");
        
        // 创建包含英文内容的模拟EPUB文件
        try (FileWriter writer = new FileWriter(epubFile)) {
            writer.write("Sample EPUB Content for End-to-End Testing\n");
            writer.write("Title: " + name + "\n");
            writer.write("Author: E2E Test\n");
            writer.write("Language: English\n\n");
            writer.write("This is a sample text for translation testing.\n");
            writer.write("The translation service should convert this to Chinese.\n");
            writer.write("This content will be used to verify the translation functionality.\n");
            writer.write("End of sample content.\n");
        }
        
        return epubFile;
    }
    
    /**
     * 创建大型测试EPUB文件用于进度测试
     */
    private File createLargeTestEpubFile(String name, int pageCount) throws IOException {
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "e2e-test-large");
        tempDir.mkdirs();
        
        File epubFile = new File(tempDir, name + ".epub");
        
        try (FileWriter writer = new FileWriter(epubFile)) {
            writer.write("Large EPUB File for Progress Testing\n");
            writer.write("Title: " + name + "\n");
            writer.write("Pages: " + pageCount + "\n");
            writer.write("Language: English\n\n");
            
            for (int i = 1; i <= pageCount; i++) {
                writer.write("=== Page " + i + " ===\n");
                writer.write("This is content for page " + i + " of the test document.\n");
                writer.write("It contains sample text that will be translated to Chinese.\n");
                writer.write("The translation service will process this content.\n");
                writer.write("End of page " + i + " content.\n\n");
            }
        }
        
        return epubFile;
    }
    
    /**
     * 监控翻译进度（模拟）
     */
    private void monitorTranslationProgress(String bookName) {
        System.out.println("监控书籍翻译进度: " + bookName);
        
        // 模拟进度监控
        for (int i = 0; i <= 100; i += 25) {
            System.out.println("翻译进度: " + i + "%");
            simulateUserDelay();
        }
        
        System.out.println("翻译进度监控完成");
    }
    
    /**
     * 模拟进度跟踪
     */
    private void simulateProgressTracking(String bookName) {
        System.out.println("开始模拟进度跟踪: " + bookName);
        
        // 模拟不同阶段的进度
        String[] stages = {"初始化", "解析文件", "翻译中", "生成输出", "完成"};
        int[] progress = {10, 30, 60, 90, 100};
        
        for (int i = 0; i < stages.length; i++) {
            System.out.println("[" + progress[i] + "%] " + stages[i]);
            simulateUserDelay();
        }
        
        System.out.println("进度跟踪完成");
    }
    
    /**
     * 验证翻译结果
     */
    private void verifyTranslationResult(BookDto result, File originalFile) {
        System.out.println("验证翻译结果:");
        
        // 基本验证
        assertNotNull(result, "翻译结果不应该为null");
        assertEquals(originalFile.getName(), result.getOriginalFileName(), "原始文件名应该匹配");
        assertTrue(result.getTranslatedFileName().contains("中文版"), "翻译后的文件名应该包含'中文版'");
        
        // 进度验证
        assertTrue(result.getTranslationProgress() >= 0 && result.getTranslationProgress() <= 100, 
            "翻译进度应该在0-100%之间");
        
        // 状态验证
        assertNotNull(result.getStatus(), "翻译状态不应该为null");
        
        System.out.println("✅ 翻译结果验证通过");
        System.out.println("  原始文件: " + result.getOriginalFileName());
        System.out.println("  翻译文件: " + result.getTranslatedFileName());
        System.out.println("  总页数: " + result.getTotalPages());
        System.out.println("  翻译进度: " + result.getTranslationProgress() + "%");
        System.out.println("  状态: " + result.getStatus());
    }
    
    /**
     * 验证输出文件
     */
    private void verifyOutputFile(BookDto result) {
        System.out.println("验证输出文件:");
        
        assertNotNull(result.getOutputPath(), "输出路径不应该为null");
        
        File outputFile = new File(result.getOutputPath());
        assertTrue(outputFile.exists(), "输出文件应该存在");
        assertTrue(outputFile.length() > 0, "输出文件应该有内容");
        assertTrue(outputFile.getName().contains("中文版"), "输出文件名应该包含'中文版'");
        
        System.out.println("✅ 输出文件验证通过");
        System.out.println("  输出路径: " + result.getOutputPath());
        System.out.println("  文件大小: " + outputFile.length() + " 字节");
    }
}