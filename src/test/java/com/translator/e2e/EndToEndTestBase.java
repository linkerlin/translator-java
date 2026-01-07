package com.translator.e2e;

import com.translator.EpubTranslatorApplication;
import com.translator.infrastructure.config.TranslationProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.TimeUnit;

/**
 * 端到端测试基类
 * 提供测试环境和通用工具方法
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EpubTranslatorApplication.class, 
                webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = {
    "spring.main.web-application-type=none",
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration"
})
public abstract class EndToEndTestBase {
    
    @Autowired
    protected TranslationProperties translationProperties;
    
    @BeforeEach
    void setUp() {
        // 设置测试环境
        System.setProperty("java.awt.headless", "true");
        
        // 记录测试开始
        System.out.println("=== 开始端到端测试: " + this.getClass().getSimpleName() + " ===");
        
        // 显示当前配置
        showCurrentConfiguration();
    }
    
    @AfterEach
    void tearDown() {
        // 清理测试环境
        System.clearProperty("java.awt.headless");
        
        // 记录测试结束
        System.out.println("=== 完成端到端测试: " + this.getClass().getSimpleName() + " ===");
    }
    
    /**
     * 显示当前配置信息
     */
    protected void showCurrentConfiguration() {
        System.out.println("当前测试配置:");
        System.out.println("OpenAI Base URL: " + getEnvOrDefault("OPENAI_BASE_URL", "https://api.openai.com"));
        System.out.println("OpenAI Model: " + getEnvOrDefault("OPENAI_MODEL", "gpt-3.5-turbo"));
        System.out.println("DeepSeek Base URL: " + getEnvOrDefault("DEEPSEEK_BASE_URL", "https://api.deepseek.com"));
        System.out.println("DeepSeek Model: " + getEnvOrDefault("DEEPSEEK_MODEL", "deepseek-chat"));
        System.out.println("---");
    }
    
    /**
     * 获取环境变量或默认值
     */
    protected String getEnvOrDefault(String envName, String defaultValue) {
        String value = System.getenv(envName);
        return value != null ? value : defaultValue;
    }
    
    /**
     * 等待指定时间（用于异步操作）
     */
    protected void waitForSeconds(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 创建模拟EPUB文件内容
     */
    protected String createMockEpubContent(String title, String author, int pageCount) {
        StringBuilder content = new StringBuilder();
        content.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        content.append("<package xmlns=\"http://www.idpf.org/2007/opf\" version=\"3.0\">\n");
        content.append("  <metadata>\n");
        content.append("    <dc:title>").append(title).append("</dc:title>\n");
        content.append("    <dc:creator>").append(author).append("</dc:creator>\n");
        content.append("    <dc:language>en</dc:language>\n");
        content.append("  </metadata>\n");
        content.append("  <manifest>\n");
        
        for (int i = 1; i <= pageCount; i++) {
            content.append("    <item id=\"page").append(i).append("\" href=\"page").append(i).append(".html\" media-type=\"application/xhtml+xml\"/>\n");
        }
        
        content.append("  </manifest>\n");
        content.append("  <spine>\n");
        
        for (int i = 1; i <= pageCount; i++) {
            content.append("    <itemref idref=\"page").append(i).append("\"/>\n");
        }
        
        content.append("  </spine>\n");
        content.append("</package>");
        
        return content.toString();
    }
    
    /**
     * 创建模拟页面内容
     */
    protected String createMockPageContent(String title, int pageNumber, String sampleText) {
        StringBuilder content = new StringBuilder();
        content.append("<!DOCTYPE html>\n");
        content.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n");
        content.append("<head>\n");
        content.append("  <title>").append(title).append(" - Page ").append(pageNumber).append("</title>\n");
        content.append("</head>\n");
        content.append("<body>\n");
        content.append("  <h1>").append(title).append(" - Chapter ").append(pageNumber).append("</h1>\n");
        content.append("  <p>").append(sampleText).append("</p>\n");
        content.append("  <p>This is sample content for page ").append(pageNumber).append(".</p>\n");
        content.append("</body>\n");
        content.append("</html>");
        
        return content.toString();
    }
    
    /**
     * 验证翻译结果
     */
    protected boolean validateTranslationResult(String originalText, String translatedText) {
        if (translatedText == null || translatedText.isEmpty()) {
            System.out.println("❌ 翻译结果为空");
            return false;
        }
        
        if (translatedText.equals(originalText)) {
            System.out.println("⚠️  翻译结果与原文相同，可能翻译失败");
            return false;
        }
        
        if (translatedText.length() < originalText.length() / 2) {
            System.out.println("⚠️  翻译结果过短，可能翻译不完整");
            return false;
        }
        
        System.out.println("✅ 翻译结果验证通过");
        System.out.println("原文长度: " + originalText.length() + " 字符");
        System.out.println("译文长度: " + translatedText.length() + " 字符");
        return true;
    }
    
    /**
     * 模拟用户操作延迟
     */
    protected void simulateUserDelay() {
        waitForSeconds(1); // 模拟1秒的用户操作时间
    }
}