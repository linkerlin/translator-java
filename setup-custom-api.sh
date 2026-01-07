#!/bin/bash

# 自定义API配置脚本
# 用于快速配置OpenAI或DeepSeek的自定义API地址

echo "=== EPUB翻译器 - 自定义API配置 ==="
echo ""

# 显示当前配置
echo "当前API配置:"
if [ -n "$OPENAI_API_KEY" ]; then
    echo "✓ OpenAI API密钥: 已配置"
    echo "  OpenAI Base URL: ${OPENAI_BASE_URL:-https://api.openai.com (默认)}"
else
    echo "✗ OpenAI API密钥: 未配置"
fi

echo ""

if [ -n "$DEEPSEEK_API_KEY" ]; then
    echo "✓ DeepSeek API密钥: 已配置"
    echo "  DeepSeek Base URL: ${DEEPSEEK_BASE_URL:-https://api.deepseek.com (默认)}"
else
    echo "✗ DeepSeek API密钥: 未配置"
fi

echo ""
echo "请选择配置选项:"
echo "1. 配置OpenAI自定义API地址"
echo "2. 配置DeepSeek自定义API地址"
echo "3. 配置Azure OpenAI服务"
echo "4. 查看配置示例"
echo "5. 清除所有配置"
echo "6. 退出"
echo ""

read -p "请输入选项 (1-6): " choice

