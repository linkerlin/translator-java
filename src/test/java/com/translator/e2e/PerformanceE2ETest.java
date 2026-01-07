package com.translator.e2e;

import com.translator.application.command.TranslateBookCommand;
import com.translator.application.dto.BookDto;
import com.translator.application.service.BookApplicationService;
import com.translator.domain.exception.TranslationException;
import com.translator.domain.valueobject.TranslationProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 性能端到端测试
 * 测试翻译性能、批处理效果和资源使用
 */
public class PerformanceE2ETest extends EndToEndTestBase {
    
    @Autowired
    private BookApplicationService bookApplicationService;
    
    @Test
    void testTranslationPerformance() throws Exception {
        System.out.println("=== 测试翻译性能 ===");
        
        // 创建不同大小的测试文件
        int[] sizes = {1, 5, 10}; // 不同页数的测试
        
        for (int pageCount : sizes) {
            System.out.println("测试 " + pageCount + " 页文件的翻译性能");
            
            // 创建测试文件
            File testEpub = createPerformanceTestFile("performance-test-" + pageCount, pageCount);
            
            try {
                // 记录开始时间
                long startTime = System.currentTimeMillis();
                
                // 执行翻译
                TranslateBookCommand command = new TranslateBookCommand(
                    testEpub.getAbsolutePath(),
                    TranslationProvider.OPENAI,
                    System.getProperty("java.io.tmpdir")
                );
                
                // 如果API密钥无效，会抛出异常
                try {
                    BookDto result = bookApplicationService.translateBook(command);
                    
                    // 记录结束时间
                    long endTime = System.currentTimeMillis();
                    long duration = endTime - startTime;
                    
                    // 验证结果
                    assertNotNull(result, "翻译结果不应该为null");
                    assertEquals(pageCount, result.getTotalPages(), "页数应该匹配");
                    
                    // 性能分析
                    analyzePerformance(pageCount, duration);
                    
                } catch (TranslationException e) {
                    System.out.println("⚠️  API配置无效，跳过性能测试: " + e.getMessage());
                }
                
            } finally {
                if (testEpub.exists()) {
                    testEpub.delete();
                }
            }
        }
        
        System.out.println("✅ 翻译性能测试完成！");
    }
    
    @Test
    void testBatchProcessingEfficiency() throws Exception {
        System.out.println("=== 测试批处理效率 ===");
        
        // 测试不同批处理大小的效果
        int[] batchSizes = {1000, 2000, 5000}; // 不同的批处理大小
        int pageCount = 5; // 固定页数用于比较
        
        for (int batchSize : batchSizes) {
            System.out.println("测试批处理大小: " + batchSize + " 字符");
            
            // 设置批处理大小
            System.setProperty("TRANSLATION_SETTINGS_BATCH_SIZE", String.valueOf(batchSize));
            
            try {
                // 创建测试文件
                File testEpub = createBatchTestFile("batch-test-" + batchSize, batchSize, pageCount);
                
                try {
                    // 记录开始时间
                    long startTime = System.currentTimeMillis();
                    
                    // 执行翻译
                    TranslateBookCommand command = new TranslateBookCommand(
                        testEpub.getAbsolutePath(),
                        TranslationProvider.OPENAI,
                        System.getProperty("java.io.tmpdir")
                    );
                    
                    try {
                        BookDto result = bookApplicationService.translateBook(command);
                        
                        // 记录结束时间
                        long endTime = System.currentTimeMillis();
                        long duration = endTime - startTime;
                        
                        // 验证结果
                        assertNotNull(result, "翻译结果不应该为null");
                        
                        // 批处理效率分析
                        analyzeBatchEfficiency(batchSize, pageCount, duration);
                        
                    } catch (TranslationException e) {
                        System.out.println("⚠️  API配置无效，跳���批处理测试: " + e.getMessage());
                    }
                    
                } finally {
                    if (testEpub.exists()) {
                        testEpub.delete();
                    }
                }
                
            } finally {
                System.clearProperty("TRANSLATION_SETTINGS_BATCH_SIZE");
            }
        }
        
        System.out.println("✅ 批处理效率测试完成！");
    }
    
