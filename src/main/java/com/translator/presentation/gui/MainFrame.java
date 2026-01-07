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
 * 主窗口
 * Swing GUI的主界面
 */
@Component
public class MainFrame extends JFrame {
    
    private static final Logger logger = LoggerFactory.getLogger(MainFrame.class);
    
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
    
    public MainFrame(BookApplicationService bookService) {
        this.bookService = bookService;
        this.executorService = Executors.newFixedThreadPool(2);
        
        initializeUI();
        setupEventListeners();
    }
    
    private void initializeUI() {
        setTitle("EPUB翻译器 - 基于DDD架构");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // 主面板
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
        
        // 翻译按钮
        translateButton = new JButton("开始翻译");
        translateButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
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
    
    private JPanel createProgressPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("翻译进度"));
        
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(400, 25));
        progressBar.setMaximumSize(new Dimension(400, 25));
        
        statusLabel = new JLabel("准备就绪");
        statusLabel.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        
        panel.add(progressBar);
        panel.add(Box.createVerticalStrut(5));
        panel.add(statusLabel);
        
        return panel;
    }
    
    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("翻译日志"));
        
        logArea = new JTextArea();
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
        
        // 输出目录浏览按钮
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
        });
    }
}