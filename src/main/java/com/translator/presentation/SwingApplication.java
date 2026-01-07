package com.translator.presentation;

import com.translator.EpubTranslatorApplication;
import com.translator.presentation.controller.MainController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.*;

/**
 * Swing应用程序启动器
 * 负责启动Spring Boot应用和Swing界面
 */
public class SwingApplication {
    
    private static final Logger logger = LoggerFactory.getLogger(SwingApplication.class);
    
    private static ConfigurableApplicationContext applicationContext;
    
    public static void main(String[] args) {
        logger.info("启动EPUB翻译器应用程序");
        
        try {
            // 设置系统属性以优化Swing和Spring的集成
            System.setProperty("java.awt.headless", "false");
            System.setProperty("spring.main.web-application-type", "none");
            
            // 启动Spring Boot应用
            logger.info("正在启动Spring Boot应用上下文...");
            applicationContext = new SpringApplicationBuilder(EpubTranslatorApplication.class)
                    .headless(false)
                    .run(args);
            
            logger.info("Spring Boot应用上下文启动完成");
            
            // 注册关闭钩子
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("应用程序正在关闭...");
                shutdownApplication();
            }));
            
            // 在主线程中保持应用运行
            logger.info("应用程序已完全启动，按Ctrl+C退出");
            
        } catch (Exception e) {
            logger.error("启动应用程序失败", e);
            shutdownApplication();
            System.exit(1);
        }
    }
    
    /**
     * 关闭应用程序
     */
    private static void shutdownApplication() {
        try {
            if (applicationContext != null && applicationContext.isActive()) {
                logger.info("正在关闭Spring应用上下文...");
                
                // 获取主控制器并执行清理
                try {
                    MainController controller = applicationContext.getBean(MainController.class);
                    controller.cleanup();
                } catch (Exception e) {
                    logger.warn("清理控制器资源时出错", e);
                }
                
                // 关闭Spring上下文
                applicationContext.close();
                logger.info("Spring应用上下文已关闭");
            }
        } catch (Exception e) {
            logger.error("关闭应用程序时出错", e);
        }
    }
    
    /**
     * 获取应用上下文（用于测试和调试）
     */
    public static ConfigurableApplicationContext getApplicationContext() {
        return applicationContext;
    }
}