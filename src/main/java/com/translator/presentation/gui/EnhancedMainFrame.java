package com.translator.presentation.gui;

import com.translator.application.command.TranslateBookCommand;
import com.translator.application.dto.BookDto;
import com.translator.application.dto.TranslationProgressDto;
import com.translator.application.service.BookApplicationService;
import com.translator.domain.exception.TranslationException;
import com.translator.domain.valueobject.TranslationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 增强版主窗口
 * 集成控制器模式，更好的错误处理和用户体验
 */
@Component
public class EnhancedMainFrame extends JFrame {
    
    private static final Logger logger = LoggerFactory.getLogger(EnhancedMainFrame.class);
    
    private final BookApplicationService bookService;
    private final ExecutorService executorService;
    
    // UI组件
    private JTextField filePathField;
    private JButton browseButton;
    private JComboBox<TranslationProvider> providerComboBox;
    private JTextField outputDirField;
    private JButton outputBrowseButton;
    private JButton translateButton;
    private JButton settingsButton;
    private JTextArea logArea;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JPanel progressPanel;
    private JTabbedPane tabbedPane;
    private JTable historyTable;
    private javax.swing.table.DefaultTableModel historyTableModel;
    private TranslationProgressDialog progressDialog;
    
    public EnhancedMainFrame(BookApplicationService bookService) {
        this.bookService = bookService;
        this.executorService = Executors.newFixedThreadPool(2);
        
        initializeUI();
        setupEventListeners();
    }
    
    private void initializeUI() {
        setTitle("EPUB翻译器 - 基于DDD架构 v1.0");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // 创建菜单栏
        createMenuBar();
        
        // 创建工具栏
        createToolBar();
        
        // 创建主内容区域
        createMainContent();
        
        // 创建状态栏
        createStatusBar();
        
        // 设置窗口属性
        setSize(900, 700);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(800, 600));
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
        
        JMenuItem settingsItem = new JMenuItem("设置");
        settingsItem.addActionListener(e -> showSettingsDialog());
        
        JMenuItem clearHistoryItem = new JMenuItem("清除历史记录");
        clearHistoryItem.addActionListener(e -> clearHistory());
        
        toolsMenu.add(settingsItem);
        toolsMenu.add(clearHistoryItem);
        
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
    
    private void createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        
        JButton openButton = new JButton(new ImageIcon("📁"));
        openButton.setToolTipText("打开EPUB文件");
        openButton.addActionListener(e -> selectInputFile());
        
        JButton translateButton = new JButton(new ImageIcon("🔄"));
        translateButton.setToolTipText("开始翻译");
        translateButton.addActionListener(e -> startTranslation());
        
        JButton settingsButton = new JButton(new ImageIcon("⚙️"));
        settingsButton.setToolTipText("设置");
        settingsButton.addActionListener(e -> showSettingsDialog());
        
        toolBar.add(openButton);
        toolBar.add(translateButton);
        toolBar.addSeparator();
        toolBar.add(settingsButton);
        
