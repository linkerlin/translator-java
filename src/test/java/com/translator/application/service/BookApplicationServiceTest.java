package com.translator.application.service;

import com.translator.application.command.TranslateBookCommand;
import com.translator.application.dto.BookDto;
import com.translator.domain.exception.TranslationException;
import com.translator.domain.model.Book;
import com.translator.domain.model.Page;
import com.translator.domain.repository.BookRepository;
import com.translator.domain.service.TranslationService;
import com.translator.domain.valueobject.BookMetadata;
import com.translator.domain.valueobject.TranslationProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 书籍应用服务测试
 */
@ExtendWith(MockitoExtension.class)
public class BookApplicationServiceTest {
    
    @Mock
    private BookRepository bookRepository;
    
    @Mock
    private TranslationService translationService;
    
    @Mock
    private EpubProcessingService epubProcessingService;
    
    private BookApplicationService bookService;
    
    @BeforeEach
    void setUp() {
        bookService = new BookApplicationService(bookRepository, translationService, epubProcessingService);
    }
    
    @Test
    void testTranslateBookSuccess() throws TranslationException {
        // 准备测试数据
        String filePath = "/path/to/test.epub";
        String outputDir = "/output/dir";
        TranslationProvider provider = TranslationProvider.OPENAI;
        
        TranslateBookCommand command = new TranslateBookCommand(filePath, provider, outputDir);
        
        // 创建模拟的书籍
        Book mockBook = new Book("test.epub");
        BookMetadata metadata = new BookMetadata(
            "Test Book",
            Arrays.asList("Test Author"),
            "en",
            "Test Publisher",
            "Test Description",
            "123-456"
        );
        mockBook.updateMetadata(metadata);
        
        Page page1 = new Page("page1", 1, "Chapter 1", "Content 1");
        Page page2 = new Page("page2", 2, "Chapter 2", "Content 2");
        mockBook.addPage(page1);
        mockBook.addPage(page2);
        
        // 设置模拟行为
        when(epubProcessingService.parseEpub(filePath)).thenReturn(mockBook);
        when(translationService.isServiceAvailable(provider)).thenReturn(true);
        when(bookRepository.save(any(Book.class))).thenReturn(mockBook);
        when(epubProcessingService.createTranslatedEpub(any(Book.class), eq(outputDir)))
            .thenReturn("/output/dir/test 中文版.epub");
        
        // 执行测试
        BookDto result = bookService.translateBook(command);
        
        // 验证结果
        assertNotNull(result);
        assertEquals("test.epub", result.getOriginalFileName());
        assertEquals("test 中文版.epub", result.getTranslatedFileName());
        assertEquals(2, result.getTotalPages());
        assertEquals("/output/dir/test 中文版.epub", result.getOutputPath());
        
        // 验证交互
        verify(epubProcessingService).parseEpub(filePath);
        verify(translationService).isServiceAvailable(provider);
        verify(translationService).translateBook(mockBook, provider);
        verify(bookRepository, times(2)).save(mockBook);
        verify(epubProcessingService).createTranslatedEpub(mockBook, outputDir);
    }
    
    @Test
    void testTranslateBookServiceUnavailable() throws TranslationException {
        // 准备测试数据
        String filePath = "/path/to/test.epub";
        TranslationProvider provider = TranslationProvider.OPENAI;
        TranslateBookCommand command = new TranslateBookCommand(filePath, provider, "/output/dir");
        
        Book mockBook = new Book("test.epub");
        
        // 设置模拟行为 - 服务不可用
        when(epubProcessingService.parseEpub(filePath)).thenReturn(mockBook);
        when(translationService.isServiceAvailable(provider)).thenReturn(false);
        
        // 执行测试并验证异常
        TranslationException exception = assertThrows(TranslationException.class, () -> {
            bookService.translateBook(command);
        });
        
        assertTrue(exception.getMessage().contains("翻译服务不可用"));
        
        // 验证交互
        verify(epubProcessingService).parseEpub(filePath);
        verify(translationService).isServiceAvailable(provider);
        verify(translationService, never()).translateBook(any(), any());
    }
    
    @Test
    void testGetTranslationProgress() {
        // 准备测试数据
        Book mockBook = new Book("test.epub");
        String bookId = mockBook.getId();
        
        // 添加一些页面并模拟翻译进度
        for (int i = 1; i <= 5; i++) {
            Page page = new Page("page" + i, i, "Chapter " + i, "Content " + i);
            if (i <= 3) {
                page.translate("翻译内容" + i);
            }
            mockBook.addPage(page);
        }
        mockBook.markTranslationStarted();
        
        // 设置模拟行为
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(mockBook));
        
