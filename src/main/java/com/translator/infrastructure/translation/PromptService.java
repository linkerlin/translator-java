package com.translator.infrastructure.translation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Prompt管理服务
 * 负责从 AGENTS.md 文件中读取系统提示词
 */
@Service
public class PromptService {

    private static final Logger logger = LoggerFactory.getLogger(PromptService.class);
    private static final String DEFAULT_PROMPT = "You are a professional translator. Translate the following English text to Chinese. Preserve the HTML structure and formatting. Only return the translated text without any explanations.";
    private static final String AGENTS_FILE = "AGENTS.md";

    /**
     * 获取系统提示词
     */
    public String getSystemPrompt() {
        try {
            Path path = Paths.get(AGENTS_FILE);
            if (!Files.exists(path)) {
                logger.warn("{} 文件不存在，使用默认提示词", AGENTS_FILE);
                return DEFAULT_PROMPT;
            }

            String content = Files.readString(path);
            String prompt = extractPromptFromMarkdown(content);
            
            if (prompt == null || prompt.trim().isEmpty()) {
                logger.warn("无法从 {} 中提取提示词，使用默认提示词", AGENTS_FILE);
                return DEFAULT_PROMPT;
            }
            
            logger.debug("已加载系统提示词，长度: {}", prompt.length());
            return prompt;
            
        } catch (IOException e) {
            logger.error("读取 {} 失败，使用默认提示词", AGENTS_FILE, e);
            return DEFAULT_PROMPT;
        }
    }

    /**
     * 从Markdown内容中提取代码块
     * 寻找 "## Translation Agent System Prompt" 下面的第一个代码块
     */
    private String extractPromptFromMarkdown(String content) {
        // 查找标题
        String header = "## Translation Agent System Prompt";
        int headerIndex = content.indexOf(header);
        if (headerIndex == -1) {
            return null;
        }

        // 从标题后开始寻找代码块
        String contentAfterHeader = content.substring(headerIndex + header.length());
        
        // 匹配 ```text ... ``` 或 ``` ... ```
        Pattern pattern = Pattern.compile("```(?:text)?\\s*([\\s\\S]*?)\\s*```");
        Matcher matcher = pattern.matcher(contentAfterHeader);
        
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        return null;
    }
}
