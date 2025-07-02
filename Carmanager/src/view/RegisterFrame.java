package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import service.UserService;
import service.UserServiceImpl;
import javax.swing.JPasswordField;

public class RegisterFrame extends JFrame {
    private JTextField cnoField, cnameField, cageField, cphoneField;
    private JPasswordField cpassField;
    private JComboBox<String> csexComboBox;
    private JComboBox<String> caddressComboBox;
    private UserService userService = new UserServiceImpl();

    // �й� 34 ��ʡ��������
    private static final String[] PROVINCES = {
            "������", "�����", "�Ϻ���", "������",
            "�ӱ�ʡ", "ɽ��ʡ", "����ʡ", "����ʡ",
            "������ʡ", "����ʡ", "�㽭ʡ", "����ʡ",
            "����ʡ", "����ʡ", "ɽ��ʡ", "����ʡ",
            "����ʡ", "����ʡ", "�㶫ʡ", "����ʡ",
            "�Ĵ�ʡ", "����ʡ", "����ʡ", "����ʡ",
            "����ʡ", "�ຣʡ", "̨��ʡ", "���ɹ�������",
            "����׳��������", "����������", "���Ļ���������",
            "�½�ά���������", "����ر�������", "�����ر�������"
    };

    public RegisterFrame() {
        setTitle("�ͻ�ע��");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // �����
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // ����
        JLabel titleLabel = new JLabel("�ͻ�ע��", JLabel.CENTER);
        titleLabel.setFont(new Font("����", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);

        // ע���
        gbc.gridwidth = 1;
        addFormField(mainPanel, gbc, "�ͻ���ţ�", 1, cnoField = new JTextField(20));
        addFormField(mainPanel, gbc, "�ͻ�������", 2, cnameField = new JTextField(20));

        // �Ա�ѡ���
        String[] genders = {"��", "Ů"};
        csexComboBox = new JComboBox<>(genders);
        addFormField(mainPanel, gbc, "�ͻ��Ա�", 3, csexComboBox);

        addFormField(mainPanel, gbc, "�ͻ����䣺", 4, cageField = new JTextField(20));
        addFormField(mainPanel, gbc, "��ϵ�绰��", 5, cphoneField = new JTextField(20));

        // ��ַѡ���
        caddressComboBox = new JComboBox<>(PROVINCES);
        addFormField(mainPanel, gbc, "�ͻ���ַ��", 6, caddressComboBox);

        addFormField(mainPanel, gbc, "�ͻ����룺", 7, cpassField = new JPasswordField(20));

        // ��ť���
        JPanel buttonPanel = new JPanel();
        JButton registerBtn = new JButton("ע��");
        JButton backBtn = new JButton("����");

        registerBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String cno = cnoField.getText().trim();
                String cname = cnameField.getText().trim();
                String csex = (String) csexComboBox.getSelectedItem();
                String cageStr = cageField.getText().trim();
                String cphone = cphoneField.getText().trim();
                String caddress = (String) caddressComboBox.getSelectedItem();
                String cpass = new String(((JPasswordField) cpassField).getPassword());

                // ������֤
                if (cno.isEmpty() || cname.isEmpty() || cpass.isEmpty()) {
                    showRegistrationResult(false, "�ͻ���š��ͻ����������벻��Ϊ��");
                    return;
                }

                if (cageStr.isEmpty() || !isNumeric(cageStr)) {
                    showRegistrationResult(false, "�������Ϊ����");
                    return;
                }

                // ��֤��ϵ�绰�Ƿ�Ϊ 11 λ
                if (cphone.length() != 11 || !cphone.matches("\\d+")) {
                    showRegistrationResult(false, "������ 11 λ��Ч����");
                    return;
                }

                int cage = Integer.parseInt(cageStr);
                boolean result = userService.registerCustomer(cno, cname, csex, cage, cphone, caddress, cpass);
                showRegistrationResult(result, result ? "ע��ɹ���" : "ע��ʧ�ܣ��ͻ���ſ����Ѵ���");

                if (result) {
                    dispose(); // �رյ�ǰע�ᴰ��
                    new LoginFrame(); // �򿪵�¼����
                }
            }
        });

        backBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                new MainFrame();
            }
        });

        buttonPanel.add(registerBtn);
        buttonPanel.add(backBtn);

        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        mainPanel.add(buttonPanel, gbc);

        add(mainPanel);
        setVisible(true);
    }

    // ������������ӱ��ֶ�
    private void addFormField(JPanel panel, GridBagConstraints gbc,
                              String labelText, int y, JComponent component) {
        gbc.gridx = 0;
        gbc.gridy = y;
        panel.add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        panel.add(component, gbc);
    }

    // ������������֤�Ƿ�Ϊ����
    private boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // ������������ʾע����
    private void showRegistrationResult(boolean success, String message) {
        String title = success ? "ע��ɹ�" : "ע��ʧ��";
        int messageType = success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE;
        JOptionPane.showMessageDialog(RegisterFrame.this, message, title, messageType);
    }
}