        // 执行测试
        var progress = bookService.getTranslationProgress(bookId);
        
        // 验证结果
        assertNotNull(progress);
        assertEquals(bookId, progress.getBookId());
        assertEquals("test.epub", progress.getBookName());
        assertEquals(5, progress.getTotalPages());
        assertEquals(3, progress.getTranslatedPages());
        assertEquals(60.0, progress.getProgressPercentage(), 0.01);
        
        verify(bookRepository).findById(bookId);
    }
    
    @Test
    void testGetTranslationProgressBookNotFound() {
        String bookId = "non-existent-book";
        
        // 设置模拟行为 - 书籍不存在
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());
        
        // 执行测试并验证异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookService.getTranslationProgress(bookId);
        });
        
        assertTrue(exception.getMessage().contains("书籍不存在"));
        
        verify(bookRepository).findById(bookId);
    }
    
    @Test
    void testGetBook() {
        // 准备测试数据
        Book mockBook = new Book("test.epub");
        String bookId = mockBook.getId();
        BookMetadata metadata = new BookMetadata(
            "Test Book",
            Arrays.asList("Test Author"),
            "en",
            "Test Publisher",
            "Test Description",
            "123-456"
        );
        mockBook.updateMetadata(metadata);
        
        // 设置模拟行为
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(mockBook));
        
        // 执行测试
        BookDto result = bookService.getBook(bookId);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(bookId, result.getId());
        assertEquals("test.epub", result.getOriginalFileName());
        assertEquals("test 中文版.epub", result.getTranslatedFileName());
        assertNotNull(result.getMetadata());
        assertEquals("Test Book", result.getMetadata().getTitle());
        
        verify(bookRepository).findById(bookId);
    }
    
    @Test
    void testDeleteBook() {
        // 准备测试数据
        String bookId = "test-book-id";
        Book mockBook = new Book("test.epub");
        
        // 设置模拟行为
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(mockBook));
        
        // 执行测试
        bookService.deleteBook(bookId);
        
        // 验证交互
        verify(bookRepository).findById(bookId);
        verify(bookRepository).delete(mockBook);
    }
    
    @Test
    void testDeleteNonExistentBook() {
        String bookId = "non-existent-book";
        
        // 设置模拟行为 - 书籍不存在
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());
        
        // 执行测试 - 应该不抛出异常
        assertDoesNotThrow(() -> {
            bookService.deleteBook(bookId);
        });
        
        verify(bookRepository).findById(bookId);
        verify(bookRepository, never()).delete(any());
    }
    
    @Test
    void testTranslateBookFallbackToDeepSeek() throws TranslationException {
        // 准备测试数据
        String filePath = "/path/to/test.epub";
        String outputDir = "/output/dir";
        TranslationProvider provider = TranslationProvider.OPENAI;
        
        TranslateBookCommand command = new TranslateBookCommand(filePath, provider, outputDir);
        
        Book mockBook = new Book("test.epub");
        
        // 设置模拟行为
        when(epubProcessingService.parseEpub(filePath)).thenReturn(mockBook);
        when(translationService.isServiceAvailable(provider)).thenReturn(true);
        when(bookRepository.save(any(Book.class))).thenReturn(mockBook);
        
        // 模拟OpenAI失败
        doThrow(new TranslationException("OpenAI Limit Exceeded"))
            .when(translationService).translateBook(mockBook, TranslationProvider.OPENAI);
            
        // 模拟DeepSeek可用并成功
        when(translationService.isServiceAvailable(TranslationProvider.DEEPSEEK)).thenReturn(true);
        doNothing().when(translationService).translateBook(mockBook, TranslationProvider.DEEPSEEK);
        
        when(epubProcessingService.createTranslatedEpub(any(Book.class), eq(outputDir)))
            .thenReturn("/output/dir/test 中文版.epub");
            
        // 执行测试
        BookDto result = bookService.translateBook(command);
        
        // 验证结果
        assertNotNull(result);
        assertEquals("/output/dir/test 中文版.epub", result.getOutputPath());
        
        // 验证交互
        verify(translationService).translateBook(mockBook, TranslationProvider.OPENAI);
        verify(translationService).isServiceAvailable(TranslationProvider.DEEPSEEK);
        verify(translationService).translateBook(mockBook, TranslationProvider.DEEPSEEK);
    }
}