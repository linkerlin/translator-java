# EPUB翻译器

一个基于领域驱动设计(DDD)架构的EPUB电子书翻译工具，支持OpenAI和DeepSeek API，提供Swing图形界面。

## 功能特性

- 📚 **EPUB格式支持**: 完整解析和生成EPUB文件
- 🌐 **多翻译服务**: 支持OpenAI兼容API和DeepSeek API
- 🖥️ **图形界面**: 基于Swing的用户友好界面
- 📊 **进度显示**: 实时显示翻译进度和剩余时间
- 🏗️ **DDD架构**: 采用领域驱动设计，代码结构清晰
- 🔄 **批量处理**: 支持批量翻译页面，提高效率
- ⚡ **异步处理**: 翻译过程不阻塞用户界面

## 技术架构

### 分层架构（DDD）

```
┌─────────────────────────────────────┐
│        表示层 (Presentation)        │
│   ├─ Swing GUI (MainFrame)         │
│   ├─ 控制器 (MainController)       │
│   └─ 进度对话框                     │
├─────────────────────────────────────┤
│        应用层 (Application)         │
│   ├─ 应用服务 (BookApplicationService) │
│   ├─ 命令对象 (TranslateBookCommand) │
│   └─ 数据传输对象 (DTO)             │
├─────────────────────────────────────┤
│        领域层 (Domain)              │
│   ├─ 实体 (Book, Page)             │
│   ├─ 值对象 (BookMetadata, TranslationRequest) │
│   ├─ 领域服务 (TranslationService) │
│   ├─ 仓库接口 (BookRepository)     │
│   └─ 异常 (TranslationException)   │
├─────────────────────────────────────┤
│      基础设施层 (Infrastructure)    │
│   ├─ 仓库实现 (InMemoryBookRepository) │
│   ├─ 翻译服务 (OpenAITranslationService) │
│   ├─ EPUB处理 (EpubProcessingService) │
│   └─ 配置 (TranslationProperties)  │
└─────────────────────────────────────┘
```

### 核心依赖

- **Spring Boot**: 依赖注入和配置管理
- **EPUBLib**: EPUB文件处理
- **Apache HttpClient**: HTTP请求处理
- **Jackson**: JSON处理
- **Swing**: 图形用户界面

## 快速开始

### 环境要求

- Java 17 或更高版本
- Maven 3.6 或更高版本
- OpenAI API密钥 或 DeepSeek API密钥

### 配置API密钥

在运行应用前，需要配置翻译服务的API密钥：

#### 环境变量方式（推荐）

```bash
# OpenAI
export OPENAI_API_KEY="your-openai-api-key"
export OPENAI_BASE_URL="https://api.openai.com"  # 可选，自定义API地址
export OPENAI_MODEL="gpt-3.5-turbo"              # 可选，自定义模型
export OPENAI_MAX_TOKENS="2000"                  # 可选，最大token数
export OPENAI_TEMPERATURE="0.3"                  # 可选，温度参数

# DeepSeek
export DEEPSEEK_API_KEY="your-deepseek-api-key"
export DEEPSEEK_BASE_URL="https://api.deepseek.com"  # 可选，自定义API地址
export DEEPSEEK_MODEL="deepseek-chat"                # 可选，自定义模型
export DEEPSEEK_MAX_TOKENS="2000"                    # 可选，最大token数
export DEEPSEEK_TEMPERATURE="0.3"                    # 可选，温度参数
```

#### 自定义OpenAI API地址

支持使用自定义的OpenAI兼容API服务，如：
- Azure OpenAI Service
- 国内代理服务
- 其他OpenAI兼容的API服务

示例：
```bash
# 使用Azure OpenAI
export OPENAI_BASE_URL="https://your-resource.openai.azure.com/openai"
export OPENAI_API_KEY="your-azure-api-key"
export OPENAI_MODEL="gpt-35-turbo"

# 使用国内代理
export OPENAI_BASE_URL="https://api.openai-proxy.org"
export OPENAI_API_KEY="your-proxy-api-key"
```

#### 配置验证和测试

使用配置测试工具验证API配置：
```bash
# 验证所有配置
./run-example.sh
# 选择选项5: 验证API配置

# 或者使用专门的配置测试工具
mvn compile exec:java -Dexec.mainClass="com.translator.ConfigTestTool" -Dexec.args="validate"

# 测试翻译服务
mvn compile exec:java -Dexec.mainClass="com.translator.ConfigTestTool" -Dexec.args="test-translation openai"
```

使用配置设置脚本快速配置：
```bash
# 运行配置设置脚本
./setup-custom-api.sh

# 按照提示输入API密钥和自定义地址
```

