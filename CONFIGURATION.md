# EPUB翻译器 - 配置指南

## 概述

本指南详细介绍了EPUB翻译器的所有配置选项，特别是如何配置自定义的OpenAI和DeepSeek API地址。

## 环境变量配置

### OpenAI配置

| 环境变量 | 说明 | 默认值 | 示例 |
|----------|------|--------|------|
| `OPENAI_API_KEY` | OpenAI API密钥 | 无 | `sk-xxxxxxxxxxxxxxxx` |
| `OPENAI_BASE_URL` | OpenAI API基础地址 | `https://api.openai.com` | `https://api.openai-proxy.org` |
| `OPENAI_MODEL` | 使用的模型 | `gpt-3.5-turbo` | `gpt-4` |
| `OPENAI_MAX_TOKENS` | 最大token数 | `2000` | `4000` |
| `OPENAI_TEMPERATURE` | 温度参数 | `0.3` | `0.7` |

### DeepSeek配置

| 环境变量 | 说明 | 默认值 | 示例 |
|----------|------|--------|------|
| `DEEPSEEK_API_KEY` | DeepSeek API密钥 | 无 | `your-deepseek-key` |
| `DEEPSEEK_BASE_URL` | DeepSeek API基础地址 | `https://api.deepseek.com` | `https://custom.deepseek.com` |
| `DEEPSEEK_MODEL` | 使用的模型 | `deepseek-chat` | `deepseek-coder` |
| `DEEPSEEK_MAX_TOKENS` | 最大token数 | `2000` | `4000` |
| `DEEPSEEK_TEMPERATURE` | 温度参数 | `0.3` | `0.7` |

## 常见配置场景

### 1. 标准OpenAI配置
```bash
export OPENAI_API_KEY="sk-your-openai-api-key"
export OPENAI_BASE_URL="https://api.openai.com"
export OPENAI_MODEL="gpt-3.5-turbo"
export OPENAI_MAX_TOKENS="2000"
export OPENAI_TEMPERATURE="0.3"
```

### 2. Azure OpenAI配置
```bash
export OPENAI_API_KEY="your-azure-api-key"
export OPENAI_BASE_URL="https://your-resource.openai.azure.com/openai"
export OPENAI_MODEL="gpt-35-turbo"  # 注意Azure使用的是gpt-35-turbo
export OPENAI_MAX_TOKENS="2000"
export OPENAI_TEMPERATURE="0.3"
```

### 3. 国内代理服务配置
```bash
export OPENAI_API_KEY="your-proxy-api-key"
export OPENAI_BASE_URL="https://api.openai-proxy.org"
export OPENAI_MODEL="gpt-3.5-turbo"
export OPENAI_MAX_TOKENS="2000"
export OPENAI_TEMPERATURE="0.3"
```

### 4. 其他OpenAI兼容服务
```bash
export OPENAI_API_KEY="your-custom-api-key"
export OPENAI_BASE_URL="https://your-custom-api.com/v1"
export OPENAI_MODEL="your-custom-model"
export OPENAI_MAX_TOKENS="2000"
export OPENAI_TEMPERATURE="0.3"
```

### 5. DeepSeek标准配置
```bash
export DEEPSEEK_API_KEY="your-deepseek-api-key"
export DEEPSEEK_BASE_URL="https://api.deepseek.com"
export DEEPSEEK_MODEL="deepseek-chat"
export DEEPSEEK_MAX_TOKENS="2000"
export DEEPSEEK_TEMPERATURE="0.3"
```

## 配置验证

### 使用配置测试工具

```bash
# 验证所有配置
mvn compile exec:java -Dexec.mainClass="com.translator.ConfigTestTool" -Dexec.args="validate"

# 显示当前配置
mvn compile exec:java -Dexec.mainClass="com.translator.ConfigTestTool" -Dexec.args="show-config"

# 测试OpenAI翻译服务
mvn compile exec:java -Dexec.mainClass="com.translator.ConfigTestTool" -Dexec.args="test-translation openai"

# 测试DeepSeek翻译服务
mvn compile exec:java -Dexec.mainClass="com.translator.ConfigTestTool" -Dexec.args="test-translation deepseek"
```

### 使用运行脚本
```bash
# 运行脚本包含配置验证
./run-example.sh
# 选择选项5: 验证API配置
# 选择选项6: 测试翻译服务
```

### 使用配置设置脚本
```bash
# 交互式配置脚本
./setup-custom-api.sh

# 按照提示选择配置类型并输入相关信息
```

## 配置文件方式

除了环境变量，也可以通过配置文件进行设置：

编辑 `src/main/resources/application.yml`：

