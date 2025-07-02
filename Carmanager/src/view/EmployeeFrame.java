// 员工系统
package view;

import com.toedter.calendar.JDateChooser;
import entity.Employee;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import service.RentStatisticsService;

public class EmployeeFrame extends JFrame {
    private Employee employee;
    private Connection connection;
    private QueryUtil queryUtil;
    private JTabbedPane tabbedPane;
    private JTable vehicleTable, orderTable, stationTable, customerTable, employeeTable;
    private AddEntityUtil addUtil;
    private DataModifyUtil modifyUtil;
    private DeleteUtil deleteUtil; // 新增删除功能的工具类

    public EmployeeFrame(Employee employee) {
        this.employee = employee;
        setTitle("员工管理系统 - " + employee.getUserId());
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 连接数据库并初始化queryUtil
        connectToDatabase();
        if (connection != null) {
            queryUtil = new QueryUtil(connection);
            addUtil = new AddEntityUtil(connection, this);
            modifyUtil = new DataModifyUtil(connection, this, queryUtil);
            deleteUtil = new DeleteUtil(connection, this); // 初始化DeleteUtil

            // 主面板
            JPanel mainPanel = new JPanel(new BorderLayout());

            // 欢迎信息
            JLabel welcomeLabel = new JLabel("欢迎 " + employee.getUserId() + " 进入员工管理系统！");
            welcomeLabel.setFont(new Font("宋体", Font.BOLD, 16));
            welcomeLabel.setHorizontalAlignment(JLabel.CENTER);
            welcomeLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            mainPanel.add(welcomeLabel, BorderLayout.NORTH);

            // 标签页面板
            tabbedPane = new JTabbedPane();
            tabbedPane.addTab("车辆信息", createVehiclePanel());
            tabbedPane.addTab("订单信息", createOrderPanel());
            tabbedPane.addTab("站点信息", createStationPanel());
            tabbedPane.addTab("客户信息", createCustomerPanel());
            tabbedPane.addTab("员工信息", createEmployeePanel());

            mainPanel.add(tabbedPane, BorderLayout.CENTER);

            // 底部按钮面板
            JPanel buttonPanel = new JPanel();
            JButton logoutBtn = new JButton("退出登录");
            logoutBtn.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "确认要退出登录吗？",
                        "确认退出",
                        JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    dispose();
                    try {
                        // 确保MainFrame类存在
                        Class.forName("view.MainFrame");
                        java.lang.reflect.Constructor<?> constructor = Class.forName("view.MainFrame").getConstructor();
                        constructor.newInstance();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(this,
                                "返回登录界面失败: " + ex.getMessage(),
                                "错误",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            buttonPanel.add(logoutBtn);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);

            add(mainPanel);
            setVisible(true);
        }
    }

    // 修改createVehiclePanel方法，为按钮添加图标
    private JPanel createVehiclePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        // 初始化车辆信息表
        String[] columnNames = {"车辆编号", "车辆名称", "型号", "价格", "状态", "站点名称"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        vehicleTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(vehicleTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 功能按钮
        JPanel btnPanel = new JPanel();
        JButton refreshBtn = new JButton("刷新车辆列表");
        JButton addBtn = new JButton("添加车辆");
        JButton modifyBtn = new JButton("修改车辆信息");
        JButton deleteBtn = new JButton("删除车辆");

        // 为按钮添加图标
        service.IconUtils.addIconToButton(refreshBtn, "/view/刷新.png");
        service.IconUtils.addIconToButton(addBtn, "/view/增加.png");
        service.IconUtils.addIconToButton(modifyBtn, "/view/修改.png");
        service.IconUtils.addIconToButton(deleteBtn, "/view/删除.png");

        // 添加按钮事件
        refreshBtn.addActionListener(e -> queryUtil.queryTable(vehicleTable, "vehicle"));
        addBtn.addActionListener(e -> {
            addUtil.showVehicleForm();
            queryUtil.queryTable(vehicleTable, "vehicle");
        });
        modifyBtn.addActionListener(e -> modifyUtil.modifyVehicle(vehicleTable));
        deleteBtn.addActionListener(e -> {
            int selectedRow = vehicleTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "请选择要删除的车辆", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String vehicleId = (String) vehicleTable.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "确认要删除车辆 " + vehicleId + " 吗？",
                    "确认删除",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (deleteUtil.deleteVehicle(vehicleId)) {
                    queryUtil.queryTable(vehicleTable, "vehicle");
                }
            }
        });

        btnPanel.add(refreshBtn);
        btnPanel.add(addBtn);
        btnPanel.add(modifyBtn);
        btnPanel.add(deleteBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        // 添加车辆查询面板
        VehicleQueryPanel queryPanel = new VehicleQueryPanel(queryUtil, vehicleTable, connection);
        panel.add(queryPanel, BorderLayout.NORTH);

        // 初始化车辆信息
        queryUtil.queryTable(vehicleTable, "vehicle");
        return panel;
    }


    // 在createOrderPanel方法中为订单面板按钮添加图标
    private JPanel createOrderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        // 初始化订单信息表 '订单状态' 字段
        String[] columnNames = {"订单编号", "客户编号", "车辆编号", "开始日期", "归还日期", "订单状态", "费用"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        orderTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(orderTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // 订单统计查询面板和查询按钮
        JPanel queryPanel = new JPanel();
        JDateChooser startDateChooser = new JDateChooser();
        JDateChooser endDateChooser = new JDateChooser();
        JButton queryBtn = new JButton("统计费用");
        
        // 为统计按钮添加图标
        service.IconUtils.addIconToButton(queryBtn, "/view/刷新.png");
        
        queryPanel.add(new JLabel("开始日期:"));
        queryPanel.add(startDateChooser);
        queryPanel.add(new JLabel("结束日期:"));
        queryPanel.add(endDateChooser);
        queryPanel.add(queryBtn);
        panel.add(queryPanel, BorderLayout.NORTH);
        
        // 统计费用按钮的点击事件处理
        queryBtn.addActionListener(e -> {
            Date startDate = startDateChooser.getDate();
            Date endDate = endDateChooser.getDate();
            // 检查日期是否为空
            if (startDate == null || endDate == null) {
                JOptionPane.showMessageDialog(this, "日期不能为空，请重新选择！", "日期选择错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // 检查开始日期是否晚于结束日期
            if (startDate.after(endDate)) {
                JOptionPane.showMessageDialog(this, "开始日期不能晚于结束日期，请重新选择！", "日期选择错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String startDateStr = sdf.format(startDate);
            String endDateStr = sdf.format(endDate);
            try {
                RentStatisticsService statisticsService = new RentStatisticsService(connection);
                double totalFee = statisticsService.calculateRentFee(startDateStr, endDateStr);
                JOptionPane.showMessageDialog(this, "该时间段的租赁总费用: " + totalFee + " 元", "统计结果", JOptionPane.INFORMATION_MESSAGE);
            } catch (ParseException | SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "统计费用时发生错误: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        // 刷新按钮
        JButton refreshBtn = new JButton("刷新订单列表");
        
        // 为刷新按钮添加图标
        service.IconUtils.addIconToButton(refreshBtn, "/view/刷新.png");
        
        refreshBtn.addActionListener(e -> queryUtil.queryTable(orderTable, "orders"));
        JPanel btnPanel = new JPanel();
        btnPanel.add(refreshBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);
        
        // 初始化订单信息
        queryUtil.queryTable(orderTable, "orders");
        return panel;
    }

    // 在createStationPanel方法中为站点面板按钮添加图标
    private JPanel createStationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        // 初始化站点信息表
        String[] columnNames = {"站点编号", "站点名称", "地址"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        stationTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(stationTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // 添加站点按钮
        JButton addStationBtn = new JButton("添加站点");
        JButton modifyBtn = new JButton("修改站点信息");
        JButton deleteBtn = new JButton("删除站点");
        JButton refreshBtn = new JButton("刷新站点列表");
        
        // 为按钮添加图标
        service.IconUtils.addIconToButton(addStationBtn, "/view/增加.png");
        service.IconUtils.addIconToButton(modifyBtn, "/view/修改.png");
        service.IconUtils.addIconToButton(deleteBtn, "/view/删除.png");
        service.IconUtils.addIconToButton(refreshBtn, "/view/刷新.png");
        
        addStationBtn.addActionListener(e -> {
            addUtil.showStationForm();
            queryUtil.queryTable(stationTable, "station");
        });
        modifyBtn.addActionListener(e -> modifyUtil.modifyStation(stationTable));
        deleteBtn.addActionListener(e -> {
            int selectedRow = stationTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "请选择要删除的站点", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String stationId = (String) stationTable.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "确认要删除站点 " + stationId + " 吗？",
                    "确认删除",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (deleteUtil.deleteStation(stationId)) {
                    queryUtil.queryTable(stationTable, "station");
                }
            }
        });
        refreshBtn.addActionListener(e -> queryUtil.queryTable(stationTable, "station"));
        
        JPanel btnPanel = new JPanel();
        btnPanel.add(refreshBtn);
        btnPanel.add(addStationBtn);
        btnPanel.add(modifyBtn);
        btnPanel.add(deleteBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);
        
        // 添加站点查询面板
        StationQueryPanel queryPanel = new StationQueryPanel(queryUtil, stationTable, connection);
        panel.add(queryPanel, BorderLayout.NORTH);
        
        // 初始化站点信息
        queryUtil.queryTable(stationTable, "station");
        return panel;
    }

    // 创建客户信息面板
    private JPanel createCustomerPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // 初始化客户信息表
        String[] columnNames = {"客户编号", "姓名", "联系方式", "注册时间"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        customerTable = new JTable(model);

        JButton modifyBtn = new JButton("修改客户信息");
        modifyBtn.addActionListener(e -> modifyUtil.modifyCustomer(customerTable));

        JScrollPane scrollPane = new JScrollPane(customerTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 刷新按钮
        JButton refreshBtn = new JButton("刷新客户列表");
        refreshBtn.addActionListener(e -> queryUtil.queryTable(customerTable, "customer"));

        // 添加删除按钮
        JButton deleteBtn = new JButton("删除客户");
        deleteBtn.addActionListener(e -> {
            int selectedRow = customerTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "请选择要删除的客户", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String customerId = (String) customerTable.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "确认要删除客户 " + customerId + " 吗？",
                    "确认删除",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (deleteUtil.deleteCustomer(customerId)) {
                    queryUtil.queryTable(customerTable, "customer"); // 刷新列表
                }
            }
        });

        // 为按钮添加图标
        service.IconUtils.addIconToButton(modifyBtn, "/view/修改.png");
        service.IconUtils.addIconToButton(deleteBtn, "/view/删除.png");
        service.IconUtils.addIconToButton(refreshBtn, "/view/刷新.png");

        JPanel btnPanel = new JPanel();
        btnPanel.add(refreshBtn);
        btnPanel.add(modifyBtn); // 将修改按钮添加到面板
        btnPanel.add(deleteBtn); // 将删除按钮添加到按钮面板
        panel.add(btnPanel, BorderLayout.SOUTH);

        // 初始化客户信息
        queryUtil.queryTable(customerTable, "customer");

        return panel;
    }

    // 创建员工信息面板
    private JPanel createEmployeePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // 初始化员工信息表
        String[] columnNames = {"员工编号", "姓名", "职位"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        employeeTable = new JTable(model);

        JButton modifyBtn = new JButton("修改员工信息");
        modifyBtn.addActionListener(e -> modifyUtil.modifyEmployee(employeeTable));

        JScrollPane scrollPane = new JScrollPane(employeeTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 刷新按钮
        JButton refreshBtn = new JButton("刷新员工列表");
        refreshBtn.addActionListener(e -> queryUtil.queryTable(employeeTable, "employee"));

        // 添加按钮
        JButton addBtn = new JButton("添加员工");
        addBtn.addActionListener(e -> {
            addUtil.showEmployeeForm();
            queryUtil.queryTable(employeeTable, "employee"); // 刷新列表
        });

        // 为按钮添加图标
        service.IconUtils.addIconToButton(addBtn, "/view/增加.png");
        service.IconUtils.addIconToButton(modifyBtn, "/view/修改.png");
        service.IconUtils.addIconToButton(refreshBtn, "/view/刷新.png");

        JPanel btnPanel = new JPanel();
        btnPanel.add(refreshBtn);
        btnPanel.add(addBtn);  // 添加添加员工按钮
        btnPanel.add(modifyBtn); // 将修改按钮添加到面板
        panel.add(btnPanel, BorderLayout.SOUTH);

        // 初始化员工信息
        queryUtil.queryTable(employeeTable, "employee");

        return panel;
    }

    private void connectToDatabase() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            connection = DriverManager.getConnection(
                    "jdbc:sqlserver://localhost:1433;databaseName=Car;trustServerCertificate=true",
                    "sa", "tangyuan999");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "数据库连接失败: " + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
            // 关闭窗口
            dispose();
        }
    }
}