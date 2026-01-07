package com.translator.domain.service;

import com.translator.domain.model.Book;
import com.translator.domain.model.Page;
import com.translator.domain.valueobject.TranslationProvider;
import com.translator.domain.exception.TranslationException;

/**
 * 翻译服务接口
 * 领域服务，处理翻译相关的业务逻辑
 */
public interface TranslationService {
    
    /**
     * 翻译书籍
     */
    void translateBook(Book book, TranslationProvider provider) throws TranslationException;
    
    /**
     * 翻译单个页面
     */
    void translatePage(Page page, TranslationProvider provider) throws TranslationException;
    
    /**
     * 批量翻译页面
     */
    void translatePages(java.util.List<Page> pages, TranslationProvider provider) throws TranslationException;
    
    /**
     * 检测文本语言
     */
    String detectLanguage(String text) throws TranslationException;
    
    /**
     * 检查翻译服务是否可用
     */
    boolean isServiceAvailable(TranslationProvider provider);
}