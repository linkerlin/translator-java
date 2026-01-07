#!/bin/bash

# EPUB翻译器运行脚本
# 用于快速启动和测试项目

echo "=== EPUB翻译器运行脚本 ==="
echo "当前目录: $(pwd)"

# 检查Java版本
echo "检查Java版本..."
java -version
if [ $? -ne 0 ]; then
    echo "错误: 未安装Java或Java不在PATH中"
    exit 1
fi

# 检查Maven
if command -v mvn &> /dev/null; then
    echo "Maven已安装"
    MVN_CMD="mvn"
else
    echo "Maven未安装，尝试使用Maven Wrapper..."
    if [ -f "mvnw" ]; then
        MVN_CMD="./mvnw"
    else
        echo "错误: 未安装Maven且找不到Maven Wrapper"
        echo "请安装Maven或下载Maven Wrapper"
        exit 1
    fi
fi

# 检查示例文件
if [ -f "brave-new-world.epub" ]; then
    echo "找到示例文件: brave-new-world.epub ($(du -h brave-new-world.epub | cut -f1))"
else
    echo "警告: 未找到示例文件 brave-new-world.epub"
fi

# 检查API密钥配置
echo "检查API密钥配置..."
if [ -z "$OPENAI_API_KEY" ] && [ -z "$DEEPSEEK_API_KEY" ]; then
    echo "警告: 未设置API密钥"
    echo "请设置环境变量:"
    echo "  export OPENAI_API_KEY='your-openai-api-key'"
    echo "  export OPENAI_BASE_URL='https://api.openai.com'  # 可选，自定义API地址"
    echo "  export DEEPSEEK_API_KEY='your-deepseek-api-key'"
    echo "  export DEEPSEEK_BASE_URL='https://api.deepseek.com'  # 可选，自定义API地址"
    echo ""
    read -p "是否继续运行？(y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
else
    if [ -n "$OPENAI_API_KEY" ]; then
        echo "✓ OpenAI API密钥已配置"
        if [ -n "$OPENAI_BASE_URL" ]; then
            echo "✓ OpenAI Base URL: $OPENAI_BASE_URL"
        else
            echo "  OpenAI Base URL: 使用默认值 (https://api.openai.com)"
        fi
    fi
    if [ -n "$DEEPSEEK_API_KEY" ]; then
        echo "✓ DeepSeek API密钥已配置"
        if [ -n "$DEEPSEEK_BASE_URL" ]; then
            echo "✓ DeepSeek Base URL: $DEEPSEEK_BASE_URL"
        else
            echo "  DeepSeek Base URL: 使用默认值 (https://api.deepseek.com)"
        fi
    fi
fi

# 清理和编译
echo "清理和编译项目..."
$MVN_CMD clean compile
if [ $? -ne 0 ]; then
    echo "编译失败！"
    exit 1
fi

# 运行选项
echo ""
echo "选择运行方式:"
echo "1. 运行完整应用 (推荐)"
echo "2. 快速测试"
echo "3. 编译打包"
echo "4. 运行测试"
echo "5. 验证API配置"
echo "6. 测试翻译服务"
echo ""
read -p "请输入选项 (1-6): " choice

case $choice in
    1)
        echo "启动完整应用..."
        $MVN_CMD spring-boot:run
        ;;
    2)
        echo "运行快速测试..."
        $MVN_CMD exec:java -Dexec.mainClass="com.translator.Runner" -Dexec.args="quickTest"
        ;;
    3)
        echo "编译打包..."
        $MVN_CMD clean package
        if [ $? -eq 0 ]; then
            echo "打包成功！"
            echo "可执行JAR文件: target/epub-translator-1.0.0.jar"
            echo ""
            read -p "是否立即运行打包的JAR？(y/N): " -n 1 -r
            echo
            if [[ $REPLY =~ ^[Yy]$ ]]; then
                java -jar target/epub-translator-1.0.0.jar
            fi
        fi
        ;;
    4)
        echo "运行测试..."
        $MVN_CMD test
        ;;
    5)
        echo "验证API配置..."
        $MVN_CMD compile exec:java -Dexec.mainClass="com.translator.ConfigTestTool" -Dexec.args="validate"
        ;;
    6)
        echo "选择翻译服务进行测试:"
        echo "1. OpenAI"
        echo "2. DeepSeek"
        read -p "请输入选项 (1-2): " test_choice
        
        case $test_choice in
            1)
                echo "测试OpenAI翻译服务..."
                $MVN_CMD compile exec:java -Dexec.mainClass="com.translator.ConfigTestTool" -Dexec.args="test-translation openai"
                ;;
            2)
                echo "测试DeepSeek翻译服务..."
                $MVN_CMD compile exec:java -Dexec.mainClass="com.translator.ConfigTestTool" -Dexec.args="test-translation deepseek"
                ;;
            *)
                echo "无效选项！"
                exit 1
                ;;
        esac
        ;;
    *)
        echo "无效选项！"
        exit 1
        ;;
esac

echo "脚本执行完成！"