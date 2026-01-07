package com.translator;

import com.translator.infrastructure.config.TranslationProperties;
import com.translator.infrastructure.translation.ConfigValidationTool;
import com.translator.infrastructure.translation.OpenAITranslationService;
import com.translator.domain.valueobject.TranslationProvider;
import com.translator.domain.exception.TranslationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 配置测试工具
 * 独立的命令行工具，用于测试OpenAI和DeepSeek的API配置
 */
@SpringBootApplication
@EnableConfigurationProperties(TranslationProperties.class)
public class ConfigTestTool {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigTestTool.class);
    
    public static void main(String[] args) {
        logger.info("=== EPUB翻译器 - 配置测试工具 ===");
        
        // 检查命令行参数
        if (args.length == 0) {
            printUsage();
            return;
        }
        
        String command = args[0].toLowerCase();
        
        switch (command) {
            case "validate":
                runValidation(args);
                break;
            case "test-translation":
                testTranslation(args);
                break;
            case "show-config":
                showConfig(args);
                break;
            default:
                printUsage();
                break;
        }
    }
    
    /**
     * 运行配置验证
     */
    private static void runValidation(String[] args) {
        try {
            logger.info("运行配置验证...");
            
            // 启动Spring上下文
            ConfigurableApplicationContext context = SpringApplication.run(ConfigTestTool.class, args);
            
            // 获取配置属性
            TranslationProperties properties = context.getBean(TranslationProperties.class);
            
            // 执行验证
            ConfigValidationTool validationTool = new ConfigValidationTool(properties);
            ConfigValidationTool.ValidationResult result = validationTool.validateAllConfigurations();
            
            // 打印结果
            result.printResult();
            
            // 退出上下文
            context.close();
            
            // 根据验证结果设置退出码
            System.exit(result.hasErrors() ? 1 : 0);
            
        } catch (Exception e) {
            logger.error("配置验证失败", e);
            System.exit(1);
        }
    }
    
    /**
     * 测试翻译功能
     */
    private static void testTranslation(String[] args) {
        if (args.length < 2) {
            logger.error("请指定要测试的翻译服务: openai 或 deepseek");
            System.exit(1);
        }
        
        String providerName = args[1].toLowerCase();
        TranslationProvider provider;
        
        try {
            if ("openai".equals(providerName)) {
                provider = TranslationProvider.OPENAI;
            } else if ("deepseek".equals(providerName)) {
                provider = TranslationProvider.DEEPSEEK;
            } else {
                logger.error("不支持的翻译服务: {}", providerName);
                System.exit(1);
                return;
            }
            
            logger.info("测试{}翻译服务...", provider.getName());
            
            // 启动Spring上下文
            ConfigurableApplicationContext context = SpringApplication.run(ConfigTestTool.class, args);
            
            // 获取翻译服务
            OpenAITranslationService translationService = context.getBean(OpenAITranslationService.class);
            
            // 测试翻译服务是否可用
            boolean isAvailable = translationService.isServiceAvailable(provider);
            
            if (!isAvailable) {
                logger.error("{}翻译服务不可用", provider.getName());
                context.close();
                System.exit(1);
                return;
            }
            
            logger.info("{}翻译服务可用，开始测试翻译...", provider.getName());
            
            // 测试翻译
            String testText = "Hello, world! This is a test message for translation.";
            String translatedText = translationService.testTranslation(testText, provider);
            
            logger.info("测试翻译成功！");
            logger.info("原文: {}", testText);
            logger.info("译文: {}", translatedText);
            
            // 退出上下文
            context.close();
            
            logger.info("翻译测试完成！");
            System.exit(0);
            
        } catch (TranslationException e) {
            logger.error("翻译测试失败: {}", e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            logger.error("翻译测试过程中出错", e);
            System.exit(1);
        }
    }
    
    /**
     * 显示当前配置
     */
    private static void showConfig(String[] args) {
        try {
            logger.info("显示当前配置...");
            
            // 启动Spring上下文
            ConfigurableApplicationContext context = SpringApplication.run(ConfigTestTool.class, args);
            
            // 获取配置属性
            TranslationProperties properties = context.getBean(TranslationProperties.class);
            
            logger.info("=== 当前配置 ===");
            
            // OpenAI配置
            TranslationProperties.ProviderConfig openaiConfig = properties.getApi().getOpenai();
            if (openaiConfig != null) {
                logger.info("OpenAI配置:");
                logger.info("  Base URL: {}", openaiConfig.getBaseUrl());
                logger.info("  Model: {}", openaiConfig.getModel());
                logger.info("  Max Tokens: {}", openaiConfig.getMaxTokens());
                logger.info("  Temperature: {}", openaiConfig.getTemperature());
                logger.info("  API Key: {}", openaiConfig.getApiKey() != null && !openaiConfig.getApiKey().isEmpty() ? "已配置" : "未配置");
            }
            
            // DeepSeek配置
            TranslationProperties.ProviderConfig deepseekConfig = properties.getApi().getDeepseek();
            if (deepseekConfig != null) {
                logger.info("DeepSeek配置:");
                logger.info("  Base URL: {}", deepseekConfig.getBaseUrl());
                logger.info("  Model: {}", deepseekConfig.getModel());
                logger.info("  API Key: {}", deepseekConfig.getApiKey() != null && !deepseekConfig.getApiKey().isEmpty() ? "已配置" : "未配置");
            }
            
            // 翻译设置
            TranslationProperties.TranslationSettings settings = properties.getSettings();
            if (settings != null) {
                logger.info("翻译设置:");
                logger.info("  默认提供商: {}", settings.getDefaultProvider());
                logger.info("  批处理大小: {}", settings.getBatchSize());
                logger.info("  重试次数: {}", settings.getRetryCount());
                logger.info("  重试间隔: {}ms", settings.getRetryDelay());
            }
            
            // 退出上下文
            context.close();
            
        } catch (Exception e) {
            logger.error("显示配置时出错", e);
            System.exit(1);
        }
    }
    
    /**
     * 打印使用说明
     */
    private static void printUsage() {
        System.out.println("=== EPUB翻译器 - 配置测试工具 ===");
        System.out.println();
        System.out.println("用法: java -cp target/classes:target/dependency/* com.translator.ConfigTestTool <命令> [参数]");
        System.out.println();
        System.out.println("命令:");
        System.out.println("  validate        - 验证所有翻译服务配置");
        System.out.println("  test-translation <provider> - 测试翻译服务 (provider: openai 或 deepseek)");
        System.out.println("  show-config     - 显示当前配置");
        System.out.println();
        System.out.println("示例:");
        System.out.println("  java ConfigTestTool validate");
        System.out.println("  java ConfigTestTool test-translation openai");
        System.out.println("  java ConfigTestTool show-config");
        System.out.println();
        System.out.println("环境变量:");
        System.out.println("  OPENAI_API_KEY      - OpenAI API密钥");
        System.out.println("  OPENAI_BASE_URL     - OpenAI Base URL (可选)");
        System.out.println("  OPENAI_MODEL        - OpenAI模型 (可选)");
        System.out.println("  DEEPSEEK_API_KEY    - DeepSeek API密钥");
        System.out.println("  DEEPSEEK_BASE_URL   - DeepSeek Base URL (可选)");
    }
}