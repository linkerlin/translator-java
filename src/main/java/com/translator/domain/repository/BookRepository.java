package com.translator.domain.repository;

import com.translator.domain.model.Book;

import java.util.Optional;

/**
 * 书籍仓库接口
 * 定义书籍持久化的领域接口
 */
public interface BookRepository {
    
    /**
     * 保存书籍
     */
    Book save(Book book);
    
    /**
     * 根据ID查找书籍
     */
    Optional<Book> findById(String id);
    
    /**
     * 根据文件名查找书籍
     */
    Optional<Book> findByFileName(String fileName);
    
    /**
     * 删除书籍
     */
    void delete(Book book);
    
    /**
     * 检查书籍是否存在
     */
    boolean existsById(String id);
}