📖 **详细配置指南**: 查看 [CONFIGURATION.md](CONFIGURATION.md) 获取完整的配置说明和故障排除指南。

#### 配置文件方式

编辑 `src/main/resources/application.yml`：

```yaml
translation:
  api:
    openai:
      api-key: "your-openai-api-key"
    deepseek:
      api-key: "your-deepseek-api-key"
```

### 编译和运行

1. **克隆项目**
```bash
git clone <repository-url>
cd epub-translator
```

2. **编译项目**
```bash
mvn clean compile
```

3. **运行应用**
```bash
mvn spring-boot:run
```

或者编译为可执行JAR：
```bash
mvn clean package
java -jar target/epub-translator-1.0.0.jar
```

### 使用示例

#### 基本使用

1. **启动应用**后，会显示图形界面
2. **选择EPUB文件**: 点击"浏览..."按钮选择要翻译的EPUB文件
3. **选择翻译服务**: 从下拉框选择OpenAI或DeepSeek
4. **设置输出目录**: 选择翻译后文件的保存位置
5. **开始翻译**: 点击"开始翻译"按钮
6. **查看进度**: 实时查看翻译进度和日志

#### 自定义API配置示例

**步骤1: 配置自定义OpenAI API地址**
```bash
# 使用Azure OpenAI服务
export OPENAI_BASE_URL="https://your-resource.openai.azure.com/openai"
export OPENAI_API_KEY="your-azure-api-key"
export OPENAI_MODEL="gpt-35-turbo"

# 或者使用配置脚本
./setup-custom-api.sh
# 选择选项3，按照提示输入Azure OpenAI配置
```

**步骤2: 验证配置**
```bash
# 验证配置是否正确
./run-example.sh
# 选择选项5: 验证API配置

# 或者使用配置测试工具
mvn compile exec:java -Dexec.mainClass="com.translator.ConfigTestTool" -Dexec.args="validate"
```

**步骤3: 测试翻译服务**
```bash
# 测试OpenAI翻译服务
mvn compile exec:java -Dexec.mainClass="com.translator.ConfigTestTool" -Dexec.args="test-translation openai"

# 测试DeepSeek翻译服务  
mvn compile exec:java -Dexec.mainClass="com.translator.ConfigTestTool" -Dexec.args="test-translation deepseek"
```

**步骤4: 运行完整应用**
```bash
# 启动应用
./run-example.sh
# 选择选项1: 运行完整应用
```

#### 查看当前配置
```bash
# 显示当前所有配置
mvn compile exec:java -Dexec.mainClass="com.translator.ConfigTestTool" -Dexec.args="show-config"
```

## 项目结构

```
epub-translator/
├── src/
│   ├── main/
│   │   ├── java/com/translator/
│   │   │   ├── domain/              # 领域层
│   │   │   │   ├── model/           # 实体
│   │   │   │   ├── valueobject/     # 值对象
│   │   │   │   ├── repository/      # 仓库接口
│   │   │   │   ├── service/         # 领域服务
│   │   │   │   └── exception/       # 异常
│   │   │   ├── application/         # 应用层
│   │   │   │   ├── service/         # 应用服务
│   │   │   │   ├── dto/             # 数据传输对象
│   │   │   │   └── command/         # 命令对象
│   │   │   ├── infrastructure/      # 基础设施层
│   │   │   │   ├── repository/      # 仓库实现
│   │   │   │   ├── translation/     # 翻译服务实现
│   │   │   │   └── config/          # 配置
│   │   │   ├── presentation/        # 表示层
│   │   │   │   ├── gui/             # Swing界面
│   │   │   │   └── controller/      # 控制器
│   │   │   └── EpubTranslatorApplication.java  # 主应用类
│   │   └── resources/
│   │       └── application.yml      # 配置文件
│   └── test/                        # 测试代码
├── pom.xml                          # Maven配置
├── README.md                        # 项目文档
└── brave-new-world.epub            # 示例书籍
```

## 核心功能详解

### 1. EPUB解析与生成

使用EPUBLib库处理EPUB文件：
- 提取书籍元数据（标题、作者、语言等）
- 解析HTML内容页面
- 生成翻译后的EPUB文件
- 自动添加"中文版"到文件名

### 2. 翻译服务集成

支持多种翻译API：
- **OpenAI GPT**: 通过/v1/chat/completions接口
- **DeepSeek**: 兼容OpenAI API格式
- 智能批处理：合并多个页面提高翻译效率
- 重试机制：网络失败时自动重试
- 进度跟踪：实时显示翻译状态

### 3. 异步处理

- 翻译过程在后台线程执行
- 用户界面保持响应
- 支持取消操作
- 实时进度更新

