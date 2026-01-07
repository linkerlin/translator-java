package com.translator;

import com.translator.infrastructure.config.TranslationProperties;
import com.translator.infrastructure.translation.ConfigValidationTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * EPUB翻译器主应用
 */
@SpringBootApplication
@EnableConfigurationProperties(TranslationProperties.class)
public class EpubTranslatorApplication {
    
    private static final Logger logger = LoggerFactory.getLogger(EpubTranslatorApplication.class);
    
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(EpubTranslatorApplication.class, args);
        
        // 在应用启动后进行配置验证
        try {
            validateConfiguration(context);
        } catch (Exception e) {
            logger.error("配置验证失败", e);
        }
    }
    
    @org.springframework.context.annotation.Bean
    public ConfigValidationTool configValidationTool(TranslationProperties properties) {
        return new ConfigValidationTool(properties);
    }
    
    /**
     * 验证应用配置
     */
    private static void validateConfiguration(ConfigurableApplicationContext context) {
        try {
            logger.info("开始验证应用配置...");
            
            // 获取配置属性
            TranslationProperties properties = context.getBean(TranslationProperties.class);
            
            // 创建配置验证工具
            ConfigValidationTool validationTool = new ConfigValidationTool(properties);
            
            // 执行验证
            ConfigValidationTool.ValidationResult result = validationTool.validateAllConfigurations();
            
            // 打印验证结果
            result.printResult();
            
            // 如果有错误，给出建议
            if (result.hasErrors()) {
                logger.error("应用配置存在错误，请检查环境变量和配置文件");
                logger.info("使用 ./setup-custom-api.sh 脚本可以帮助您正确配置API");
            } else if (result.hasWarnings()) {
                logger.warn("应用配置存在警告，但可以使用");
            } else {
                logger.info("所有配置验证通过！");
            }
            
        } catch (Exception e) {
            logger.error("配置验证过程中出错", e);
        }
    }
}