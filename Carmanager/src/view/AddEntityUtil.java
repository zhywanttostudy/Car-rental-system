package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AddEntityUtil {
    private Connection connection;
    private JFrame parentFrame; // 新增：保存父窗口引用

    public AddEntityUtil(Connection connection, JFrame parentFrame) {
        this.connection = connection;
        this.parentFrame = parentFrame; // 初始化父窗口
    }

    // 添加车辆的方法
    public boolean addVehicle(String vno, String vname, String vmodel, double vprice, String vstatus, String sno) {
        // 开启事务管理，确保添加车辆和更新站点数量的原子性
        try {
            connection.setAutoCommit(false); // 关闭自动提交

            // 1. 先执行添加车辆的操作
            String insertSql = "INSERT INTO vehicle (Vno, Vname, Vmodel, Vprice, Vstatus, Sno) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(insertSql)) {
                pstmt.setString(1, vno);
                pstmt.setString(2, vname);
                pstmt.setString(3, vmodel);
                pstmt.setDouble(4, vprice);
                pstmt.setString(5, vstatus);
                pstmt.setString(6, sno);

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected <= 0) {
                    connection.rollback(); // 插入失败则回滚
                    throw new SQLException("车辆添加失败");
                }
            }

            // 2. 再执行更新站点车辆数量的操作
            String updateSql = "UPDATE station SET Vcount = Vcount + 1 WHERE Sno = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(updateSql)) {
                pstmt.setString(1, sno);
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected <= 0) {
                    connection.rollback(); // 更新失败则回滚
                    throw new SQLException("未找到对应的站点编号: " + sno);
                }
            }

            connection.commit(); // 全部成功则提交事务
            return true;
        } catch (SQLException e) {
            try {
                connection.rollback(); // 发生异常时回滚事务
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame,
                    "添加车辆失败: " + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            try {
                connection.setAutoCommit(true); // 恢复自动提交模式
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    // 添加站点的方法
    public boolean addStation(String sno, String sname, int vcount) {
        String sql = "INSERT INTO station (Sno, Sname, Vcount) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, sno);
            pstmt.setString(2, sname);
            pstmt.setInt(3, vcount);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame, // 使用父窗口
                    "添加站点失败: " + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    // 新增：显示添加车辆的表单对话框
    public void showVehicleForm() {
        JDialog dialog = new JDialog(parentFrame, "添加新车辆", true);
        dialog.setSize(500, 300);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setLayout(new GridLayout(7, 2, 10, 10));
        dialog.setResizable(false);

        // 创建表单组件
        JLabel vnoLabel = new JLabel("车辆编号:");
        JTextField vnoField = new JTextField();

        JLabel vnameLabel = new JLabel("车辆名称:");
        JTextField vnameField = new JTextField();

        JLabel vmodelLabel = new JLabel("车型:");
        JTextField vmodelField = new JTextField();

        JLabel vpriceLabel = new JLabel("日租金:");
        JTextField vpriceField = new JTextField();

        JLabel vstatusLabel = new JLabel("车辆状态:");
        String[] statusOptions = {"待租", "已租"};
        JComboBox<String> vstatusCombo = new JComboBox<>(statusOptions);

        JLabel snoLabel = new JLabel("所属站点编号:");
        JTextField snoField = new JTextField();

        JButton submitBtn = new JButton("提交");
        JButton cancelBtn = new JButton("取消");

        // 添加组件到对话框
        dialog.add(vnoLabel);
        dialog.add(vnoField);
        dialog.add(vnameLabel);
        dialog.add(vnameField);
        dialog.add(vmodelLabel);
        dialog.add(vmodelField);
        dialog.add(vpriceLabel);
        dialog.add(vpriceField);
        dialog.add(vstatusLabel);
        dialog.add(vstatusCombo);
        dialog.add(snoLabel);
        dialog.add(snoField);
        dialog.add(submitBtn);
        dialog.add(cancelBtn);

        // 取消按钮事件
        cancelBtn.addActionListener(e -> dialog.dispose());

        // 提交按钮事件
        submitBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String vno = vnoField.getText().trim();
                String vname = vnameField.getText().trim();
                String vmodel = vmodelField.getText().trim();
                String vpriceStr = vpriceField.getText().trim();
                String vstatus = (String) vstatusCombo.getSelectedItem();
                String sno = snoField.getText().trim();

                // 验证输入
                if (vno.isEmpty() || vname.isEmpty() || vmodel.isEmpty() ||
                        vpriceStr.isEmpty() || sno.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog,
                            "所有字段均为必填项!", "输入错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                double vprice;
                try {
                    vprice = Double.parseDouble(vpriceStr);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog,
                            "日租金必须是数字!", "输入错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 调用添加方法
                if (addVehicle(vno, vname, vmodel, vprice, vstatus, sno)) {
                    dialog.dispose();
                }
            }
        });

        dialog.setVisible(true);
    }

    // 新增：显示添加站点的表单对话框
    public void showStationForm() {
        JDialog dialog = new JDialog(parentFrame, "添加新站点", true);
        dialog.setSize(500, 250);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setLayout(new GridLayout(5, 2, 10, 10));
        dialog.setResizable(false);

        // 创建表单组件
        JLabel snoLabel = new JLabel("站点编号:");
        JTextField snoField = new JTextField();

        JLabel snameLabel = new JLabel("站点名称:");
        JTextField snameField = new JTextField();

        JLabel vcountLabel = new JLabel("车辆数量:");
        JSpinner vcountSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));

        JButton submitBtn = new JButton("提交");
        JButton cancelBtn = new JButton("取消");

        // 添加组件到对话框
        dialog.add(snoLabel);
        dialog.add(snoField);
        dialog.add(snameLabel);
        dialog.add(snameField);
        dialog.add(vcountLabel);
        dialog.add(vcountSpinner);
        dialog.add(submitBtn);
        dialog.add(cancelBtn);

        // 取消按钮事件
        cancelBtn.addActionListener(e -> dialog.dispose());

        // 提交按钮事件
        submitBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String sno = snoField.getText().trim();
                String sname = snameField.getText().trim();
                int vcount = (Integer) vcountSpinner.getValue();

                // 验证输入
                if (sno.isEmpty() || sname.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog,
                            "所有字段均为必填项!", "输入错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 调用添加方法
                if (addStation(sno, sname, vcount)) {
                    dialog.dispose();
                }
            }
        });

        dialog.setVisible(true);
    }

    // 添加员工的方法
    public boolean addEmployee(String eno, String ename, String epass) {
        String sql = "INSERT INTO employee (Eno, Ename, Epass) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, eno);
            pstmt.setString(2, ename);
            pstmt.setString(3, epass);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame,
                    "添加员工失败: " + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    // 新增：显示添加员工的表单对话框
    public void showEmployeeForm() {
        JDialog dialog = new JDialog(parentFrame, "添加新员工", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setLayout(new GridLayout(5, 2, 10, 10));
        dialog.setResizable(false);

        // 创建表单组件
        JLabel enoLabel = new JLabel("员工编号:");
        JTextField enoField = new JTextField();

        JLabel enameLabel = new JLabel("员工姓名:");
        JTextField enameField = new JTextField();

        JLabel epassLabel = new JLabel("登录密码:");
        JPasswordField epassField = new JPasswordField();

        JButton submitBtn = new JButton("提交");
        JButton cancelBtn = new JButton("取消");

        // 添加组件到对话框
        dialog.add(enoLabel);
        dialog.add(enoField);
        dialog.add(enameLabel);
        dialog.add(enameField);
        dialog.add(epassLabel);
        dialog.add(epassField);
        dialog.add(submitBtn);
        dialog.add(cancelBtn);

        // 取消按钮事件
        cancelBtn.addActionListener(e -> dialog.dispose());

        // 提交按钮事件
        submitBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String eno = enoField.getText().trim();
                String ename = enameField.getText().trim();
                String epass = new String(epassField.getPassword()).trim();

                // 验证输入
                if (eno.isEmpty() || ename.isEmpty() || epass.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog,
                            "所有字段均为必填项!", "输入错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 调用添加方法
                if (addEmployee(eno, ename, epass)) {
                    JOptionPane.showMessageDialog(dialog,
                            "员工添加成功!", "成功", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                }
            }
        });

        dialog.setVisible(true);
    }

}