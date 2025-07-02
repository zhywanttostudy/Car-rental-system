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

    // 中国 34 个省级行政区
    private static final String[] PROVINCES = {
            "北京市", "天津市", "上海市", "重庆市",
            "河北省", "山西省", "辽宁省", "吉林省",
            "黑龙江省", "江苏省", "浙江省", "安徽省",
            "福建省", "江西省", "山东省", "河南省",
            "湖北省", "湖南省", "广东省", "海南省",
            "四川省", "贵州省", "云南省", "陕西省",
            "甘肃省", "青海省", "台湾省", "内蒙古自治区",
            "广西壮族自治区", "西藏自治区", "宁夏回族自治区",
            "新疆维吾尔自治区", "香港特别行政区", "澳门特别行政区"
    };

    public RegisterFrame() {
        setTitle("客户注册");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 主面板
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 标题
        JLabel titleLabel = new JLabel("客户注册", JLabel.CENTER);
        titleLabel.setFont(new Font("宋体", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);

        // 注册表单
        gbc.gridwidth = 1;
        addFormField(mainPanel, gbc, "客户编号：", 1, cnoField = new JTextField(20));
        addFormField(mainPanel, gbc, "客户姓名：", 2, cnameField = new JTextField(20));

        // 性别选择框
        String[] genders = {"男", "女"};
        csexComboBox = new JComboBox<>(genders);
        addFormField(mainPanel, gbc, "客户性别：", 3, csexComboBox);

        addFormField(mainPanel, gbc, "客户年龄：", 4, cageField = new JTextField(20));
        addFormField(mainPanel, gbc, "联系电话：", 5, cphoneField = new JTextField(20));

        // 地址选择框
        caddressComboBox = new JComboBox<>(PROVINCES);
        addFormField(mainPanel, gbc, "客户地址：", 6, caddressComboBox);

        addFormField(mainPanel, gbc, "客户密码：", 7, cpassField = new JPasswordField(20));

        // 按钮面板
        JPanel buttonPanel = new JPanel();
        JButton registerBtn = new JButton("注册");
        JButton backBtn = new JButton("返回");

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

                // 输入验证
                if (cno.isEmpty() || cname.isEmpty() || cpass.isEmpty()) {
                    showRegistrationResult(false, "客户编号、客户姓名和密码不能为空");
                    return;
                }

                if (cageStr.isEmpty() || !isNumeric(cageStr)) {
                    showRegistrationResult(false, "年龄必须为数字");
                    return;
                }

                // 验证联系电话是否为 11 位
                if (cphone.length() != 11 || !cphone.matches("\\d+")) {
                    showRegistrationResult(false, "请输入 11 位有效号码");
                    return;
                }

                int cage = Integer.parseInt(cageStr);
                boolean result = userService.registerCustomer(cno, cname, csex, cage, cphone, caddress, cpass);
                showRegistrationResult(result, result ? "注册成功！" : "注册失败，客户编号可能已存在");

                if (result) {
                    dispose(); // 关闭当前注册窗口
                    new LoginFrame(); // 打开登录窗口
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

    // 辅助方法：添加表单字段
    private void addFormField(JPanel panel, GridBagConstraints gbc,
                              String labelText, int y, JComponent component) {
        gbc.gridx = 0;
        gbc.gridy = y;
        panel.add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        panel.add(component, gbc);
    }

    // 辅助方法：验证是否为数字
    private boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // 辅助方法：显示注册结果
    private void showRegistrationResult(boolean success, String message) {
        String title = success ? "注册成功" : "注册失败";
        int messageType = success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE;
        JOptionPane.showMessageDialog(RegisterFrame.this, message, title, messageType);
    }
}