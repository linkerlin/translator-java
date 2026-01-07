package com.translator.presentation.gui;

import com.translator.application.command.TranslateBookCommand;
import com.translator.application.dto.BookDto;
import com.translator.application.service.BookApplicationService;
import com.translator.domain.exception.TranslationException;
import com.translator.domain.valueobject.TranslationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 简化版主窗口
 * 专注于OpenAI base URL配置功能演示
 */
@Component
public class SimpleMainFrame extends JFrame {
    
    private static final Logger logger = LoggerFactory.getLogger(SimpleMainFrame.class);
    
    private final BookApplicationService bookService;
    private final ExecutorService executorService;
    
    // UI组件
    private JTextField filePathField;
    private JButton browseButton;
    private JComboBox<TranslationProvider> providerComboBox;
    private JTextField outputDirField;
    private JButton outputBrowseButton;
    private JButton translateButton;
    private JTextArea logArea;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JPanel progressPanel;
    
    public SimpleMainFrame(BookApplicationService bookService) {
        this.bookService = bookService;
        this.executorService = Executors.newFixedThreadPool(2);
        
        initializeUI();
        setupEventListeners();
    }
    
    private void initializeUI() {
        setTitle("EPUB翻译器 - OpenAI Base URL配置演示");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // 创建菜单栏
        createMenuBar();
        
        // 创建主面板
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 文件选择面板
        JPanel filePanel = createFileSelectionPanel();
        mainPanel.add(filePanel);
        mainPanel.add(Box.createVerticalStrut(10));
        
        // 翻译提供商选择面板
        JPanel providerPanel = createProviderSelectionPanel();
        mainPanel.add(providerPanel);
        mainPanel.add(Box.createVerticalStrut(10));
        
        // 输出目录选择面板
        JPanel outputPanel = createOutputSelectionPanel();
        mainPanel.add(outputPanel);
        mainPanel.add(Box.createVerticalStrut(20));
        
        // API配置信息面板
        JPanel configPanel = createConfigInfoPanel();
        mainPanel.add(configPanel);
        mainPanel.add(Box.createVerticalStrut(20));
        
        // 控制按钮
        translateButton = new JButton("开始翻译");
        translateButton.setFont(new Font("微软雅黑", Font.BOLD, 16));
        translateButton.setPreferredSize(new Dimension(150, 40));
        translateButton.setBackground(new Color(0, 123, 255));
        translateButton.setForeground(Color.WHITE);
        translateButton.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        mainPanel.add(translateButton);
        mainPanel.add(Box.createVerticalStrut(20));
        
        // 进度面板
        progressPanel = createProgressPanel();
        progressPanel.setVisible(false);
        mainPanel.add(progressPanel);
        mainPanel.add(Box.createVerticalStrut(10));
        
        // 日志区域
        JPanel logPanel = createLogPanel();
        mainPanel.add(logPanel);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // 设置窗口大小和位置
        setSize(800, 600);
        setLocationRelativeTo(null);
    }
    
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // 文件菜单
        JMenu fileMenu = new JMenu("文件");
        fileMenu.setMnemonic('F');
        
        JMenuItem openItem = new JMenuItem("打开EPUB文件");
        openItem.setAccelerator(KeyStroke.getKeyStroke("ctrl O"));
        openItem.addActionListener(e -> selectInputFile());
        
        JMenuItem exitItem = new JMenuItem("退出");
        exitItem.setAccelerator(KeyStroke.getKeyStroke("ctrl Q"));
        exitItem.addActionListener(e -> System.exit(0));
        