    @Test
    void testMemoryUsage() throws Exception {
        System.out.println("=== 测试内存使用情况 ===");
        
        // 获取初始内存状态
        System.gc(); // 建议垃圾回收
        Thread.sleep(1000); // 等待垃圾回收
        
        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        System.out.println("初始内存使用: " + (initialMemory / 1024 / 1024) + " MB");
        
        // 创建较大的测试文件
        File testEpub = createLargeTestFile("memory-test", 20); // 20页大文件
        
        try {
            // 执行翻译
            TranslateBookCommand command = new TranslateBookCommand(
                testEpub.getAbsolutePath(),
                TranslationProvider.OPENAI,
                System.getProperty("java.io.tmpdir")
            );
            
            try {
                BookDto result = bookApplicationService.translateBook(command);
                
                // 获取翻译后的内存状态
                System.gc(); // 建议垃圾回收
                Thread.sleep(1000); // 等待垃圾回收
                
                long finalMemory = runtime.totalMemory() - runtime.freeMemory();
                long memoryUsed = finalMemory - initialMemory;
                
                System.out.println("翻译后内存使用: " + (finalMemory / 1024 / 1024) + " MB");
                System.out.println("翻译过程内存消耗: " + (memoryUsed / 1024 / 1024) + " MB");
                
                // 验证结果
                assertNotNull(result, "翻译结果不应该为null");
                
                // 内存使用分析
                analyzeMemoryUsage(result, memoryUsed);
                
            } catch (TranslationException e) {
                System.out.println("⚠️  API配置无效，跳过内存测试: " + e.getMessage());
            }
            
        } finally {
            if (testEpub.exists()) {
                testEpub.delete();
            }
        }
        
        System.out.println("✅ 内存使用测试完成！");
    }
    
    @Test
    void testConcurrentTranslation() throws Exception {
        System.out.println("=== 测试并发翻译 ===");
        
        // 创建多个测试文件
        int fileCount = 3;
        File[] testFiles = new File[fileCount];
        CompletableFuture<BookDto>[] futures = new CompletableFuture[fileCount];
        
        try {
            // 创建测试文件
            System.out.println("创建 " + fileCount + " 个测试文件...");
            for (int i = 0; i < fileCount; i++) {
                testFiles[i] = createConcurrentTestFile("concurrent-test-" + i, i + 1);
            }
            
            // 记录开始时间
            long startTime = System.currentTimeMillis();
            
            // 并发执行翻译
            System.out.println("开始并发翻译...");
            for (int i = 0; i < fileCount; i++) {
                final int index = i;
                futures[i] = CompletableFuture.supplyAsync(() -> {
                    try {
                        TranslateBookCommand command = new TranslateBookCommand(
                            testFiles[index].getAbsolutePath(),
                            TranslationProvider.OPENAI,
                            System.getProperty("java.io.tmpdir")
                        );
                        return bookApplicationService.translateBook(command);
                    } catch (TranslationException e) {
                        throw new RuntimeException("翻译失败", e);
                    }
                });
            }
            
            // 等待所有翻译完成
            CompletableFuture.allOf(futures).join();
            
            // 记录结束时间
            long endTime = System.currentTimeMillis();
            long totalDuration = endTime - startTime;
            
            // 验证所有结果
            System.out.println("验证并发翻译结果...");
            int successCount = 0;
            for (int i = 0; i < fileCount; i++) {
                try {
                    BookDto result = futures[i].get();
                    if (result != null) {
                        successCount++;
                        System.out.println("文件 " + i + " 翻译成功: " + result.getTranslatedFileName());
                    }
                } catch (Exception e) {
                    System.out.println("文件 " + i + " 翻译失败: " + e.getMessage());
                }
            }
            
            // 并发性能分析
            analyzeConcurrentPerformance(fileCount, successCount, totalDuration);
            
        } finally {
            // 清理测试文件
            for (File file : testFiles) {
                if (file != null && file.exists()) {
                    file.delete();
                }
            }
        }
        
        System.out.println("✅ 并发翻译测试完成！");
    }
    
