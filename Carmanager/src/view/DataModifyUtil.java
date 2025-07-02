package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 数据修改工具类 - 集中管理所有实体数据的修改功能
 */
public class DataModifyUtil {
    private Connection connection;
    private JFrame parentFrame;
    private QueryUtil queryUtil;

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

    public DataModifyUtil(Connection connection, JFrame parentFrame, QueryUtil queryUtil) {
        this.connection = connection;
        this.parentFrame = parentFrame;
        this.queryUtil = queryUtil;
    }

    /**
     * 修改车辆数据（包含站点数量调整逻辑）
     */
    public void modifyVehicle(JTable vehicleTable) {
        int selectedRow = vehicleTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(parentFrame, "请先选择要修改的车辆记录",
                    "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 获取当前数据
        DefaultTableModel model = (DefaultTableModel) vehicleTable.getModel();
        String vno = (String) model.getValueAt(selectedRow, 0);
        String vname = (String) model.getValueAt(selectedRow, 1);
        String vmodel = (String) model.getValueAt(selectedRow, 2);
        double vprice = Double.parseDouble(model.getValueAt(selectedRow, 3).toString());
        String vstatus = (String) model.getValueAt(selectedRow, 4);
        String oldSno = (String) model.getValueAt(selectedRow, 5); // 原站点编号

        // 创建修改对话框
        JDialog dialog = new JDialog(parentFrame, "修改车辆信息", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setLayout(new GridBagLayout());
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 表单组件
        JTextField vnoField = new JTextField(vno, 10);
        vnoField.setEditable(false);
        JTextField vnameField = new JTextField(vname, 10);
        JTextField vmodelField = new JTextField(vmodel, 10);
        JTextField vpriceField = new JTextField(String.valueOf(vprice), 10);
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"待租", "已租"});
        statusCombo.setSelectedItem(vstatus);
        JTextField snoField = new JTextField(oldSno, 10); // 初始显示原站点编号

        // 添加组件到对话框
        addComponent(dialog, new JLabel("车辆编号:"), gbc, 0, 0);
        addComponent(dialog, vnoField, gbc, 1, 0);
        addComponent(dialog, new JLabel("车辆名称:"), gbc, 0, 1);
        addComponent(dialog, vnameField, gbc, 1, 1);
        addComponent(dialog, new JLabel("车型:"), gbc, 0, 2);
        addComponent(dialog, vmodelField, gbc, 1, 2);
        addComponent(dialog, new JLabel("日租金:"), gbc, 0, 3);
        addComponent(dialog, vpriceField, gbc, 1, 3);
        addComponent(dialog, new JLabel("状态:"), gbc, 0, 4);
        addComponent(dialog, statusCombo, gbc, 1, 4);
        addComponent(dialog, new JLabel("所属站点编号:"), gbc, 0, 5);
        addComponent(dialog, snoField, gbc, 1, 5);

        // 按钮面板
        JPanel btnPanel = new JPanel();
        JButton confirmBtn = new JButton("确认修改");
        JButton cancelBtn = new JButton("取消");

        confirmBtn.addActionListener(e -> {
            try {
                String newVname = vnameField.getText().trim();
                String newVmodel = vmodelField.getText().trim();
                double newVprice = Double.parseDouble(vpriceField.getText().trim());
                String newVstatus = (String) statusCombo.getSelectedItem();
                String newSno = snoField.getText().trim();

                // 验证输入
                if (newVname.isEmpty() || newSno.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "车辆名称和站点编号不能为空",
                            "输入错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 开启事务
                connection.setAutoCommit(false);

                try {
                    // 1. 更新车辆所属站点
                    String sqlVehicle = "UPDATE vehicle SET Vname = ?, Vmodel = ?, Vprice = ?, " +
                            "Vstatus = ?, Sno = ? WHERE Vno = ?";
                    try (PreparedStatement pstmt = connection.prepareStatement(sqlVehicle)) {
                        pstmt.setString(1, newVname);
                        pstmt.setString(2, newVmodel);
                        pstmt.setDouble(3, newVprice);
                        pstmt.setString(4, newVstatus);
                        pstmt.setString(5, newSno);
                        pstmt.setString(6, vno);
                        int affectedRows = pstmt.executeUpdate();
                        if (affectedRows == 0) {
                            throw new SQLException("未找到要修改的车辆记录");
                        }
                    }

                    // 2. 如果站点编号变更，调整站点车辆数量
                    if (!oldSno.equals(newSno)) {
                        // 原站点减1
                        String sqlOldStation = "UPDATE station SET Vcount = Vcount - 1 WHERE Sno = ?";
                        try (PreparedStatement pstmt = connection.prepareStatement(sqlOldStation)) {
                            pstmt.setString(1, oldSno);
                            int affectedRows = pstmt.executeUpdate();
                            if (affectedRows == 0) {
                                throw new SQLException("未找到原站点记录");
                            }
                        }

                        // 新站点加1
                        String sqlNewStation = "UPDATE station SET Vcount = Vcount + 1 WHERE Sno = ?";
                        try (PreparedStatement pstmt = connection.prepareStatement(sqlNewStation)) {
                            pstmt.setString(1, newSno);
                            int affectedRows = pstmt.executeUpdate();
                            if (affectedRows == 0) {
                                throw new SQLException("未找到新站点记录");
                            }
                        }
                    }

                    // 提交事务
                    connection.commit();

                    // 刷新表格
                    queryUtil.queryTable(vehicleTable, "vehicle");
                    dialog.dispose();
                } catch (SQLException ex) {
                    // 事务回滚
                    connection.rollback();
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(dialog, "修改失败: " + ex.getMessage(),
                            "错误", JOptionPane.ERROR_MESSAGE);
                } finally {
                    // 恢复自动提交模式
                    connection.setAutoCommit(true);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "日租金必须是数字",
                        "输入错误", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "数据库操作失败: " + ex.getMessage(),
                        "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        btnPanel.add(confirmBtn);
        btnPanel.add(cancelBtn);
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        dialog.add(btnPanel, gbc);

        dialog.setVisible(true);
    }

    /**
     * 修改订单数据
     */
    public void modifyOrder(JTable orderTable) {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(parentFrame, "请先选择要修改的订单记录",
                    "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 获取当前数据
        DefaultTableModel model = (DefaultTableModel) orderTable.getModel();
        String ono = (String) model.getValueAt(selectedRow, 0);
        String cno = (String) model.getValueAt(selectedRow, 1);
        String vno = (String) model.getValueAt(selectedRow, 2);
        String startDateStr = (String) model.getValueAt(selectedRow, 3);
        String returnDateStr = (String) model.getValueAt(selectedRow, 4);
        double ofee = Double.parseDouble(model.getValueAt(selectedRow, 5).toString());

        // 创建修改对话框
        JDialog dialog = new JDialog(parentFrame, "修改订单信息", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setLayout(new GridBagLayout());
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 表单组件
        JTextField onoField = new JTextField(ono, 10);
        onoField.setEditable(false);
        JTextField cnoField = new JTextField(cno, 10);
        JTextField vnoField = new JTextField(vno, 10);
        JTextField startDateField = new JTextField(startDateStr, 10);
        JTextField returnDateField = new JTextField(returnDateStr, 10);
        JTextField ofeeField = new JTextField(String.valueOf(ofee), 10);

        // 添加组件到对话框
        addComponent(dialog, new JLabel("订单编号:"), gbc, 0, 0);
        addComponent(dialog, onoField, gbc, 1, 0);
        addComponent(dialog, new JLabel("客户编号:"), gbc, 0, 1);
        addComponent(dialog, cnoField, gbc, 1, 1);
        addComponent(dialog, new JLabel("车辆编号:"), gbc, 0, 2);
        addComponent(dialog, vnoField, gbc, 1, 2);
        addComponent(dialog, new JLabel("开始日期 (yyyy-MM-dd):"), gbc, 0, 3);
        addComponent(dialog, startDateField, gbc, 1, 3);
        addComponent(dialog, new JLabel("归还日期 (yyyy-MM-dd):"), gbc, 0, 4);
        addComponent(dialog, returnDateField, gbc, 1, 4);
        addComponent(dialog, new JLabel("费用:"), gbc, 0, 5);
        addComponent(dialog, ofeeField, gbc, 1, 5);

        // 按钮面板
        JPanel btnPanel = new JPanel();
        JButton confirmBtn = new JButton("确认修改");
        JButton cancelBtn = new JButton("取消");

        confirmBtn.addActionListener(e -> {
            try {
                String newCno = cnoField.getText().trim();
                String newVno = vnoField.getText().trim();
                String newStartDateStr = startDateField.getText().trim();
                String newReturnDateStr = returnDateField.getText().trim();
                double newOfee = Double.parseDouble(ofeeField.getText().trim());

                // 验证输入
                if (newCno.isEmpty() || newVno.isEmpty() ||
                        newStartDateStr.isEmpty() || newReturnDateStr.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "客户编号、车辆编号和日期不能为空",
                            "输入错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 日期格式验证
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date newStartDate = sdf.parse(newStartDateStr);
                Date newReturnDate = sdf.parse(newReturnDateStr);

                // 日期顺序验证
                if (newReturnDate.before(newStartDate)) {
                    JOptionPane.showMessageDialog(dialog, "归还日期必须在开始日期之后",
                            "输入错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 执行更新
                String sql = "UPDATE orders SET Cno = ?, Vno = ?, Ostart = ?, " +
                        "Oreturn = ?, Ofee = ? WHERE Ono = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                    pstmt.setString(1, newCno);
                    pstmt.setString(2, newVno);
                    pstmt.setDate(3, new java.sql.Date(newStartDate.getTime()));
                    pstmt.setDate(4, new java.sql.Date(newReturnDate.getTime()));
                    pstmt.setDouble(5, newOfee);
                    pstmt.setString(6, ono);
                    int affectedRows = pstmt.executeUpdate();
                    if (affectedRows > 0) {
                        // 刷新表格
                        queryUtil.queryTable(orderTable, "orders");
                        dialog.dispose();
                    } else {
                        throw new SQLException("未找到要修改的记录");
                    }
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "费用必须是数字",
                        "输入错误", JOptionPane.ERROR_MESSAGE);
            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(dialog, "日期格式必须为 yyyy-MM-dd",
                        "输入错误", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "修改失败: " + ex.getMessage(),
                        "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        btnPanel.add(confirmBtn);
        btnPanel.add(cancelBtn);
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        dialog.add(btnPanel, gbc);

        dialog.setVisible(true);
    }

    /**
     * 修改站点数据
     */
    public void modifyStation(JTable stationTable) {
        int selectedRow = stationTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(parentFrame, "请先选择要修改的站点记录",
                    "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 获取当前数据
        DefaultTableModel model = (DefaultTableModel) stationTable.getModel();
        String sno = (String) model.getValueAt(selectedRow, 0);
        String sname = (String) model.getValueAt(selectedRow, 1);
        int vcount = Integer.parseInt(model.getValueAt(selectedRow, 2).toString());

        // 创建修改对话框
        JDialog dialog = new JDialog(parentFrame, "修改站点信息", true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setLayout(new GridBagLayout());
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 表单组件
        JTextField snoField = new JTextField(sno, 10);
        snoField.setEditable(false);
        JTextField snameField = new JTextField(sname, 10);
        JTextField vcountField = new JTextField(String.valueOf(vcount), 10);

        // 添加组件到对话框
        addComponent(dialog, new JLabel("站点编号:"), gbc, 0, 0);
        addComponent(dialog, snoField, gbc, 1, 0);
        addComponent(dialog, new JLabel("站点名称:"), gbc, 0, 1);
        addComponent(dialog, snameField, gbc, 1, 1);
        addComponent(dialog, new JLabel("车辆数量:"), gbc, 0, 2);
        addComponent(dialog, vcountField, gbc, 1, 2);

        // 按钮面板
        JPanel btnPanel = new JPanel();
        JButton confirmBtn = new JButton("确认修改");
        JButton cancelBtn = new JButton("取消");

        confirmBtn.addActionListener(e -> {
            try {
                String newSname = snameField.getText().trim();
                int newVcount = Integer.parseInt(vcountField.getText().trim());

                // 验证输入
                if (newSname.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "站点名称不能为空",
                            "输入错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 执行更新
                String sql = "UPDATE station SET Sname = ?, Vcount = ? WHERE Sno = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                    pstmt.setString(1, newSname);
                    pstmt.setInt(2, newVcount);
                    pstmt.setString(3, sno);
                    int affectedRows = pstmt.executeUpdate();
                    if (affectedRows > 0) {
                        // 刷新表格
                        queryUtil.queryTable(stationTable, "station");
                        dialog.dispose();
                    } else {
                        throw new SQLException("未找到要修改的记录");
                    }
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "车辆数量必须是整数",
                        "输入错误", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "修改失败: " + ex.getMessage(),
                        "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        btnPanel.add(confirmBtn);
        btnPanel.add(cancelBtn);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        dialog.add(btnPanel, gbc);

        dialog.setVisible(true);
    }

    /**
     * 修改客户数据
     */
    public void modifyCustomer(JTable customerTable) {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(parentFrame, "请先选择要修改的客户记录",
                    "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 获取当前数据
        DefaultTableModel model = (DefaultTableModel) customerTable.getModel();
        String cno = (String) model.getValueAt(selectedRow, 0);

        // 从数据库获取完整数据
        String cname = "";
        String csex = "";
        Integer cage = null;
        String caddress = "";
        String cphone = "";
        String cpass = "";
        String sql = "SELECT Cname, Csex, Cage, TRIM(Caddress) AS Caddress, Cphone, Cpass FROM customer WHERE Cno = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, cno);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    cname = rs.getString("Cname");
                    csex = rs.getString("Csex");
                    cage = rs.getInt("Cage");
                    caddress = rs.getString("Caddress");
                    cphone = rs.getString("Cphone");
                    cpass = rs.getString("Cpass");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame, "获取数据失败: " + ex.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 创建修改对话框
        JDialog dialog = new JDialog(parentFrame, "修改客户信息", true);
        dialog.setSize(400, 450); // 调整对话框高度
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setLayout(new GridBagLayout());
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 表单组件
        JTextField cnoField = new JTextField(cno, 10);
        cnoField.setEditable(false);
        JTextField cnameField = new JTextField(cname, 10);
        JComboBox<String> sexCombo = new JComboBox<>(new String[]{"", "男", "女"});
        sexCombo.setEditable(false);
        if (csex != null && !csex.isEmpty()) {
            sexCombo.setSelectedItem(csex);
        }
        JTextField ageField = new JTextField(cage != null ? String.valueOf(cage) : "", 10);

        // 地址选择组件 - 纯下拉列表（无详细地址输入框）
        JComboBox<String> addressCombo = new JComboBox<>(PROVINCES);
        addressCombo.setEditable(false); // 下拉框不可编辑
        // 从数据库地址中查找匹配的省份并选择
        if (caddress != null && !caddress.isEmpty()) {
            for (String province : PROVINCES) {
                if (caddress.startsWith(province)) {
                    addressCombo.setSelectedItem(province);
                    break;
                }
            }
        }

        JTextField phoneField = new JTextField(cphone, 10);
        JTextField passField = new JTextField(cpass, 10);

        // 添加组件到对话框
        addComponent(dialog, new JLabel("客户编号:"), gbc, 0, 0);
        addComponent(dialog, cnoField, gbc, 1, 0);
        addComponent(dialog, new JLabel("客户姓名:"), gbc, 0, 1);
        addComponent(dialog, cnameField, gbc, 1, 1);
        addComponent(dialog, new JLabel("性别:"), gbc, 0, 2);
        addComponent(dialog, sexCombo, gbc, 1, 2);
        addComponent(dialog, new JLabel("年龄:"), gbc, 0, 3);
        addComponent(dialog, ageField, gbc, 1, 3);
        addComponent(dialog, new JLabel("地址:"), gbc, 0, 4);
        addComponent(dialog, addressCombo, gbc, 1, 4); // 直接使用下拉列表作为地址选择
        addComponent(dialog, new JLabel("联系电话:"), gbc, 0, 5);
        addComponent(dialog, phoneField, gbc, 1, 5);
        addComponent(dialog, new JLabel("密码:"), gbc, 0, 6);
        addComponent(dialog, passField, gbc, 1, 6);

        // 按钮面板
        JPanel btnPanel = new JPanel();
        JButton confirmBtn = new JButton("确认修改");
        JButton cancelBtn = new JButton("取消");

        confirmBtn.addActionListener(e -> {
            try {
                String newCname = cnameField.getText().trim();
                String newCsex = (String) sexCombo.getSelectedItem();
                Integer newCage = null;
                if (!ageField.getText().trim().isEmpty()) {
                    newCage = Integer.parseInt(ageField.getText().trim());
                }

                String newCaddress = (String) addressCombo.getSelectedItem(); // 直接使用选择的省份作为地址
                String newCphone = phoneField.getText().trim();
                String newCpass = passField.getText().trim();

                // 验证输入
                if (newCname.isEmpty() || newCphone.isEmpty() || newCpass.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "姓名、联系电话和密码不能为空",
                            "输入错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (newCaddress == null || newCaddress.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "请选择地址",
                            "输入错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 执行更新
                String sqlUpdate = "UPDATE customer SET Cname = ?, Csex = ?, Cage = ?, " +
                        "Caddress = ?, Cphone = ?, Cpass = ? WHERE Cno = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(sqlUpdate)) {
                    pstmt.setString(1, newCname);
                    pstmt.setString(2, newCsex);
                    if (newCage != null) {
                        pstmt.setInt(3, newCage);
                    } else {
                        pstmt.setNull(3, Types.INTEGER);
                    }
                    pstmt.setString(4, newCaddress); // 直接存储选择的省份
                    pstmt.setString(5, newCphone);
                    pstmt.setString(6, newCpass);
                    pstmt.setString(7, cno);

                    int affectedRows = pstmt.executeUpdate();
                    if (affectedRows > 0) {
                        queryUtil.queryTable(customerTable, "customer");
                        dialog.dispose();
                        JOptionPane.showMessageDialog(parentFrame, "客户信息修改成功",
                                "成功", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        throw new SQLException("未找到要修改的记录");
                    }
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "年龄必须是整数",
                        "输入错误", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "修改失败: " + ex.getMessage(),
                        "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        btnPanel.add(confirmBtn);
        btnPanel.add(cancelBtn);
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        dialog.add(btnPanel, gbc);

        dialog.setVisible(true);
    }

    /**
     * 修改员工数据
     */
    public void modifyEmployee(JTable employeeTable) {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(parentFrame, "请先选择要修改的员工记录",
                    "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 获取当前数据
        DefaultTableModel model = (DefaultTableModel) employeeTable.getModel();
        String eno = (String) model.getValueAt(selectedRow, 0);
        String ename = (String) model.getValueAt(selectedRow, 1);
        String epass = (String) model.getValueAt(selectedRow, 2);

        // 创建修改对话框
        JDialog dialog = new JDialog(parentFrame, "修改员工信息", true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setLayout(new GridBagLayout());
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 表单组件
        JTextField enoField = new JTextField(eno, 10);
        enoField.setEditable(false);
        JTextField enameField = new JTextField(ename, 10);
        JTextField epassField = new JTextField(epass, 10);

        // 添加组件到对话框
        addComponent(dialog, new JLabel("员工编号:"), gbc, 0, 0);
        addComponent(dialog, enoField, gbc, 1, 0);
        addComponent(dialog, new JLabel("员工姓名:"), gbc, 0, 1);
        addComponent(dialog, enameField, gbc, 1, 1);
        addComponent(dialog, new JLabel("员工密码:"), gbc, 0, 2);
        addComponent(dialog, epassField, gbc, 1, 2);

        // 按钮面板
        JPanel btnPanel = new JPanel();
        JButton confirmBtn = new JButton("确认修改");
        JButton cancelBtn = new JButton("取消");

        confirmBtn.addActionListener(e -> {
            try {
                String newEname = enameField.getText().trim();
                String newEpass = epassField.getText().trim();

                // 验证输入
                if (newEname.isEmpty() || newEpass.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "姓名和密码不能为空",
                            "输入错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 执行更新
                String sql = "UPDATE employee SET Ename = ?, Epass = ? WHERE Eno = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                    pstmt.setString(1, newEname);
                    pstmt.setString(2, newEpass);
                    pstmt.setString(3, eno);
                    int affectedRows = pstmt.executeUpdate();
                    if (affectedRows > 0) {
                        // 刷新表格
                        queryUtil.queryTable(employeeTable, "employee");
                        dialog.dispose();
                    } else {
                        throw new SQLException("未找到要修改的记录");
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "修改失败: " + ex.getMessage(),
                        "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        btnPanel.add(confirmBtn);
        btnPanel.add(cancelBtn);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        dialog.add(btnPanel, gbc);

        dialog.setVisible(true);
    }

    /**
     * 辅助方法：向对话框添加组件
     */
    private void addComponent(JDialog dialog, Component component, GridBagConstraints gbc, int gridx, int gridy) {
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        dialog.add(component, gbc);
    }
}