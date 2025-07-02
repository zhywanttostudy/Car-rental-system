package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.URL;

public class MainFrame extends JFrame {
    private JLabel imageLabel;
    private ImageIcon originalImageIcon;
    private static final int MIN_WIDTH = 100;
    private static final int MIN_HEIGHT = 100;

    public MainFrame() {
        setTitle("车辆租赁系统");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 创建主面板，使用 BorderLayout 布局
        JPanel mainPanel = new JPanel(new BorderLayout());

        // 创建按钮面板，使用 GridLayout 布局
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(3, 1, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        // 按钮
        JButton loginBtn = new JButton("登录");
        JButton registerBtn = new JButton("注册");
        JButton exitBtn = new JButton("退出系统");

        // 按钮事件
        loginBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // 关闭当前窗口
                new LoginFrame();
            }
        });

        registerBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                new RegisterFrame();
            }
        });

        exitBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        // 添加按钮到按钮面板
        buttonPanel.add(loginBtn);
        buttonPanel.add(registerBtn);
        buttonPanel.add(exitBtn);

        // 创建标题标签
        JLabel titleLabel = new JLabel("车辆租赁系统", JLabel.CENTER);
        titleLabel.setFont(new Font("宋体", Font.BOLD, 24));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // 创建图片面板
        JPanel imagePanel = new JPanel();

        // 使用类加载器获取图片URL
        URL imgUrl = MainFrame.class.getResource("登陆页面.jpg");
        if (imgUrl != null) {
            originalImageIcon = new ImageIcon(imgUrl);
            Image scaledImage = scaleImage(originalImageIcon.getImage(), imagePanel.getWidth(), imagePanel.getHeight());
            ImageIcon scaledImageIcon = new ImageIcon(scaledImage);
            imageLabel = new JLabel(scaledImageIcon);
            imagePanel.add(imageLabel);
        } else {
            System.err.println("错误：图片未找到！");
            imagePanel.add(new JLabel("无法加载图片")); // 显示错误提示
        }

        // 创建一个中间面板，使用 BorderLayout 布局来放置按钮面板和图片面板
        JPanel middlePanel = new JPanel(new BorderLayout());
        // 将图片面板添加到左边
        middlePanel.add(imagePanel, BorderLayout.WEST);
        // 将按钮面板添加到中间
        middlePanel.add(buttonPanel, BorderLayout.CENTER);

        mainPanel.add(middlePanel, BorderLayout.CENTER);

        // 将主面板添加到窗口
        add(mainPanel);

        // 监听窗口大小变化事件
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (originalImageIcon != null && imageLabel != null) {
                    Image scaledImage = scaleImage(originalImageIcon.getImage(), imagePanel.getWidth(), imagePanel.getHeight());
                    ImageIcon scaledImageIcon = new ImageIcon(scaledImage);
                    imageLabel.setIcon(scaledImageIcon);
                }
            }
        });

        setVisible(true);
    }

    // 缩放图片的方法
    private Image scaleImage(Image originalImage, int targetWidth, int targetHeight) {
        // 确保宽度和高度非零
        targetWidth = Math.max(targetWidth, MIN_WIDTH);
        targetHeight = Math.max(targetHeight, MIN_HEIGHT);

        int originalWidth = originalImage.getWidth(null);
        int originalHeight = originalImage.getHeight(null);

        // 计算缩放比例
        double widthRatio = (double) targetWidth / originalWidth;
        double heightRatio = (double) targetHeight / originalHeight;
        double ratio = Math.min(widthRatio, heightRatio);

        // 计算缩放后的尺寸
        int newWidth = (int) (originalWidth * ratio);
        int newHeight = (int) (originalHeight * ratio);

        // 缩放图片
        return originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
    }


}