package com.translator;

import com.translator.presentation.SwingApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 项目运行器
 * 用于测试和演示项目功能
 */
public class Runner {
    
    private static final Logger logger = LoggerFactory.getLogger(Runner.class);
    
    public static void main(String[] args) {
        logger.info("=== EPUB翻译器项目启动 ===");
        
        try {
            // 检查示例文件是否存在
            java.io.File exampleFile = new java.io.File("brave-new-world.epub");
            if (!exampleFile.exists()) {
                logger.warn("示例文件 brave-new-world.epub 不存在，请确保文件在正确位置");
            } else {
                logger.info("找到示例文件: {} (大小: {} KB)", 
                    exampleFile.getName(), 
                    exampleFile.length() / 1024);
            }
            
            // 启动Swing应用
            logger.info("正在启动Swing应用程序...");
            SwingApplication.main(args);
            
        } catch (Exception e) {
            logger.error("项目启动失败", e);
            System.exit(1);
        }
    }
    
    /**
     * 快速测试方法 - 用于验证核心功能
     */
    public static void quickTest() {
        logger.info("开始快速测试...");
        
        try {
            // 测试书籍领域模型
            testBookDomainModel();
            
            // 测试翻译请求
            testTranslationRequest();
            
            logger.info("快速测试完成！");
            
        } catch (Exception e) {
            logger.error("快速测试失败", e);
        }
    }
    
    private static void testBookDomainModel() {
        logger.info("测试书籍领域模型...");
        
        // 创建书籍
        com.translator.domain.model.Book book = new com.translator.domain.model.Book("test.epub");
        
        // 添加页面
        com.translator.domain.model.Page page1 = new com.translator.domain.model.Page("page1", 1, "Chapter 1", "This is a test content.");
        com.translator.domain.model.Page page2 = new com.translator.domain.model.Page("page2", 2, "Chapter 2", "Another test content.");
        
        book.addPage(page1);
        book.addPage(page2);
        
        // 验证状态
        assert book.getTotalPages() == 2 : "页面数量不正确";
        assert book.getTranslatedPages() == 0 : "翻译页面数量不正确";
        assert book.getTranslationProgress() == 0.0 : "翻译进度不正确";
        
        // 模拟翻译
        page1.translate("这是测试内容。");
        
        assert book.getTranslatedPages() == 1 : "翻译后页面数量不正确";
        assert book.getTranslationProgress() == 50.0 : "翻译进度计算不正确";
        
        logger.info("书籍领域模型测试通过！");
    }
    
    private static void testTranslationRequest() {
        logger.info("测试翻译请求值对象...");
        
        com.translator.domain.valueobject.TranslationRequest request = 
            new com.translator.domain.valueobject.TranslationRequest(
                "Hello World",
                "en",
                "zh",
                com.translator.domain.valueobject.TranslationProvider.OPENAI
            );
        
        assert "Hello World".equals(request.getText()) : "文本内容不正确";
        assert "en".equals(request.getSourceLanguage()) : "源语言不正确";
        assert "zh".equals(request.getTargetLanguage()) : "目标语言不正确";
        assert com.translator.domain.valueobject.TranslationProvider.OPENAI == request.getProvider() : "提供商不正确";
        
        logger.info("翻译请求测试通过！");
    }
}