package com.translator.infrastructure.repository;

import com.translator.domain.model.Book;
import com.translator.domain.repository.BookRepository;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存实现的书籍仓库
 * 实际项目中应该使用数据库存储
 */
@Repository
public class InMemoryBookRepository implements BookRepository {
    
    private final Map<String, Book> books = new ConcurrentHashMap<>();
    
    @Override
    public Book save(Book book) {
        books.put(book.getId(), book);
        return book;
    }
    
    @Override
    public Optional<Book> findById(String id) {
        return Optional.ofNullable(books.get(id));
    }
    
    @Override
    public Optional<Book> findByFileName(String fileName) {
        return books.values().stream()
            .filter(book -> book.getOriginalFileName().equals(fileName))
            .findFirst();
    }
    
    @Override
    public void delete(Book book) {
        books.remove(book.getId());
    }
    
    @Override
    public boolean existsById(String id) {
        return books.containsKey(id);
    }
    
    /**
     * 获取所有书籍（用于测试和调试）
     */
    public Collection<Book> findAll() {
        return new ArrayList<>(books.values());
    }
    
    /**
     * 清空仓库（用于测试）
     */
    public void clear() {
        books.clear();
    }
}