```yaml
translation:
  api:
    openai:
      base-url: "https://your-custom-api.com"  # 会覆盖环境变量
      api-key: "your-api-key"                  # 会覆盖环境变量
      model: "gpt-3.5-turbo"                   # 会覆盖环境变量
      max-tokens: 2000                         # 会覆盖环境变量
      temperature: 0.3                         # 会覆盖环境变量
    deepseek:
      base-url: "https://api.deepseek.com"
      api-key: "your-deepseek-api-key"
      model: "deepseek-chat"
      max-tokens: 2000
      temperature: 0.3
  settings:
    default-provider: "openai"  # 默认翻译服务
    batch-size: 2000           # 批处理大小
    retry-count: 3             # 重试次数
    retry-delay: 1000          # 重试间隔（毫秒）
```

**注意**: 环境变量的优先级高于配置文件。

## 配置优先级

1. **环境变量** - 最高优先级
2. **配置文件** (`application.yml`) - 中等优先级
3. **默认值** - 最低优先级

## 常见问题

### Q: 如何确认我的自定义API地址配置正确？

A: 使用配置验证工具：
```bash
mvn compile exec:java -Dexec.mainClass="com.translator.ConfigTestTool" -Dexec.args="validate"
```

### Q: 支持哪些OpenAI兼容的服务？

A: 支持任何兼容OpenAI API格式的服务，包括：
- Azure OpenAI Service
- 国内代理服务
- 自托管的OpenAI兼容API
- 其他第三方OpenAI兼容服务

### Q: 如何调试网络连接问题？

A: 启用调试日志：
```bash
export LOGGING_LEVEL_COM_TRANSLATOR=DEBUG
./run-example.sh
```

### Q: Base URL需要什么格式？

A: Base URL需要完整的HTTP/HTTPS格式：
- ✅ `https://api.openai.com`
- ✅ `https://your-resource.openai.azure.com/openai`
- ❌ `api.openai.com` (缺少协议)
- ❌ `https://api.openai.com/` (尾部有斜杠，会自动处理)

### Q: 可以同时配置OpenAI和DeepSeek吗？

A: 可以，应用会根据用户选择使用相应的服务。

## 高级配置

### 批处理配置
```bash
export TRANSLATION_SETTINGS_BATCH_SIZE=1000  # 减小批处理大小
export TRANSLATION_SETTINGS_RETRY_COUNT=5    # 增加重试次数
export TRANSLATION_SETTINGS_RETRY_DELAY=2000 # 增加重试间隔
```

### 日志配置
```bash
export LOGGING_LEVEL_COM_TRANSLATOR=DEBUG    # 启用调试日志
export LOGGING_LEVEL_ROOT=WARN               # 减少通用日志
```

### JVM配置
```bash
export MAVEN_OPTS="-Xmx2g -Xms1g"           # 增加内存
export JAVA_OPTS="-Dfile.encoding=UTF-8"    # 设置文件编码
```

## 配置示例脚本

### 快速配置标准OpenAI
```bash
#!/bin/bash
export OPENAI_API_KEY="your-api-key-here"
export OPENAI_BASE_URL="https://api.openai.com"
export OPENAI_MODEL="gpt-3.5-turbo"
./run-example.sh
```

### 快速配置Azure OpenAI
```bash
#!/bin/bash
export OPENAI_API_KEY="your-azure-key-here"
export OPENAI_BASE_URL="https://your-resource.openai.azure.com/openai"
export OPENAI_MODEL="gpt-35-turbo"
./run-example.sh
```

### 快速配置国内代理
```bash
#!/bin/bash
export OPENAI_API_KEY="your-proxy-key-here"
export OPENAI_BASE_URL="https://api.openai-proxy.org"
export OPENAI_MODEL="gpt-3.5-turbo"
./run-example.sh
```

## 故障排除

### 配置验证失败

1. **检查URL格式**: 确保包含协议前缀
2. **测试网络连接**: 使用`curl`或浏览器访问API地址
3. **验证API密钥**: 确认密钥有效且有足够额度
4. **检查防火墙**: 确保网络环境允许访问API服务

### 翻译服务不可用

1. **运行配置测试**: 
   ```bash
   mvn compile exec:java -Dexec.mainClass="com.translator.ConfigTestTool" -Dexec.args="validate"
   ```

2. **检查服务状态**: 访问API提供商的状态页面
3. **查看详细日志**: 
   ```bash
   export LOGGING_LEVEL_COM_TRANSLATOR=DEBUG
   ./run-example.sh
   ```

### 网络超时

1. **增加超时时间**: 修改配置文件中的重试参数
2. **检查网络代理**: 如果使用代理，确保配置正确
3. **选择其他服务**: 尝试使用不同的翻译服务

## 获取帮助

如果配置过程中遇到问题：

1. 查看详细日志输出
2. 使用配置验证工具
3. 运行配置测试
4. 参考本指南的故障排除部分
5. 提交Issue获取帮助

---

**配置完成！现在您可以享受自定义API带来的灵活翻译体验了！ 🚀**