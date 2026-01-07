package com.translator.application.service;

import com.translator.application.command.TranslateBookCommand;
import com.translator.application.dto.BookDto;
import com.translator.application.dto.TranslationProgressDto;
import com.translator.domain.exception.TranslationException;
import com.translator.domain.model.Book;
import com.translator.domain.model.Page;
import com.translator.domain.repository.BookRepository;
import com.translator.domain.service.TranslationService;
import com.translator.domain.valueobject.BookMetadata;
import com.translator.domain.valueobject.TranslationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 书籍应用服务
 * 协调领域对象完成用例，不包含业务逻辑
 */
@Service
public class BookApplicationService {
    
    private static final Logger logger = LoggerFactory.getLogger(BookApplicationService.class);
    
    private final BookRepository bookRepository;
    private final TranslationService translationService;
    private final EpubProcessingService epubProcessingService;
    
    public BookApplicationService(BookRepository bookRepository, 
                                 TranslationService translationService,
                                 EpubProcessingService epubProcessingService) {
        this.bookRepository = bookRepository;
        this.translationService = translationService;
        this.epubProcessingService = epubProcessingService;
    }
    
    /**
     * 翻译书籍
     */
    public BookDto translateBook(TranslateBookCommand command) throws TranslationException {
        logger.info("开始翻译书籍: {}", command.getFilePath());
        
        // 1. 解析EPUB文件
        Book book = epubProcessingService.parseEpub(command.getFilePath());
        
        // 2. 选择翻译提供商
        TranslationProvider provider = command.getProvider() != null ? 
            command.getProvider() : TranslationProvider.OPENAI;
        
        // 3. 检查翻译服务可用性
        if (!translationService.isServiceAvailable(provider)) {
            throw new TranslationException("翻译服务不可用: " + provider.getName());
        }
        
        // 4. 保存书籍信息
        book = bookRepository.save(book);
        
        // 5. 执行翻译
        book.markTranslationStarted();
        try {
            translationService.translateBook(book, provider);
            book.markTranslationCompleted();
            logger.info("书籍翻译完成: {}", book.getOriginalFileName());
        } catch (Exception e) {
            book.markTranslationFailed(e.getMessage());
            logger.error("书籍翻译失败: {}", book.getOriginalFileName(), e);
            throw new TranslationException("翻译失败: " + e.getMessage(), e);
        }
        
        // 6. 生成翻译后的EPUB文件
        String outputPath = epubProcessingService.createTranslatedEpub(book, command.getOutputDirectory());
        
        // 7. 更新书籍信息
        book = bookRepository.save(book);
        
        return BookDto.fromDomain(book, outputPath);
    }
    
    /**
     * 获取翻译进度
     */
    public TranslationProgressDto getTranslationProgress(String bookId) {
        Optional<Book> bookOpt = bookRepository.findById(bookId);
        if (bookOpt.isEmpty()) {
            throw new IllegalArgumentException("书籍不存在: " + bookId);
        }
        
        Book book = bookOpt.get();
        return TranslationProgressDto.fromDomain(book);
    }
    
    /**
     * 获取书籍信息
     */
    public BookDto getBook(String bookId) {
        Optional<Book> bookOpt = bookRepository.findById(bookId);
        if (bookOpt.isEmpty()) {
            throw new IllegalArgumentException("书籍不存在: " + bookId);
        }
        
        return BookDto.fromDomain(bookOpt.get(), null);
    }
    
    /**
     * 列出所有书籍
     */
    public List<BookDto> listBooks() {
        // 这里可以实现分页查询
        return List.of(); // 简化实现
    }
    
    /**
     * 删除书籍
     */
    public void deleteBook(String bookId) {
        Optional<Book> bookOpt = bookRepository.findById(bookId);
        if (bookOpt.isPresent()) {
            bookRepository.delete(bookOpt.get());
        }
    }
}