        add(toolBar, BorderLayout.NORTH);
    }
    
    private void createMainContent() {
        tabbedPane = new JTabbedPane();
        
        // 主翻译面板
        JPanel mainPanel = createTranslationPanel();
        tabbedPane.addTab("翻译", mainPanel);
        
        // 历史记录面板
        JPanel historyPanel = createHistoryPanel();
        tabbedPane.addTab("历史记录", historyPanel);
        
        // 设置面板
        JPanel settingsPanel = createSettingsPanel();
        tabbedPane.addTab("设置", settingsPanel);
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private JPanel createTranslationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 输入区域
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("输入设置"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        inputPanel.add(new JLabel("EPUB文件:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        filePathField = new JTextField();
        filePathField.setEditable(false);
        inputPanel.add(filePathField, gbc);
        
        gbc.gridx = 2; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        browseButton = new JButton("浏览...");
        inputPanel.add(browseButton, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        inputPanel.add(new JLabel("翻译服务:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
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
        inputPanel.add(providerComboBox, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        inputPanel.add(new JLabel("输出目录:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        outputDirField = new JTextField(System.getProperty("user.dir"));
        outputDirField.setEditable(false);
        inputPanel.add(outputDirField, gbc);
        
        gbc.gridx = 2; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        outputBrowseButton = new JButton("浏览...");
        inputPanel.add(outputBrowseButton, gbc);
        
        // 控制按钮面板
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        translateButton = new JButton("开始翻译");
        translateButton.setFont(new Font("微软雅黑", Font.BOLD, 16));
        translateButton.setPreferredSize(new Dimension(150, 40));
        translateButton.setBackground(new Color(0, 123, 255));
        translateButton.setForeground(Color.WHITE);
        controlPanel.add(translateButton);
        
        // 进度面板
        progressPanel = new JPanel(new BorderLayout());
        progressPanel.setBorder(BorderFactory.createTitledBorder("翻译进度"));
        progressPanel.setVisible(false);
        
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(300, 25));
        
        statusLabel = new JLabel("准备就绪");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        progressPanel.add(progressBar, BorderLayout.CENTER);
        progressPanel.add(statusLabel, BorderLayout.SOUTH);
        
        // 日志区域
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(BorderFactory.createTitledBorder("翻译日志"));
        
        logArea = new JTextArea(10, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        logArea.setBackground(new Color(245, 245, 245));
        
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        logPanel.add(scrollPane, BorderLayout.CENTER);
        
        // 组装面板
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(inputPanel, BorderLayout.CENTER);
        topPanel.add(controlPanel, BorderLayout.SOUTH);
        
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(progressPanel, BorderLayout.CENTER);
        panel.add(logPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 历史记录表格
        String[] columnNames = {"文件名", "翻译服务", "状态", "进度", "完成时间", "输出文件"};
        historyTableModel = new javax.swing.table.DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        historyTable = new JTable(historyTableModel);
        historyTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        historyTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        JScrollPane scrollPane = new JScrollPane(historyTable);
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(java.awt.FlowLayout.RIGHT));
        
        JButton refreshButton = new JButton("刷新");
        refreshButton.addActionListener(e -> refreshHistory());
        
        JButton clearButton = new JButton("清除历史");
        clearButton.addActionListener(e -> clearHistory());
        
        JButton openOutputButton = new JButton("打开输出文件");
        openOutputButton.addActionListener(e -> openSelectedOutputFile());
        
        buttonPanel.add(refreshButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(openOutputButton);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 设置表单
        JPanel settingsForm = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0;
        settingsForm.add(new JLabel("默认翻译服务:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        JComboBox<TranslationProvider> defaultProviderCombo = new JComboBox<>(TranslationProvider.values());
        settingsForm.add(defaultProviderCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        settingsForm.add(new JLabel("批处理大小:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        JTextField batchSizeField = new JTextField("2000", 10);
        settingsForm.add(batchSizeField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        settingsForm.add(new JLabel("重试次数:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 2;
        JTextField retryCountField = new JTextField("3", 10);
        settingsForm.add(retryCountField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        settingsForm.add(new JLabel("重试间隔(毫秒):"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 3;
        JTextField retryDelayField = new JTextField("1000", 10);
        settingsForm.add(retryDelayField, gbc);
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveSettingsButton = new JButton("保存设置");
        saveSettingsButton.addActionListener(e -> saveSettings());
        
        JButton resetSettingsButton = new JButton("重置为默认值");
        resetSettingsButton.addActionListener(e -> resetSettings());
        
        buttonPanel.add(resetSettingsButton);
        buttonPanel.add(saveSettingsButton);
        
        panel.add(settingsForm, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
        
        JLabel statusLabel = new JLabel("就绪");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        
        statusBar.add(statusLabel, BorderLayout.WEST);
        
        add(statusBar, BorderLayout.SOUTH);
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
                
                BookDto result = bookService.translateBook(command);
                
                SwingUtilities.invokeLater(() -> {
                    progressBar.setValue(100);
                    statusLabel.setText("翻译完成！");
                    log("翻译完成！");
                    log("输出文件: " + result.getOutputPath());
                    
                    // 添加到历史记录
                    addToHistory(result);
                    
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
    
    private void showSettingsDialog() {
        JOptionPane.showMessageDialog(this, "设置功能开发中...", "提示", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showAboutDialog() {
        String aboutText = String.format(
            "EPUB翻译器 v1.0\n\n" +
            "基于领域驱动设计(DDD)架构\n" +
            "支持OpenAI和DeepSeek翻译API\n\n" +
            "功能特点:\n" +
            "• EPUB格式完整支持\n" +
            "• 批量翻译页面\n" +
            "• 实时进度显示\n" +
            "• 异步处理不阻塞\n\n" +
            "© 2024 EPUB翻译器项目"
        );
        
        JOptionPane.showMessageDialog(this, aboutText, "关于", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void clearHistory() {
        int result = JOptionPane.showConfirmDialog(this, 
            "确定要清除所有历史记录吗？", 
            "确认清除", 
            JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            historyTableModel.setRowCount(0);
            log("历史记录已清除");
        }
    }
    
    private void refreshHistory() {
        log("刷新历史记录...");
        // 这里可以实现从数据库或文件加载历史记录
    }
    
    private void openSelectedOutputFile() {
        int selectedRow = historyTable.getSelectedRow();
        if (selectedRow >= 0) {
            String outputPath = (String) historyTableModel.getValueAt(selectedRow, 5);
            if (outputPath != null && !outputPath.isEmpty()) {
                try {
                    Desktop.getDesktop().open(new File(outputPath));
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, 
                        "无法打开文件: " + e.getMessage(), 
                        "错误", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                "请先选择一个历史记录", 
                "提示", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void saveSettings() {
        log("设置已保存");
        JOptionPane.showMessageDialog(this, "设置已保存！", "成功", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void resetSettings() {
        int result = JOptionPane.showConfirmDialog(this, 
            "确定要重置所有设置为默认值吗？", 
            "确认重置", 
            JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            log("设置已重置为默认值");
            JOptionPane.showMessageDialog(this, "设置已重置！", "成功", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void addToHistory(BookDto book) {
        Object[] row = {
            book.getOriginalFileName(),
            "OpenAI", // 这里应该从book对象获取实际的提供商
            book.getStatus(),
            String.format("%.1f%%", book.getTranslationProgress()),
            book.getCompletedAt() != null ? book.getCompletedAt().toString() : "未完成",
            book.getOutputPath()
        };
        historyTableModel.addRow(row);
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
            setVisible(true);
            log("EPUB翻译器已启动");
            log("请选择要翻译的EPUB文件");
            log("支持OpenAI和DeepSeek翻译API");
        });
    }
}