package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerInfoModifyUtil {
    private Connection connection;
    private JFrame parentFrame;
    private String customerId;

    // 34 个省级行政区
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

    public CustomerInfoModifyUtil(Connection connection, JFrame parentFrame, String customerId) {
        this.connection = connection;
        this.parentFrame = parentFrame;
        this.customerId = customerId;
    }

    public void showModifyInfoDialog() {
        JDialog dialog = new JDialog(parentFrame, "修改客户信息", true);
        dialog.setSize(400, 600);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 标题
        JLabel titleLabel = new JLabel("修改客户信息", JLabel.CENTER);
        titleLabel.setFont(new Font("宋体", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        dialog.add(titleLabel, gbc);

        // 客户编号（不可编辑）
        gbc.gridwidth = 1;
        JLabel cnoLabel = new JLabel("客户编号:");
        JLabel cnoValueLabel = new JLabel(customerId);
        addFormField(dialog, gbc, cnoLabel, 1, cnoValueLabel);

        // 客户姓名
        JLabel cnameLabel = new JLabel("客户姓名:");
        JTextField cnameField = new JTextField();
        addFormField(dialog, gbc, cnameLabel, 2, cnameField);

        // 客户性别
        JLabel csexLabel = new JLabel("客户性别:");
        String[] genders = {"男", "女"};
        JComboBox<String> csexCombo = new JComboBox<>(genders);
        addFormField(dialog, gbc, csexLabel, 3, csexCombo);

        // 客户年龄
        JLabel cageLabel = new JLabel("客户年龄:");
        JTextField cageField = new JTextField();
        addFormField(dialog, gbc, cageLabel, 4, cageField);

        // 联系电话
        JLabel cphoneLabel = new JLabel("联系电话:");
        JTextField cphoneField = new JTextField();
        addFormField(dialog, gbc, cphoneLabel, 5, cphoneField);

        // 客户地址
        JLabel caddressLabel = new JLabel("客户地址:");
        JComboBox<String> caddressComboBox = new JComboBox<>(PROVINCES);
        addFormField(dialog, gbc, caddressLabel, 6, caddressComboBox);

        // 客户密码（修改为普通文本字段显示明文）
        JLabel cpassLabel = new JLabel("客户密码:");
        JTextField cpassField = new JTextField();  // 改为JTextField
        addFormField(dialog, gbc, cpassLabel, 7, cpassField);

        // 提交和取消按钮
        JPanel buttonPanel = new JPanel();
        JButton submitBtn = new JButton("提交");
        JButton cancelBtn = new JButton("取消");

        buttonPanel.add(submitBtn);
        buttonPanel.add(cancelBtn);

        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 2;
        dialog.add(buttonPanel, gbc);

        // 加载当前客户信息
        loadCustomerInfo(cnameField, csexCombo, cageField, cphoneField, caddressComboBox, cpassField);

        cancelBtn.addActionListener(e -> dialog.dispose());

        submitBtn.addActionListener(e -> {
            String cname = cnameField.getText().trim();
            String csex = (String) csexCombo.getSelectedItem();
            String cageStr = cageField.getText().trim();
            String cphone = cphoneField.getText().trim();
            String caddress = (String) caddressComboBox.getSelectedItem();
            String cpass = cpassField.getText().trim();  // 直接获取文本

            // 验证输入
            if (cname.isEmpty() || cphone.isEmpty() || cpass.isEmpty()) {
                showModifyResult(false, "客户姓名、联系电话和密码不能为空");
                return;
            }

            if (cageStr.isEmpty() || !isNumeric(cageStr)) {
                showModifyResult(false, "年龄必须为数字");
                return;
            }

            // 验证联系电话是否为 11 位
            if (cphone.length() != 11 || !cphone.matches("\\d+")) {
                showModifyResult(false, "联系电话必须为 11 位有效数字");
                return;
            }

            int cage = Integer.parseInt(cageStr);

            // 更新数据库
            String updateSql = "UPDATE customer SET Cname = ?, Csex = ?, Cage = ?, Cphone = ?, Caddress = ?, Cpass = ? WHERE Cno = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(updateSql)) {
                pstmt.setString(1, cname);
                pstmt.setString(2, csex);
                pstmt.setInt(3, cage);
                pstmt.setString(4, cphone);
                pstmt.setString(5, caddress);
                pstmt.setString(6, cpass);
                pstmt.setString(7, customerId);
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    showModifyResult(true, "信息修改成功！");
                    dialog.dispose();
                } else {
                    showModifyResult(false, "信息修改失败！");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                showModifyResult(false, "数据库操作失败: " + ex.getMessage());
            }
        });

        dialog.setVisible(true);
    }

    // 添加表单字段
    private void addFormField(Container panel, GridBagConstraints gbc, JLabel label, int y, JComponent component) {
        gbc.gridx = 0;
        gbc.gridy = y;
        panel.add(label, gbc);

        gbc.gridx = 1;
        panel.add(component, gbc);
    }

    // 验证是否为数字
    private boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // 显示修改结果
    private void showModifyResult(boolean success, String message) {
        String title = success ? "修改成功" : "修改失败";
        int messageType = success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE;
        JOptionPane.showMessageDialog(parentFrame, message, title, messageType);
    }

    private void loadCustomerInfo(JTextField cnameField, JComboBox<String> csexCombo, 
                                 JTextField cageField, JTextField cphoneField, 
                                 JComboBox<String> caddressComboBox, JTextField cpassField) {  // 参数类型改为JTextField
        String sql = "SELECT Cname, Csex, Cage, Cphone, Caddress, Cpass FROM customer WHERE Cno = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // 加载姓名
                    cnameField.setText(rs.getString("Cname"));
                    
                    // 加载性别（处理可能的空值和空格）
                    String dbSex = rs.getString("Csex");
                    if (dbSex != null) {
                        dbSex = dbSex.trim();
                        if ("男".equals(dbSex)) {
                            csexCombo.setSelectedIndex(0);
                        } else if ("女".equals(dbSex)) {
                            csexCombo.setSelectedIndex(1);
                        }
                    }
                    
                    // 加载年龄
                    int age = rs.getInt("Cage");
                    if (!rs.wasNull()) { // 检查是否为NULL
                        cageField.setText(String.valueOf(age));
                    } else {
                        cageField.setText("");
                    }
                    
                    // 加载电话
                    cphoneField.setText(rs.getString("Cphone"));
                    
                    // 加载地址
                    String dbAddress = rs.getString("Caddress");
                    if (dbAddress != null) {
                        dbAddress = dbAddress.trim(); // 去除空格
                        
                        // 尝试在组合框中查找匹配项
                        boolean found = false;
                        for (int i = 0; i < caddressComboBox.getItemCount(); i++) {
                            if (caddressComboBox.getItemAt(i).equals(dbAddress)) {
                                caddressComboBox.setSelectedIndex(i);
                                found = true;
                                break;
                            }
                        }
                        
                        // 未找到匹配项时设置默认值
                        if (!found && caddressComboBox.getItemCount() > 0) {
                            caddressComboBox.setSelectedIndex(0);
                        }
                    } else {
                        // 地址为NULL时设置默认值
                        caddressComboBox.setSelectedIndex(0);
                    }
                    
                    // 加载密码（直接显示明文）
                    cpassField.setText(rs.getString("Cpass"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame,
                    "查询客户信息失败: " + e.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}