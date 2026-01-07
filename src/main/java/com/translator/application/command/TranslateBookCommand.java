package com.translator.application.command;

import com.translator.domain.valueobject.TranslationProvider;

/**
 * 翻译书籍命令
 * 表示用户翻译书籍的请求
 */
public class TranslateBookCommand {
    private final String filePath;
    private final TranslationProvider provider;
    private final String outputDirectory;
    
    public TranslateBookCommand(String filePath, TranslationProvider provider, String outputDirectory) {
        this.filePath = filePath;
        this.provider = provider;
        this.outputDirectory = outputDirectory;
    }
    
    public TranslateBookCommand(String filePath) {
        this(filePath, null, System.getProperty("user.dir"));
    }
    
    // Getters
    public String getFilePath() {
        return filePath;
    }
    
    public TranslationProvider getProvider() {
        return provider;
    }
    
    public String getOutputDirectory() {
        return outputDirectory;
    }
}