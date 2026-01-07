package com.translator.presentation.controller;

import com.translator.application.command.TranslateBookCommand;
import com.translator.application.dto.BookDto;
import com.translator.application.dto.TranslationProgressDto;
import com.translator.application.service.BookApplicationService;
import com.translator.domain.exception.TranslationException;
import com.translator.domain.valueobject.TranslationProvider;
import com.translator.presentation.gui.MainFrame;
import com.translator.presentation.gui.TranslationProgressDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Controller;

import javax.swing.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 主控制器
 * 协调GUI和应用服务之间的交互
 */
@Controller
public class MainController {
    
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);
    
    private final BookApplicationService bookService;
    private final com.translator.presentation.gui.SimpleMainFrame mainFrame;
    private final ExecutorService executorService;
    private final ScheduledExecutorService scheduledExecutorService;
    
    private TranslationProgressDialog progressDialog;
    private String currentBookId;
    private volatile boolean translationCancelled = false;
    
    public MainController(BookApplicationService bookService, com.translator.presentation.gui.SimpleMainFrame mainFrame) {
        this.bookService = bookService;
        this.mainFrame = mainFrame;
        this.executorService = Executors.newFixedThreadPool(2);
        this.scheduledExecutorService = Executors.newScheduledThreadPool(1);
    }
    
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        logger.info("应用程序已启动，准备显示主界面");
        
        // 在Swing事件分发线程中显示主界面
        SwingUtilities.invokeLater(() -> {
            try {
                // 设置系统外观
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                logger.warn("无法设置系统外观，使用默认外观", e);
            }
            
            mainFrame.showFrame();
        });
    }
    
    /**
     * 开始翻译书籍
     */
    public void startTranslation(String filePath, TranslationProvider provider, String outputDirectory) {
        if (filePath == null || filePath.trim().isEmpty()) {
            showError("请先选择EPUB文件");
            return;
        }
        
        translationCancelled = false;
        
        // 创建翻译命令
        TranslateBookCommand command = new TranslateBookCommand(filePath, provider, outputDirectory);
        
        // 显示进度对话框
        showProgressDialog(new java.io.File(filePath).getName());
        
        // 异步执行翻译
        CompletableFuture.runAsync(() -> {
            try {
                logger.info("开始翻译书籍: {}", filePath);
                
                // 执行翻译
                BookDto result = bookService.translateBook(command);
                currentBookId = result.getId();
                
                if (!translationCancelled) {
                    // 翻译成功
                    SwingUtilities.invokeLater(() -> {
                        if (progressDialog != null) {
                            progressDialog.updateProgress(createCompletedProgress(result));
                        }
                        showSuccess("翻译完成！\n输出文件: " + result.getTranslatedFileName());
                    });
                }
                
            } catch (TranslationException e) {
                logger.error("翻译失败", e);
                
                if (!translationCancelled) {
                    SwingUtilities.invokeLater(() -> {
                        if (progressDialog != null) {
                            progressDialog.showError(e.getMessage());
                        }
                        showError("翻译失败: " + e.getMessage());
                    });
                }
            } catch (Exception e) {
                logger.error("翻译过程中发生未知错误", e);
                
                if (!translationCancelled) {
                    SwingUtilities.invokeLater(() -> {
                        if (progressDialog != null) {
                            progressDialog.showError("未知错误: " + e.getMessage());
                        }
                        showError("翻译失败: " + e.getMessage());
                    });
                }
            }
        }, executorService);
        
        // 定期更新进度
        startProgressUpdateTask();
    }
    
    /**
     * 显示进度对话框
     */
    private void showProgressDialog(String bookName) {
        SwingUtilities.invokeLater(() -> {
            progressDialog = new TranslationProgressDialog(mainFrame, bookName);
            progressDialog.addCancelListener(e -> {
                int result = JOptionPane.showConfirmDialog(
                    progressDialog,
                    "确定要取消翻译吗？\n当前进度将会丢失。",
                    "确认取消",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
                );
                
                if (result == JOptionPane.YES_OPTION) {
                    translationCancelled = true;
                    progressDialog.dispose();
                    logger.info("用户取消了翻译");
                }
            });
            
            progressDialog.setVisible(true);
        });
    }
    
    /**
     * 启动进度更新任务
     */
    private void startProgressUpdateTask() {
        if (currentBookId == null) {
            return;
        }
        
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            if (translationCancelled || progressDialog == null) {
                return;
            }
            
            try {
                TranslationProgressDto progress = bookService.getTranslationProgress(currentBookId);
                
                SwingUtilities.invokeLater(() -> {
                    if (progressDialog != null && progressDialog.isVisible()) {
                        progressDialog.updateProgress(progress);
                    }
                });
                
                // 如果翻译完成或被取消，停止更新
                if (progress.getProgressPercentage() >= 100 || 
                    progress.getStatus().contains("完成") || 
                    progress.getStatus().contains("失败")) {
                    stopProgressUpdateTask();
                }
                
            } catch (Exception e) {
                logger.warn("更新翻译进度失败", e);
            }
        }, 0, 1, TimeUnit.SECONDS);
    }
    
    /**
     * 停止进度更新任务
     */
    private void stopProgressUpdateTask() {
        scheduledExecutorService.shutdown();
    }
    
    /**
     * 创建完成的进度信息
     */
    private TranslationProgressDto createCompletedProgress(BookDto book) {
        TranslationProgressDto progress = new TranslationProgressDto();
        progress.setBookId(book.getId());
        progress.setBookName(book.getOriginalFileName());
        progress.setStatus("翻译完成");
        progress.setTotalPages(book.getTotalPages());
        progress.setTranslatedPages(book.getTranslatedPages());
        progress.setProgressPercentage(100.0);
        progress.setCurrentPage("全部完成");
        progress.setEstimatedTimeRemaining("已完成");
        return progress;
    }
    
    /**
     * 显示错误消息
     */
    private void showError(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                mainFrame,
                message,
                "错误",
                JOptionPane.ERROR_MESSAGE
            );
        });
    }
    
    /**
     * 显示成功消息
     */
    private void showSuccess(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                mainFrame,
                message,
                "成功",
                JOptionPane.INFORMATION_MESSAGE
            );
        });
    }
    
    /**
     * 获取书籍信息
     */
    public void showBookDetails(String bookId) {
        try {
            BookDto book = bookService.getBook(bookId);
            
            String details = String.format(
                "书籍详情:\n\n" +
                "文件名: %s\n" +
                "翻译后文件名: %s\n" +
                "状态: %s\n" +
                "总页数: %d\n" +
                "已翻译页数: %d\n" +
                "翻译进度: %.1f%%\n" +
                "输出路径: %s",
                book.getOriginalFileName(),
                book.getTranslatedFileName(),
                book.getStatus(),
                book.getTotalPages(),
                book.getTranslatedPages(),
                book.getTranslationProgress(),
                book.getOutputPath() != null ? book.getOutputPath() : "未生成"
            );
            
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(
                    mainFrame,
                    details,
                    "书籍详情",
                    JOptionPane.INFORMATION_MESSAGE
                );
            });
            
        } catch (Exception e) {
            showError("获取书籍详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 清理资源
     */
    public void cleanup() {
        try {
            executorService.shutdown();
            scheduledExecutorService.shutdown();
            
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
            
            if (!scheduledExecutorService.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduledExecutorService.shutdownNow();
            }
            
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            scheduledExecutorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}