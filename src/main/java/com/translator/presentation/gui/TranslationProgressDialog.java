package com.translator.presentation.gui;

import com.translator.application.dto.TranslationProgressDto;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * 翻译进度对话框
 */
public class TranslationProgressDialog extends JDialog {
    
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JLabel currentPageLabel;
    private JLabel timeRemainingLabel;
    private JButton cancelButton;
    private JTextArea detailsArea;
    
    public TranslationProgressDialog(JFrame parent, String bookName) {
        super(parent, "翻译进度 - " + bookName, true);
        initializeUI();
        setupEventListeners();
    }
    
    private void initializeUI() {
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // 主面板
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // 状态信息面板
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new GridLayout(3, 1, 5, 5));
        
        statusLabel = new JLabel("正在初始化翻译...");
        statusLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        statusPanel.add(statusLabel);
        
        currentPageLabel = new JLabel("当前页面: 0 / 0");
        statusPanel.add(currentPageLabel);
        
        timeRemainingLabel = new JLabel("预计剩余时间: 计算中...");
        statusPanel.add(timeRemainingLabel);
        
        mainPanel.add(statusPanel);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // 进度条
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(400, 30));
        progressBar.setMaximumSize(new Dimension(400, 30));
        mainPanel.add(progressBar);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // 详细信息区域
        JPanel detailsPanel = new JPanel(new BorderLayout());
        detailsPanel.setBorder(BorderFactory.createTitledBorder("详细信息"));
        
        detailsArea = new JTextArea(8, 40);
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        detailsArea.setBackground(new Color(245, 245, 245));
        
        JScrollPane scrollPane = new JScrollPane(detailsArea);
        detailsPanel.add(scrollPane, BorderLayout.CENTER);
        
        mainPanel.add(detailsPanel);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        cancelButton = new JButton("取消翻译");
        cancelButton.setPreferredSize(new Dimension(100, 30));
        buttonPanel.add(cancelButton);
        
        mainPanel.add(buttonPanel);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // 设置对话框属性
        setSize(500, 400);
        setLocationRelativeTo(getParent());
        setResizable(false);
    }
    
    private void setupEventListeners() {
        cancelButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(
                this,
                "确定要取消翻译吗？\n当前进度将会丢失。",
                "确认取消",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            
            if (result == JOptionPane.YES_OPTION) {
                dispose();
            }
        });
    }
    
    public void updateProgress(TranslationProgressDto progress) {
        SwingUtilities.invokeLater(() -> {
            // 更新进度条
            progressBar.setValue((int) progress.getProgressPercentage());
            progressBar.setString(String.format("%.1f%%", progress.getProgressPercentage()));
            
            // 更新状态标签
            statusLabel.setText("状态: " + progress.getStatus());
            currentPageLabel.setText("当前页面: " + progress.getCurrentPage());
            timeRemainingLabel.setText("预计剩余时间: " + progress.getEstimatedTimeRemaining());
            
            // 更新详细信息
            StringBuilder details = new StringBuilder();
            details.append("书籍: ").append(progress.getBookName()).append("\n");
            details.append("总页数: ").append(progress.getTotalPages()).append("\n");
            details.append("已翻译: ").append(progress.getTranslatedPages()).append("\n");
            details.append("进度: ").append(String.format("%.1f%%", progress.getProgressPercentage())).append("\n");
            details.append("状态: ").append(progress.getStatus()).append("\n");
            details.append("预计剩余时间: ").append(progress.getEstimatedTimeRemaining());
            
            detailsArea.setText(details.toString());
            detailsArea.setCaretPosition(0);
            
            // 如果翻译完成，更新按钮文本
            if (progress.getProgressPercentage() >= 100 || progress.getStatus().contains("完成")) {
                cancelButton.setText("关闭");
                cancelButton.removeActionListener(cancelButton.getActionListeners()[0]);
                cancelButton.addActionListener(e -> dispose());
            }
        });
    }
    
    public void showError(String errorMessage) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("翻译失败");
            statusLabel.setForeground(Color.RED);
            
            StringBuilder details = new StringBuilder(detailsArea.getText());
            details.append("\n\n错误信息:\n");
            details.append(errorMessage);
            
            detailsArea.setText(details.toString());
            detailsArea.setCaretPosition(detailsArea.getDocument().getLength());
            
            cancelButton.setText("关闭");
            cancelButton.removeActionListener(cancelButton.getActionListeners()[0]);
            cancelButton.addActionListener(e -> dispose());
        });
    }
    
    public void addCancelListener(ActionListener listener) {
        cancelButton.addActionListener(listener);
    }
    
    public void setIndeterminate(boolean indeterminate) {
        progressBar.setIndeterminate(indeterminate);
    }
}