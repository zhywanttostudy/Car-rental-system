package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import entity.User;
import service.UserService;
import service.UserServiceImpl;
import java.util.Random;

public class LoginFrame extends JFrame {
    private JTextField userIdField;
    private JPasswordField passwordField;
    private JTextField captchaField; // 验证码输入框
    private JLabel captchaLabel; // 显示验证码的标签
    private String captcha; // 存储生成的验证码
    private UserService userService = new UserServiceImpl();
    private int loginAttempts = 0; // 登录尝试次数
    private static final int MAX_ATTEMPTS = 3; // 最大尝试次数

    public LoginFrame() {
        setTitle("用户登录");
        setSize(350, 300); // 调整窗口大小以适应验证码输入框
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 生成验证码
        captcha = generateCaptcha();

        // 主面板
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 标题
        JLabel titleLabel = new JLabel("欢迎使用系统 - 登录", JLabel.CENTER);
        titleLabel.setFont(new Font("宋体", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);

        // 用户ID标签和输入框
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("用户账号:"), gbc);

        gbc.gridx = 1;
        userIdField = new JTextField(20);
        mainPanel.add(userIdField, gbc);

        // 密码标签和输入框
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("密码:"), gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        mainPanel.add(passwordField, gbc);

        // 验证码标签和输入框
        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(new JLabel("验证码:"), gbc);

        gbc.gridx = 1;
        captchaField = new JTextField(20);
        mainPanel.add(captchaField, gbc);

        // 显示验证码
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        captchaLabel = new JLabel("验证码: " + captcha);
        captchaLabel.setHorizontalAlignment(JLabel.CENTER);
        mainPanel.add(captchaLabel, gbc);

        // 提示标签
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        JLabel tipLabel = new JLabel("登录失败次数: 0 | 最大尝试次数: " + MAX_ATTEMPTS);
        tipLabel.setForeground(Color.RED);
        tipLabel.setHorizontalAlignment(JLabel.CENTER);
        mainPanel.add(tipLabel, gbc);

        // 按钮面板
        JPanel buttonPanel = new JPanel();
        JButton loginBtn = new JButton("登录");
        JButton backBtn = new JButton("返回");

        loginBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin(tipLabel);
            }
        });

        // 支持按Enter键登录
        passwordField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin(tipLabel);
            }
        });

        backBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                new MainFrame();
            }
        });

        buttonPanel.add(loginBtn);
        buttonPanel.add(backBtn);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        mainPanel.add(buttonPanel, gbc);

        add(mainPanel);
        setVisible(true);
    }

    // 生成验证码的方法
    private String generateCaptcha() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder captcha = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            captcha.append(characters.charAt(random.nextInt(characters.length())));
        }
        return captcha.toString();
    }

    private void performLogin(JLabel tipLabel) {
        String userId = userIdField.getText().trim();
        String password = new String(passwordField.getPassword());
        String inputCaptcha = captchaField.getText().trim();

        // 验证验证码
        if (!inputCaptcha.equals(captcha)) {
            JOptionPane.showMessageDialog(LoginFrame.this, 
                                          "验证码输入错误，请重新输入", 
                                          "登录错误", 
                                          JOptionPane.ERROR_MESSAGE);
            captcha = generateCaptcha(); // 重新生成验证码
            captchaLabel.setText("验证码: " + captcha);
            return;
        }

        // 空值验证
        if (userId.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(LoginFrame.this, 
                                          "账号和密码不能为空", 
                                          "登录错误", 
                                          JOptionPane.ERROR_MESSAGE);
            return;
        }

        loginAttempts++;
        tipLabel.setText("登录失败次数: " + loginAttempts + " | 最大尝试次数: " + MAX_ATTEMPTS);

        // 调用登录方法
        User user = userService.login(userId, password);

        if (user != null) {
            // 登录成功
            loginAttempts = 0; // 重置尝试次数
            dispose();
            if (user instanceof entity.Customer) {
                new CustomerFrame((entity.Customer) user);
            } else if (user instanceof entity.Employee) {
                new EmployeeFrame((entity.Employee) user);
            }
        } else {
            // 登录失败
            String message = "账号或密码错误";
            if (loginAttempts >= MAX_ATTEMPTS) {
                message += "\n已达到最大尝试次数，程序将退出";
                JOptionPane.showMessageDialog(LoginFrame.this, 
                                              message, 
                                              "登录失败", 
                                              JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            } else {
                JOptionPane.showMessageDialog(LoginFrame.this, 
                                              message, 
                                              "登录失败", 
                                              JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}