package com.translator.e2e;

/**
 * 端到端测试套件说明
 * 
 * 本测试套件包含以下端到端测试：
 * 
 * 1. StandardOpenAIE2ETest - 标准OpenAI API测试
 *    - 测试标准OpenAI配置的完整翻译流程
 *    - 验证自定义Base URL配置
 *    - 测试配置验证功能
 * 
 * 2. AzureOpenAIE2ETest - Azure OpenAI测试  
 *    - 测试Azure OpenAI服务的完整翻译流程
 *    - 验证Azure特定的配置格式
 *    - 测试Azure配置检测和验证
 * 
 * 3. ConfigurationValidationE2ETest - 配置验证测试
 *    - 测试完整配置验证流程
 *    - 验证不同Base URL格式的处理
 *    - 测试配置错误处理和网络连接验证
 * 
 * 4. CompleteTranslationE2ETest - 完整翻译流程测试
 *    - 测试从文件选择到翻译完成的完整工作流程
 *    - 验证不同Base URL的翻译流程
 *    - 测试翻译错误处理和进度跟踪
 * 
 * 5. PerformanceE2ETest - 性能测试
 *    - 测试翻译性能和速度
 *    - 验证批处理效率和内存使用
 *    - 测试并发翻译性能
 * 
 * 运行要求：
 * - 需要设置相应的API密钥环境变量
 * - 标准测试：OPENAI_API_KEY
 * - Azure测试：AZURE_OPENAI_API_KEY
 * - 其他测试会在没有API密钥时跳过实际的API调用
 * 
 * 运行所有测试:
 * mvn test -Dtest=EndToEndTestSuite
 * 
 * 运行单个测试:
 * mvn test -Dtest=StandardOpenAIE2ETest
 * mvn test -Dtest=ConfigurationValidationE2ETest
 * 等等...
 */
public class EndToEndTestSuite {
    // 这个类仅用于文档说明，实际的测试运行通过Maven Surefire插件完成
}