case $choice in
    1)
        echo ""
        echo "配置OpenAI自定义API地址:"
        read -p "请输入API密钥: " api_key
        read -p "请输入Base URL (默认: https://api.openai.com): " base_url
        read -p "请输入模型名称 (默认: gpt-3.5-turbo): " model
        read -p "请输入最大token数 (默认: 2000): " max_tokens
        read -p "请输入温度参数 (默认: 0.3): " temperature
        
        echo ""
        echo "配置信息:"
        echo "API密钥: ${api_key:0:10}..."
        echo "Base URL: ${base_url:-https://api.openai.com}"
        echo "模型: ${model:-gpt-3.5-turbo}"
        echo "Max Tokens: ${max_tokens:-2000}"
        echo "Temperature: ${temperature:-0.3}"
        
        read -p "确认配置？(y/N): " -n 1 -r
        echo
        
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            export OPENAI_API_KEY="$api_key"
            export OPENAI_BASE_URL="${base_url:-https://api.openai.com}"
            export OPENAI_MODEL="${model:-gpt-3.5-turbo}"
            export OPENAI_MAX_TOKENS="${max_tokens:-2000}"
            export OPENAI_TEMPERATURE="${temperature:-0.3}"
            
            echo ""
            echo "✓ OpenAI配置已保存到当前会话"
            echo ""
            echo "要使配置永久生效，请将以下内容添加到 ~/.bashrc 或 ~/.zshrc:"
            echo "export OPENAI_API_KEY=\"$api_key\""
            echo "export OPENAI_BASE_URL=\"${base_url:-https://api.openai.com}\""
            echo "export OPENAI_MODEL=\"${model:-gpt-3.5-turbo}\""
            echo "export OPENAI_MAX_TOKENS=\"${max_tokens:-2000}\""
            echo "export OPENAI_TEMPERATURE=\"${temperature:-0.3}\""
        fi
        ;;
    
    2)
        echo ""
        echo "配置DeepSeek自定义API地址:"
        read -p "请输入API密钥: " api_key
        read -p "请输入Base URL (默认: https://api.deepseek.com): " base_url
        read -p "请输入模型名称 (默认: deepseek-chat): " model
        
        echo ""
        echo "配置信息:"
        echo "API密钥: ${api_key:0:10}..."
        echo "Base URL: ${base_url:-https://api.deepseek.com}"
        echo "模型: ${model:-deepseek-chat}"
        
        read -p "确认配置？(y/N): " -n 1 -r
        echo
        
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            export DEEPSEEK_API_KEY="$api_key"
            export DEEPSEEK_BASE_URL="${base_url:-https://api.deepseek.com}"
            export DEEPSEEK_MODEL="${model:-deepseek-chat}"
            
            echo ""
            echo "✓ DeepSeek配置已保存到当前会话"
            echo ""
            echo "要使配置永久生效，请将以下内容添加到 ~/.bashrc 或 ~/.zshrc:"
            echo "export DEEPSEEK_API_KEY=\"$api_key\""
            echo "export DEEPSEEK_BASE_URL=\"${base_url:-https://api.deepseek.com}\""
            echo "export DEEPSEEK_MODEL=\"${model:-deepseek-chat}\""
        fi
        ;;
    
    3)
        echo ""
        echo "配置Azure OpenAI服务:"
        echo "Azure OpenAI的URL格式通常为: https://your-resource.openai.azure.com/openai"
        read -p "请输入Azure OpenAI密钥: " api_key
        read -p "请输入Azure OpenAI Base URL: " base_url
        read -p "请输入部署名称 (如: gpt-35-turbo): " model
        
        echo ""
        echo "配置信息:"
        echo "API密钥: ${api_key:0:10}..."
        echo "Base URL: $base_url"
        echo "部署名称: $model"
        
        read -p "确认配置？(y/N): " -n 1 -r
        echo
        
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            export OPENAI_API_KEY="$api_key"
            export OPENAI_BASE_URL="$base_url"
            export OPENAI_MODEL="$model"
            export OPENAI_MAX_TOKENS="2000"
            export OPENAI_TEMPERATURE="0.3"
            
            echo ""
            echo "✓ Azure OpenAI配置已保存到当前会话"
            echo ""
            echo "要使配置永久生效，请将以下内容添加到 ~/.bashrc 或 ~/.zshrc:"
            echo "export OPENAI_API_KEY=\"$api_key\""
            echo "export OPENAI_BASE_URL=\"$base_url\""
            echo "export OPENAI_MODEL=\"$model\""
            echo "export OPENAI_MAX_TOKENS=\"2000\""
            echo "export OPENAI_TEMPERATURE=\"0.3\""
        fi
        ;;
    
    4)
        echo ""
        echo "=== 配置示例 ==="
        echo ""
        echo "1. 标准OpenAI配置:"
        echo "export OPENAI_API_KEY=\"your-openai-api-key\""
        echo "export OPENAI_BASE_URL=\"https://api.openai.com\""
        echo ""
        echo "2. Azure OpenAI配置:"
        echo "export OPENAI_API_KEY=\"your-azure-api-key\""
        echo "export OPENAI_BASE_URL=\"https://your-resource.openai.azure.com/openai\""
        echo "export OPENAI_MODEL=\"gpt-35-turbo\""
        echo ""
        echo "3. 国内代理配置:"
        echo "export OPENAI_API_KEY=\"your-proxy-api-key\""
        echo "export OPENAI_BASE_URL=\"https://api.openai-proxy.org\""
        echo ""
        echo "4. 自定义参数配置:"
        echo "export OPENAI_MAX_TOKENS=\"2000\""
        echo "export OPENAI_TEMPERATURE=\"0.3\""
        echo ""
        echo "5. DeepSeek配置:"
        echo "export DEEPSEEK_API_KEY=\"your-deepseek-api-key\""
        echo "export DEEPSEEK_BASE_URL=\"https://api.deepseek.com\""
        ;;
    
    5)
        echo ""
        echo "清除所有配置..."
        unset OPENAI_API_KEY
        unset OPENAI_BASE_URL
        unset OPENAI_MODEL
        unset OPENAI_MAX_TOKENS
        unset OPENAI_TEMPERATURE
        unset DEEPSEEK_API_KEY
        unset DEEPSEEK_BASE_URL
        unset DEEPSEEK_MODEL
        echo "✓ 所有配置已清除"
        ;;
    
    6)
        echo "退出配置脚本"
        exit 0
        ;;
    
    *)
        echo "无效选项！"
        exit 1
        ;;
esac

echo ""
echo "配置完成！您现在可以运行 ./run-example.sh 启动应用"
echo ""
echo "提示: 要使配置永久生效，请将export命令添加到 ~/.bashrc 或 ~/.zshrc"