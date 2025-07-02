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
    private JTextField captchaField; // ��֤�������
    private JLabel captchaLabel; // ��ʾ��֤��ı�ǩ
    private String captcha; // �洢���ɵ���֤��
    private UserService userService = new UserServiceImpl();
    private int loginAttempts = 0; // ��¼���Դ���
    private static final int MAX_ATTEMPTS = 3; // ����Դ���

    public LoginFrame() {
        setTitle("�û���¼");
        setSize(350, 300); // �������ڴ�С����Ӧ��֤�������
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // ������֤��
        captcha = generateCaptcha();

        // �����
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // ����
        JLabel titleLabel = new JLabel("��ӭʹ��ϵͳ - ��¼", JLabel.CENTER);
        titleLabel.setFont(new Font("����", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);

        // �û�ID��ǩ�������
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("�û��˺�:"), gbc);

        gbc.gridx = 1;
        userIdField = new JTextField(20);
        mainPanel.add(userIdField, gbc);

        // �����ǩ�������
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("����:"), gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        mainPanel.add(passwordField, gbc);

        // ��֤���ǩ�������
        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(new JLabel("��֤��:"), gbc);

        gbc.gridx = 1;
        captchaField = new JTextField(20);
        mainPanel.add(captchaField, gbc);

        // ��ʾ��֤��
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        captchaLabel = new JLabel("��֤��: " + captcha);
        captchaLabel.setHorizontalAlignment(JLabel.CENTER);
        mainPanel.add(captchaLabel, gbc);

        // ��ʾ��ǩ
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        JLabel tipLabel = new JLabel("��¼ʧ�ܴ���: 0 | ����Դ���: " + MAX_ATTEMPTS);
        tipLabel.setForeground(Color.RED);
        tipLabel.setHorizontalAlignment(JLabel.CENTER);
        mainPanel.add(tipLabel, gbc);

        // ��ť���
        JPanel buttonPanel = new JPanel();
        JButton loginBtn = new JButton("��¼");
        JButton backBtn = new JButton("����");

        loginBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin(tipLabel);
            }
        });

        // ֧�ְ�Enter����¼
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

    // ������֤��ķ���
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

        // ��֤��֤��
        if (!inputCaptcha.equals(captcha)) {
            JOptionPane.showMessageDialog(LoginFrame.this, 
                                          "��֤�������������������", 
                                          "��¼����", 
                                          JOptionPane.ERROR_MESSAGE);
            captcha = generateCaptcha(); // ����������֤��
            captchaLabel.setText("��֤��: " + captcha);
            return;
        }

        // ��ֵ��֤
        if (userId.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(LoginFrame.this, 
                                          "�˺ź����벻��Ϊ��", 
                                          "��¼����", 
                                          JOptionPane.ERROR_MESSAGE);
            return;
        }

        loginAttempts++;
        tipLabel.setText("��¼ʧ�ܴ���: " + loginAttempts + " | ����Դ���: " + MAX_ATTEMPTS);

        // ���õ�¼����
        User user = userService.login(userId, password);

        if (user != null) {
            // ��¼�ɹ�
            loginAttempts = 0; // ���ó��Դ���
            dispose();
            if (user instanceof entity.Customer) {
                new CustomerFrame((entity.Customer) user);
            } else if (user instanceof entity.Employee) {
                new EmployeeFrame((entity.Employee) user);
            }
        } else {
            // ��¼ʧ��
            String message = "�˺Ż��������";
            if (loginAttempts >= MAX_ATTEMPTS) {
                message += "\n�Ѵﵽ����Դ����������˳�";
                JOptionPane.showMessageDialog(LoginFrame.this, 
                                              message, 
                                              "��¼ʧ��", 
                                              JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            } else {
                JOptionPane.showMessageDialog(LoginFrame.this, 
                                              message, 
                                              "��¼ʧ��", 
                                              JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}