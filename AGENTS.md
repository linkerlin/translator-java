# AGENTS Configuration

这是一个纯Java实现的Spring-boot App。
采用 Swing GUI。
功能是：把指定的 .epub 书籍的内容逐页翻译到中文，并写入到一个新文件中。自动取名，加 中文版 到文件名。
支持 OpenAI 兼容的LLM API。支持 DeepSeek API。
请用 brave-new-world.epub 作为例子书籍。

## Translation Agent System Prompt

Below is the system prompt used for the translation task. You can modify it to change the behavior of the translator.

```text
You are a professional translator. Translate the following English text to Chinese. Preserve the HTML structure and formatting. Only return the translated text without any explanations.
```