package com.translator.domain.exception;

/**
 * 翻译服务异常
 */
public class TranslationException extends Exception {
    
    public TranslationException(String message) {
        super(message);
    }
    
    public TranslationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public TranslationException(Throwable cause) {
        super(cause);
    }
}