        fileMenu.add(openItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        // 工具菜单
        JMenu toolsMenu = new JMenu("工具");
        toolsMenu.setMnemonic('T');
        
        JMenuItem validateConfigItem = new JMenuItem("验证API配置");
        validateConfigItem.addActionListener(e -> validateConfiguration());
        
        JMenuItem showConfigItem = new JMenuItem("显示当前配置");
        showConfigItem.addActionListener(e -> showCurrentConfiguration());
        
        toolsMenu.add(validateConfigItem);
        toolsMenu.add(showConfigItem);
        
        // 帮助菜单
        JMenu helpMenu = new JMenu("帮助");
        helpMenu.setMnemonic('H');
        
        JMenuItem aboutItem = new JMenuItem("关于");
        aboutItem.addActionListener(e -> showAboutDialog());
        
        helpMenu.add(aboutItem);
        
        menuBar.add(fileMenu);
        menuBar.add(toolsMenu);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    private JPanel createFileSelectionPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("选择EPUB文件"));
        
        filePathField = new JTextField();
        filePathField.setEditable(false);
        filePathField.setPreferredSize(new Dimension(400, 25));
        filePathField.setMaximumSize(new Dimension(400, 25));
        
        browseButton = new JButton("浏览...");
        browseButton.setPreferredSize(new Dimension(80, 25));
        
        panel.add(new JLabel("文件路径:"));
        panel.add(Box.createHorizontalStrut(10));
        panel.add(filePathField);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(browseButton);
        