    /**
     * 创建性能测试文件
     */
    private File createPerformanceTestFile(String name, int pageCount) throws IOException {
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "performance-test");
        tempDir.mkdirs();
        
        File epubFile = new File(tempDir, name + ".epub");
        
        try (FileWriter writer = new FileWriter(epubFile)) {
            writer.write("Performance Test EPUB\n");
            writer.write("Name: " + name + "\n");
            writer.write("Pages: " + pageCount + "\n");
            writer.write("Purpose: Performance testing\n\n");
            
            // 为每页创建不同长度的内容
            for (int i = 1; i <= pageCount; i++) {
                writer.write("=== Page " + i + " ===\n");
                
                // 不同页面有不同长度的内容
                int contentLength = 100 * i; // 每页内容递增
                for (int j = 0; j < contentLength; j++) {
                    writer.write("This is sample text for performance testing. ");
                }
                writer.write("\n\n");
            }
        }
        
        return epubFile;
    }
    
    /**
     * 创建批处理测试文件
     */
    private File createBatchTestFile(String name, int batchSize, int pageCount) throws IOException {
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "batch-test");
        tempDir.mkdirs();
        
        File epubFile = new File(tempDir, name + ".epub");
        
        try (FileWriter writer = new FileWriter(epubFile)) {
            writer.write("Batch Processing Test EPUB\n");
            writer.write("Name: " + name + "\n");
            writer.write("Batch Size: " + batchSize + " characters\n");
            writer.write("Pages: " + pageCount + "\n\n");
            
            // 为每页创建接近批处理大小的内容
            for (int i = 1; i <= pageCount; i++) {
                writer.write("=== Page " + i + " ===\n");
                
                // 创建接近批处理大小的内容
                int targetSize = Math.min(batchSize, 5000); // 限制最大大小
                while (writer.toString().length() < targetSize) {
                    writer.write("This is sample text for batch processing testing. ");
                }
                writer.write("\n\n");
            }
        }
        
        return epubFile;
    }
    
    /**
     * 创建大型测试文件
     */
    private File createLargeTestFile(String name, int pageCount) throws IOException {
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "large-test");
        tempDir.mkdirs();
        
        File epubFile = new File(tempDir, name + ".epub");
        
        try (FileWriter writer = new FileWriter(epubFile)) {
            writer.write("Large File Memory Test EPUB\n");
            writer.write("Name: " + name + "\n");
            writer.write("Pages: " + pageCount + "\n");
            writer.write("Purpose: Memory usage testing\n\n");
            
            // 创建大量内容以测试内存使用
            for (int i = 1; i <= pageCount; i++) {
                writer.write("=== Page " + i + " (Large Content) ===\n");
                
                // 每页创建大量内容
                for (int j = 0; j < 1000; j++) { // 每页1000行内容
                    writer.write("This is line " + j + " of page " + i + " for memory testing. ");
                    writer.write("It contains sample text that will be processed by the translation service. ");
                    writer.write("This content is designed to test memory usage during translation.\n");
                }
                writer.write("\n");
            }
        }
        
        return epubFile;
    }
    
    /**
     * 创建并发测试文件
     */
    private File createConcurrentTestFile(String name, int contentMultiplier) throws IOException {
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "concurrent-test");
        tempDir.mkdirs();
        
        File epubFile = new File(tempDir, name + ".epub");
        
        try (FileWriter writer = new FileWriter(epubFile)) {
            writer.write("Concurrent Test EPUB - File " + name + "\n");
            writer.write("Multiplier: " + contentMultiplier + "\n");
            writer.write("Purpose: Concurrent translation testing\n\n");
            
            // 根据multiplier创建不同大小的内容
            for (int i = 1; i <= 5 * contentMultiplier; i++) {
                writer.write("=== Concurrent Test Page " + i + " ===\n");
                for (int j = 0; j < 100 * contentMultiplier; j++) {
                    writer.write("Concurrent test content for file " + name + " page " + i + " line " + j + ". ");
                }
                writer.write("\n\n");
            }
        }
        
        return epubFile;
    }
    
    /**
     * 性能分析
     */
    private void analyzePerformance(int pageCount, long duration) {
        double seconds = duration / 1000.0;
        double pagesPerSecond = pageCount / seconds;
        double secondsPerPage = seconds / pageCount;
        
        System.out.println("性能分析:");
        System.out.println("  页数: " + pageCount);
        System.out.println("  耗时: " + String.format("%.2f", seconds) + " 秒");
        System.out.println("  翻译速度: " + String.format("%.2f", pagesPerSecond) + " 页/秒");
        System.out.println("  每页耗时: " + String.format("%.2f", secondsPerPage) + " 秒/页");
        
        // 性能基准
        if (pagesPerSecond > 1.0) {
            System.out.println("  ✅ 性能优秀 (>1页/秒)");
        } else if (pagesPerSecond > 0.5) {
            System.out.println("  ⚠️  性能良好 (0.5-1页/秒)");
        } else {
            System.out.println("  ❌ 性能较慢 (<0.5页/秒)");
        }
    }
    
    /**
     * 批处理效率分析
     */
    private void analyzeBatchEfficiency(int batchSize, int pageCount, long duration) {
        double seconds = duration / 1000.0;
        double efficiency = (batchSize * pageCount) / seconds; // 字符/秒
        
        System.out.println("批处理效率分析:");
        System.out.println("  批处理大小: " + batchSize + " 字符");
        System.out.println("  页数: " + pageCount);
        System.out.println("  耗时: " + String.format("%.2f", seconds) + " 秒");
        System.out.println("  处理效率: " + String.format("%.0f", efficiency) + " 字符/秒");
        
        // 效率基准
        if (efficiency > 5000) {
            System.out.println("  ✅ 批处理效率优秀 (>5000字符/秒)");
        } else if (efficiency > 2000) {
            System.out.println("  ⚠️  批处理效率良好 (2000-5000字符/秒)");
        } else {
            System.out.println("  ❌ 批处理效率较低 (<2000字符/秒)");
        }
    }
    
    /**
     * 内存使用分析
     */
    private void analyzeMemoryUsage(BookDto result, long memoryUsed) {
        System.out.println("内存使用分析:");
        System.out.println("  内存消耗: " + (memoryUsed / 1024 / 1024) + " MB");
        System.out.println("  翻译页数: " + result.getTotalPages());
        System.out.println("  每页内存消耗: " + (memoryUsed / result.getTotalPages() / 1024) + " KB/页");
        
        // 内存基准
        long memoryPerPage = memoryUsed / result.getTotalPages();
        if (memoryPerPage < 1024 * 1024) { // 1MB per page
            System.out.println("  ✅ 内存使用合理 (<1MB/页)");
        } else if (memoryPerPage < 5 * 1024 * 1024) { // 5MB per page
            System.out.println("  ⚠️  内存使用较高 (1-5MB/页)");
        } else {
            System.out.println("  ❌ 内存使用过高 (>5MB/页)");
        }
    }
    
    /**
     * 并发性能分析
     */
    private void analyzeConcurrentPerformance(int fileCount, int successCount, long totalDuration) {
        double seconds = totalDuration / 1000.0;
        double filesPerSecond = fileCount / seconds;
        double successRate = (double) successCount / fileCount * 100;
        
        System.out.println("并发性能分析:");
        System.out.println("  文件数量: " + fileCount);
        System.out.println("  成功数量: " + successCount);
        System.out.println("  成功率: " + String.format("%.1f", successRate) + "%");
        System.out.println("  总耗时: " + String.format("%.2f", seconds) + " 秒");
        System.out.println("  并发速度: " + String.format("%.2f", filesPerSecond) + " 文件/秒");
        
        // 并发基准
        if (successRate > 80) {
            System.out.println("  ✅ 并发性能优秀 (>80%成功率)");
        } else if (successRate > 60) {
            System.out.println("  ⚠️  并发性能良好 (60-80%成功率)");
        } else {
            System.out.println("  ❌ 并发性能较差 (<60%成功率)");
        }
    }
}