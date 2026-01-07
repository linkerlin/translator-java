package com.translator.domain.model;

import com.translator.domain.valueobject.BookMetadata;
import com.translator.domain.valueobject.TranslationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 书籍领域模型测试
 */
public class BookTest {
    
    private Book book;
    private static final String TEST_FILE_NAME = "test-book.epub";
    
    @BeforeEach
    void setUp() {
        book = new Book(TEST_FILE_NAME);
    }
    
    @Test
    void testBookCreation() {
        assertNotNull(book.getId());
        assertEquals(TEST_FILE_NAME, book.getOriginalFileName());
        assertEquals("test-book 中文版.epub", book.getTranslatedFileName());
        assertEquals(TranslationStatus.PENDING, book.getTranslationStatus());
        assertEquals(0, book.getTotalPages());
        assertEquals(0, book.getTranslatedPages());
        assertEquals(0.0, book.getTranslationProgress());
    }
    
    @Test
    void testAddPage() {
        Page page1 = new Page("page1", 1, "Chapter 1", "Content of chapter 1");
        Page page2 = new Page("page2", 2, "Chapter 2", "Content of chapter 2");
        
        book.addPage(page1);
        book.addPage(page2);
        
        assertEquals(2, book.getTotalPages());
        assertEquals(0, book.getTranslatedPages());
        assertEquals(0.0, book.getTranslationProgress());
        
        List<Page> pages = book.getPages();
        assertEquals(2, pages.size());
        assertEquals("page1", pages.get(0).getId());
        assertEquals("page2", pages.get(1).getId());
    }
    
    @Test
    void testUpdateMetadata() {
        List<String> authors = Arrays.asList("Author 1", "Author 2");
        BookMetadata metadata = new BookMetadata(
            "Test Book",
            authors,
            "en",
            "Test Publisher",
            "Test Description",
            "123-456-789"
        );
        
        book.updateMetadata(metadata);
        
        BookMetadata retrievedMetadata = book.getMetadata();
        assertNotNull(retrievedMetadata);
        assertEquals("Test Book", retrievedMetadata.getTitle());
        assertEquals(2, retrievedMetadata.getAuthors().size());
        assertEquals("Author 1", retrievedMetadata.getAuthors().get(0));
        assertEquals("en", retrievedMetadata.getLanguage());
        assertEquals("Test Publisher", retrievedMetadata.getPublisher());
        assertEquals("Test Description", retrievedMetadata.getDescription());
        assertEquals("123-456-789", retrievedMetadata.getIsbn());
    }
    
    @Test
    void testTranslationStatusTransitions() {
        assertEquals(TranslationStatus.PENDING, book.getTranslationStatus());
        
        book.markTranslationStarted();
        assertEquals(TranslationStatus.IN_PROGRESS, book.getTranslationStatus());
        
        book.markTranslationCompleted();
        assertEquals(TranslationStatus.COMPLETED, book.getTranslationStatus());
        assertTrue(book.isTranslationCompleted());
    }
    
    @Test
    void testTranslationFailed() {
        book.markTranslationStarted();
        book.markTranslationFailed("API error");
        
        assertEquals(TranslationStatus.FAILED, book.getTranslationStatus());
        assertFalse(book.isTranslationCompleted());
    }
    
    @Test
    void testTranslationProgress() {
        // 添加页面
        for (int i = 1; i <= 5; i++) {
            Page page = new Page("page" + i, i, "Chapter " + i, "Content " + i);
            book.addPage(page);
        }
        
        assertEquals(5, book.getTotalPages());
        assertEquals(0, book.getTranslatedPages());
        assertEquals(0.0, book.getTranslationProgress());
        
        // 模拟翻译一些页面
        List<Page> pages = book.getPages();
        pages.get(0).translate("翻译内容1");
        pages.get(1).translate("翻译内容2");
        
        assertEquals(2, book.getTranslatedPages());
        assertEquals(40.0, book.getTranslationProgress(), 0.01);
        
        // 翻译所有页面
        for (Page page : pages) {
            page.translate("翻译的" + page.getOriginalContent());
        }
        
        assertEquals(5, book.getTranslatedPages());
        assertEquals(100.0, book.getTranslationProgress(), 0.01);
    }
    
    @Test
    void testTranslatedFileNameGeneration() {
        // 测试带有.epub扩展名的文件
        Book epubBook = new Book("novel.epub");
        assertEquals("novel 中文版.epub", epubBook.getTranslatedFileName());
        
        // 测试不带扩展名的文件
        Book noExtBook = new Book("document");
        assertEquals("document 中文版", noExtBook.getTranslatedFileName());
        
        // 测试带有其他扩展名的文件
        Book otherExtBook = new Book("book.pdf");
        assertEquals("book.pdf 中文版", otherExtBook.getTranslatedFileName());
    }
    
    @Test
    void testImmutablePagesList() {
        Page page = new Page("test", 1, "Test", "Content");
        book.addPage(page);
        
        List<Page> pages = book.getPages();
        int originalSize = pages.size();
        
        // 尝试修改返回的列表
        pages.clear();
        
        // 验证原始书籍的页面没有被修改
        assertEquals(originalSize, book.getTotalPages());
    }
}