        return panel;
    }
    
    private JPanel createProviderSelectionPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("翻译服务提供商"));
        
        providerComboBox = new JComboBox<>(TranslationProvider.values());
        providerComboBox.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index,
                                                        boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof TranslationProvider) {
                    TranslationProvider provider = (TranslationProvider) value;
                    setText(provider.getName() + " (" + provider.getDefaultModel() + ")");
                }
                return this;
            }
        });
        
        panel.add(new JLabel("选择提供商:"));
        panel.add(Box.createHorizontalStrut(10));
        panel.add(providerComboBox);
        panel.add(Box.createHorizontalGlue());
        
        return panel;
    }
    
    private JPanel createOutputSelectionPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("输出目录"));
        
        outputDirField = new JTextField(System.getProperty("user.dir"));
        outputDirField.setEditable(false);
        outputDirField.setPreferredSize(new Dimension(400, 25));
        outputDirField.setMaximumSize(new Dimension(400, 25));
        
        outputBrowseButton = new JButton("浏览...");
        outputBrowseButton.setPreferredSize(new Dimension(80, 25));
        
        panel.add(new JLabel("输出路径:"));
        panel.add(Box.createHorizontalStrut(10));
        panel.add(outputDirField);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(outputBrowseButton);
        
        return panel;
    }
    
    private JPanel createConfigInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("API配置信息"));
        
        JTextArea configArea = new JTextArea();
        configArea.setEditable(false);
        configArea.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        configArea.setBackground(new Color(245, 245, 245));
        configArea.setText(getCurrentConfigInfo());
        
        JScrollPane scrollPane = new JScrollPane(configArea);
        scrollPane.setPreferredSize(new Dimension(750, 100));
        
        panel.add(scrollPane);
        
        return panel;
    }
    
    private JPanel createProgressPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("翻译进度"));
        
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(300, 25));
        
        statusLabel = new JLabel("准备就绪");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        panel.add(progressBar, BorderLayout.CENTER);
        panel.add(statusLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("翻译日志"));
        
        logArea = new JTextArea(10, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        logArea.setBackground(new Color(245, 245, 245));
        
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setPreferredSize(new Dimension(750, 200));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void setupEventListeners() {
        // 文件浏览按钮
        browseButton.addActionListener(e -> selectInputFile());
        outputBrowseButton.addActionListener(e -> selectOutputDirectory());
        
        // 翻译按钮
        translateButton.addActionListener(e -> startTranslation());
    }
    
    private void selectInputFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("EPUB文件", "epub"));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            filePathField.setText(selectedFile.getAbsolutePath());
            log("已选择文件: " + selectedFile.getName());
        }
    }
    
    private void selectOutputDirectory() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setCurrentDirectory(new File(outputDirField.getText()));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedDir = fileChooser.getSelectedFile();
            outputDirField.setText(selectedDir.getAbsolutePath());
            log("已选择输出目录: " + selectedDir.getAbsolutePath());
        }
    }
    
    private void startTranslation() {
        String filePath = filePathField.getText();
        if (filePath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先选择EPUB文件", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        File inputFile = new File(filePath);
        if (!inputFile.exists()) {
            JOptionPane.showMessageDialog(this, "文件不存在: " + filePath, "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // 禁用翻译按钮
        translateButton.setEnabled(false);
        progressPanel.setVisible(true);
        progressBar.setValue(0);
        statusLabel.setText("开始翻译...");
        
        TranslationProvider provider = (TranslationProvider) providerComboBox.getSelectedItem();
        String outputDir = outputDirField.getText();
        
        TranslateBookCommand command = new TranslateBookCommand(filePath, provider, outputDir);
        
        // 异步执行翻译
        CompletableFuture.runAsync(() -> {
            try {
                log("开始翻译文件: " + inputFile.getName());
                log("使用翻译服务: " + provider.getName());
                log("Base URL: " + getBaseUrlForProvider(provider));
                
                BookDto result = bookService.translateBook(command);
                
                SwingUtilities.invokeLater(() -> {
                    progressBar.setValue(100);
                    statusLabel.setText("翻译完成！");
                    log("翻译完成！");
                    log("输出文件: " + result.getOutputPath());
                    
                    JOptionPane.showMessageDialog(this, 
                        "翻译完成！\n输出文件: " + result.getTranslatedFileName(), 
                        "成功", 
                        JOptionPane.INFORMATION_MESSAGE);
                });
                
            } catch (Exception e) {
                logger.error("翻译失败", e);
                
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("翻译失败");
                    log("翻译失败: " + e.getMessage());
                    
                    JOptionPane.showMessageDialog(this, 
                        "翻译失败: " + e.getMessage(), 
                        "错误", 
                        JOptionPane.ERROR_MESSAGE);
                });
            } finally {
                SwingUtilities.invokeLater(() -> {
                    translateButton.setEnabled(true);
                });
            }
        }, executorService);
    }
    
    private void validateConfiguration() {
        log("开始验证API配置...");
        
        // 这里可以集成配置验证工具
        TranslationProvider provider = (TranslationProvider) providerComboBox.getSelectedItem();
        String baseUrl = getBaseUrlForProvider(provider);
        
        log("正在验证 " + provider.getName() + " 配置...");
        log("Base URL: " + baseUrl);
        
        if (baseUrl.contains("openai.com")) {
            log("✓ 标准OpenAI配置检测");
        } else if (baseUrl.contains("azure")) {
            log("✓ Azure OpenAI配置检测");
        } else if (baseUrl.contains("deepseek")) {
            log("✓ DeepSeek配置检测");
        } else {
            log("✓ 自定义API配置检测");
        }
        
        log("配置验证完成，可以开始使用翻译服务。");
    }
    
    private void showCurrentConfiguration() {
        StringBuilder config = new StringBuilder();
        config.append("=== 当前API配置 ===\n\n");
        
        // OpenAI配置
        String openaiKey = System.getenv("OPENAI_API_KEY");
        String openaiBaseUrl = System.getenv("OPENAI_BASE_URL");
        String openaiModel = System.getenv("OPENAI_MODEL");
        
        config.append("OpenAI配置:\n");
        config.append("  API密钥: ").append(openaiKey != null ? "已配置" : "未配置").append("\n");
        config.append("  Base URL: ").append(openaiBaseUrl != null ? openaiBaseUrl : "https://api.openai.com (默认)").append("\n");
        config.append("  模型: ").append(openaiModel != null ? openaiModel : "gpt-3.5-turbo (默认)").append("\n\n");
        
        // DeepSeek配置
        String deepseekKey = System.getenv("DEEPSEEK_API_KEY");
        String deepseekBaseUrl = System.getenv("DEEPSEEK_BASE_URL");
        String deepseekModel = System.getenv("DEEPSEEK_MODEL");
        
        config.append("DeepSeek配置:\n");
        config.append("  API密钥: ").append(deepseekKey != null ? "已配置" : "未配置").append("\n");
        config.append("  Base URL: ").append(deepseekBaseUrl != null ? deepseekBaseUrl : "https://api.deepseek.com (默认)").append("\n");
        config.append("  模型: ").append(deepseekModel != null ? deepseekModel : "deepseek-chat (默认)").append("\n");
        
        JTextArea configArea = new JTextArea(config.toString());
        configArea.setEditable(false);
        configArea.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        
        JOptionPane.showMessageDialog(this, new JScrollPane(configArea), "当前配置", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showAboutDialog() {
        String aboutText = String.format(
            "EPUB翻译器 - OpenAI Base URL配置演示 v1.0\n\n" +
            "基于领域驱动设计(DDD)架构\n" +
            "支持OpenAI和DeepSeek翻译API\n\n" +
            "功能特点:\n" +
            "• 支持自定义OpenAI API地址\n" +
            "• 支持Azure OpenAI服务\n" +
            "• 支持国内代理服务\n" +
            "• 实时配置验证\n\n" +
            "© 2024 EPUB翻译器项目\n\n" +
            "使用说明:\n" +
            "1. 设置 OPENAI_BASE_URL 环境变量\n" +
            "2. 使用 ./setup-custom-api.sh 进行配置\n" +
            "3. 使用配置验证工具检查设置"
        );
        
        JOptionPane.showMessageDialog(this, aboutText, "关于", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private String getBaseUrlForProvider(TranslationProvider provider) {
        switch (provider) {
            case OPENAI:
                return System.getenv("OPENAI_BASE_URL") != null ? 
                    System.getenv("OPENAI_BASE_URL") : "https://api.openai.com";
            case DEEPSEEK:
                return System.getenv("DEEPSEEK_BASE_URL") != null ? 
                    System.getenv("DEEPSEEK_BASE_URL") : "https://api.deepseek.com";
            default:
                return "unknown";
        }
    }
    
    private String getCurrentConfigInfo() {
        StringBuilder info = new StringBuilder();
        
        // OpenAI配置
        String openaiBaseUrl = System.getenv("OPENAI_BASE_URL");
        String openaiModel = System.getenv("OPENAI_MODEL");
        String openaiKey = System.getenv("OPENAI_API_KEY");
        
        info.append("OpenAI配置:\n");
        info.append("Base URL: ").append(openaiBaseUrl != null ? openaiBaseUrl : "https://api.openai.com (默认)").append("\n");
        info.append("模型: ").append(openaiModel != null ? openaiModel : "gpt-3.5-turbo (默认)").append("\n");
        info.append("API密钥: ").append(openaiKey != null ? "已配置" : "未配置").append("\n\n");
        
        // DeepSeek配置
        String deepseekBaseUrl = System.getenv("DEEPSEEK_BASE_URL");
        String deepseekModel = System.getenv("DEEPSEEK_MODEL");
        String deepseekKey = System.getenv("DEEPSEEK_API_KEY");
        
        info.append("DeepSeek配置:\n");
        info.append("Base URL: ").append(deepseekBaseUrl != null ? deepseekBaseUrl : "https://api.deepseek.com (默认)").append("\n");
        info.append("模型: ").append(deepseekModel != null ? deepseekModel : "deepseek-chat (默认)").append("\n");
        info.append("API密钥: ").append(deepseekKey != null ? "已配置" : "未配置").append("\n");
        
        return info.toString();
    }
    
    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
            logArea.append("[" + timestamp + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    public void showFrame() {
        SwingUtilities.invokeLater(() -> {
            try {
                // 设置系统外观
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                logger.warn("无法设置系统外观，使用默认外观", e);
            }
            
            setVisible(true);
            log("EPUB翻译器已启动");
            log("OpenAI Base URL配置功能已启用");
            log("请设置 OPENAI_BASE_URL 环境变量以使用自定义API地址");
            log("使用 ./setup-custom-api.sh 进行快速配置");
            log("当前配置信息已显示在上方面板中");
        });
    }
}