### 4. 错误处理

- 网络错误重试机制
- 文件格式验证
- API响应错误处理
- 用户友好的错误提示

## 开发指南

### 添加新的翻译服务

1. 在 `domain.valueobject.TranslationProvider` 中添加新的提供商
2. 实现 `domain.service.TranslationService` 接口
3. 在 `infrastructure.translation` 包中创建具体实现
4. 更新配置文件和属性类

### 扩展EPUB处理

1. 修改 `EpubProcessingService` 类
2. 处理更多的EPUB特性和元数据
3. 支持更多的HTML元素和样式

### 改进用户界面

1. 在 `presentation.gui` 包中添加新的界面组件
2. 使用 `MainController` 协调界面和业务逻辑
3. 遵循Swing最佳实践

## 测试

运行所有测试：
```bash
mvn test
```

运行特定测试：
```bash
mvn test -Dtest=BookTest
mvn test -Dtest=BookApplicationServiceTest
```

## 故障排除

### 常见问题

#### 1. API密钥无效
**症状**: 翻译失败，提示认证错误
**解决**:
- 检查密钥是否正确
- 确认密钥还有额度
- 检查网络连接
- 使用配置验证工具: `./run-example.sh` 选择选项5

#### 2. 自定义OpenAI Base URL配置问题
**症状**: 无法连接到自定义API服务
**解决**:
- 验证URL格式: 必须以 `http://` 或 `https://` 开头
- 测试网络连接: 
  ```bash
  mvn compile exec:java -Dexec.mainClass="com.translator.ConfigTestTool" -Dexec.args="validate"
  ```
- 检查是否需要特殊的认证头
- 确认API路径格式是否正确（通常是 `/v1/chat/completions`）

**常见自定义API配置**:
```bash
# Azure OpenAI
export OPENAI_BASE_URL="https://your-resource.openai.azure.com/openai"
export OPENAI_API_KEY="your-azure-api-key"
export OPENAI_MODEL="gpt-35-turbo"  # 注意是gpt-35-turbo，不是gpt-3.5-turbo

# 国内代理服务
export OPENAI_BASE_URL="https://api.openai-proxy.org"
export OPENAI_API_KEY="your-proxy-api-key"

# 其他OpenAI兼容服务
export OPENAI_BASE_URL="https://your-custom-api.com/v1"
export OPENAI_API_KEY="your-custom-api-key"
```

#### 3. EPUB文件解析失败
**症状**: 无法读取EPUB文件
**解决**:
- 确认文件格式正确
- 检查文件是否损坏
- 查看日志获取详细信息
- 尝试使用其他EPUB文件

#### 4. 内存不足
**症状**: 翻译大文件时内存溢出
**解决**:
- 增加JVM内存: `export MAVEN_OPTS="-Xmx2g"`
- 减小批处理大小: 修改 `translation-settings.batch-size`
- 分批处理大文件

#### 5. 网络连接问题
**症状**: 连接超时或网络错误
**解决**:
- 检查网络连接
- 确认防火墙设置
- 尝试使用代理服务器
- 增加重试次数和间隔

### 配置调试工具

#### 1. 配置验证工具
```bash
# 验证所有配置
mvn compile exec:java -Dexec.mainClass="com.translator.ConfigTestTool" -Dexec.args="validate"

# 显示当前配置
mvn compile exec:java -Dexec.mainClass="com.translator.ConfigTestTool" -Dexec.args="show-config"

# 测试翻译服务
mvn compile exec:java -Dexec.mainClass="com.translator.ConfigTestTool" -Dexec.args="test-translation openai"
```

#### 2. 配置设置脚本
```bash
# 交互式配置脚本
./setup-custom-api.sh

# 按照提示输入API密钥和自定义地址
```

#### 3. 运行脚本集成测试
```bash
# 运行脚本包含配置验证
./run-example.sh
# 选择选项5: 验证API配置
# 选择选项6: 测试翻译服务
```

### 日志查看

日志文件位于：`logs/epub-translator.log`

或者在控制台查看实时日志。

**调试模式**:
```bash
# 启用调试日志
export LOGGING_LEVEL_COM_TRANSLATOR=DEBUG
./run-example.sh
```

## 贡献指南

1. Fork 项目
2. 创建特性分支: `git checkout -b feature/amazing-feature`
3. 提交更改: `git commit -m 'Add amazing feature'`
4. 推送分支: `git push origin feature/amazing-feature`
5. 创建 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 联系方式

如有问题或建议，请通过以下方式联系：
- 提交 Issue
- 发送邮件
- 创建 Pull Request

---

**享受翻译的乐趣！ 📚🌍**