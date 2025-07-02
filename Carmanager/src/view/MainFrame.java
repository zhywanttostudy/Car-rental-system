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
        setTitle("��������ϵͳ");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // ��������壬ʹ�� BorderLayout ����
        JPanel mainPanel = new JPanel(new BorderLayout());

        // ������ť��壬ʹ�� GridLayout ����
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(3, 1, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        // ��ť
        JButton loginBtn = new JButton("��¼");
        JButton registerBtn = new JButton("ע��");
        JButton exitBtn = new JButton("�˳�ϵͳ");

        // ��ť�¼�
        loginBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // �رյ�ǰ����
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

        // ��Ӱ�ť����ť���
        buttonPanel.add(loginBtn);
        buttonPanel.add(registerBtn);
        buttonPanel.add(exitBtn);

        // ���������ǩ
        JLabel titleLabel = new JLabel("��������ϵͳ", JLabel.CENTER);
        titleLabel.setFont(new Font("����", Font.BOLD, 24));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // ����ͼƬ���
        JPanel imagePanel = new JPanel();

        // ʹ�����������ȡͼƬURL
        URL imgUrl = MainFrame.class.getResource("��½ҳ��.jpg");
        if (imgUrl != null) {
            originalImageIcon = new ImageIcon(imgUrl);
            Image scaledImage = scaleImage(originalImageIcon.getImage(), imagePanel.getWidth(), imagePanel.getHeight());
            ImageIcon scaledImageIcon = new ImageIcon(scaledImage);
            imageLabel = new JLabel(scaledImageIcon);
            imagePanel.add(imageLabel);
        } else {
            System.err.println("����ͼƬδ�ҵ���");
            imagePanel.add(new JLabel("�޷�����ͼƬ")); // ��ʾ������ʾ
        }

        // ����һ���м���壬ʹ�� BorderLayout ���������ð�ť����ͼƬ���
        JPanel middlePanel = new JPanel(new BorderLayout());
        // ��ͼƬ�����ӵ����
        middlePanel.add(imagePanel, BorderLayout.WEST);
        // ����ť�����ӵ��м�
        middlePanel.add(buttonPanel, BorderLayout.CENTER);

        mainPanel.add(middlePanel, BorderLayout.CENTER);

        // ���������ӵ�����
        add(mainPanel);

        // �������ڴ�С�仯�¼�
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

    // ����ͼƬ�ķ���
    private Image scaleImage(Image originalImage, int targetWidth, int targetHeight) {
        // ȷ����Ⱥ͸߶ȷ���
        targetWidth = Math.max(targetWidth, MIN_WIDTH);
        targetHeight = Math.max(targetHeight, MIN_HEIGHT);

        int originalWidth = originalImage.getWidth(null);
        int originalHeight = originalImage.getHeight(null);

        // �������ű���
        double widthRatio = (double) targetWidth / originalWidth;
        double heightRatio = (double) targetHeight / originalHeight;
        double ratio = Math.min(widthRatio, heightRatio);

        // �������ź�ĳߴ�
        int newWidth = (int) (originalWidth * ratio);
        int newHeight = (int) (originalHeight * ratio);

        // ����ͼƬ
        return originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
    }


}