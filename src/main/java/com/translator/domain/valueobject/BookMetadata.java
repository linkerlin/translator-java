package com.translator.domain.valueobject;

import java.util.List;

/**
 * 书籍元数据值对象
 * 包含书籍的基本信息，不可变
 */
public class BookMetadata {
    private final String title;
    private final List<String> authors;
    private final String language;
    private final String publisher;
    private final String description;
    private final String isbn;

    public BookMetadata(String title, List<String> authors, String language, 
                       String publisher, String description, String isbn) {
        this.title = title;
        this.authors = authors != null ? List.copyOf(authors) : List.of();
        this.language = language;
        this.publisher = publisher;
        this.description = description;
        this.isbn = isbn;
    }

    public BookMetadata withTranslatedTitle(String translatedTitle) {
        return new BookMetadata(translatedTitle, authors, language, 
                              publisher, description, isbn);
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public String getLanguage() {
        return language;
    }

    public String getPublisher() {
        return publisher;
    }

    public String getDescription() {
        return description;
    }

    public String getIsbn() {
        return isbn;
    }
}