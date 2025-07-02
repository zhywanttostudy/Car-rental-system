package view;

import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import entity.Customer;
import basis.Connect;
import basis.DBUtil;
import service.OrderPaymentService;
import service.VehicleReturnService;
import service.VehicleRentalService;

public class CustomerFrame extends JFrame {
    private Customer customer;
    private JTabbedPane tabbedPane;
    private QueryUtil queryUtil;
    private Connection connection;
    private VehicleRentalService rentalService;
    private OrderPaymentService paymentService;
    private VehicleReturnService returnService;

    // 所有车辆表格
    private JTable allVehicleTable;
    private JButton refreshAllBtn, rentAllBtn;

    // 可租赁车辆表格
    private JTable availableVehicleTable;
    private JButton refreshAvailableBtn, rentAvailableBtn;

    // 我的订单表格
    private JTable orderTable;
    private JButton refreshOrderBtn, returnVehicleBtn, payOrderBtn;

    // 查询面板
    private VehicleQueryPanel queryPanel;

    public CustomerFrame(Customer customer) {
        this.customer = customer;
        setTitle("客户系统 - " + customer.getUserId());
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 连接数据库
        try {
            connection = Connect.getConnection();
            queryUtil = new QueryUtil(connection);
            rentalService = new VehicleRentalService(this, connection);
            paymentService = new OrderPaymentService(this, connection);
            returnService = new VehicleReturnService(this, connection);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "数据库连接失败: " + e.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        // 初始化UI
        initComponents();
        setupLayout();
        setupListeners();

        // 加载初始数据
        loadAllVehicles();
        loadAvailableVehicles();
        loadMyOrders();

        setVisible(true);
    }

    // 在initComponents方法中，为按钮添加图标
    private void initComponents() {
        // 初始化表格
        allVehicleTable = createTable();
        availableVehicleTable = createTable();
        orderTable = createTable();
        // 初始化按钮
        refreshAllBtn = new JButton("刷新");
        rentAllBtn = new JButton("租赁");
        refreshAvailableBtn = new JButton("刷新");
        rentAvailableBtn = new JButton("租赁");
        refreshOrderBtn = new JButton("刷新订单列表");
        returnVehicleBtn = new JButton("归还");
        payOrderBtn = new JButton("支付订单");

        // 为按钮添加图标
        service.IconUtils.addIconToButton(refreshAllBtn, "/view/刷新.png");
        service.IconUtils.addIconToButton(rentAllBtn, "/view/租赁.png");
        service.IconUtils.addIconToButton(refreshAvailableBtn, "/view/刷新.png");
        service.IconUtils.addIconToButton(rentAvailableBtn, "/view/租赁.png");
        service.IconUtils.addIconToButton(refreshOrderBtn, "/view/刷新.png");
        service.IconUtils.addIconToButton(returnVehicleBtn, "/view/还车.png");
        service.IconUtils.addIconToButton(payOrderBtn, "/view/支付.png");

        // 初始化查询面板
        queryPanel = new VehicleQueryPanel(queryUtil, allVehicleTable, connection);
    }

    private JTable createTable() {
        JTable table = new JTable();
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        return table;
    }

    private void setupLayout() {
        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout());

        // 欢迎信息
        JLabel welcomeLabel = new JLabel("欢迎 " + customer.getUserId() + " 进入客户系统！");
        welcomeLabel.setFont(new Font("宋体", Font.BOLD, 16));
        welcomeLabel.setHorizontalAlignment(JLabel.CENTER);
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        mainPanel.add(welcomeLabel, BorderLayout.NORTH);

        // 所有车辆面板
        JPanel allVehiclePanel = new JPanel(new BorderLayout());
        allVehiclePanel.add(new JScrollPane(allVehicleTable), BorderLayout.CENTER);

        JPanel allBtnPanel = new JPanel();
        allBtnPanel.add(refreshAllBtn);
        allBtnPanel.add(rentAllBtn);
        allVehiclePanel.add(allBtnPanel, BorderLayout.SOUTH);
        allVehiclePanel.add(queryPanel, BorderLayout.NORTH);

        // 可租赁车辆面板
        JPanel availableVehiclePanel = new JPanel(new BorderLayout());
        availableVehiclePanel.add(new JScrollPane(availableVehicleTable), BorderLayout.CENTER);

        JPanel availableBtnPanel = new JPanel();
        availableBtnPanel.add(refreshAvailableBtn);
        availableBtnPanel.add(rentAvailableBtn);
        availableVehiclePanel.add(availableBtnPanel, BorderLayout.SOUTH);

        // 我的订单面板
        JPanel orderPanel = new JPanel(new BorderLayout());
        orderPanel.add(new JScrollPane(orderTable), BorderLayout.CENTER);

        JPanel orderBtnPanel = new JPanel();
        orderBtnPanel.add(refreshOrderBtn);
        orderBtnPanel.add(returnVehicleBtn);
        orderBtnPanel.add(payOrderBtn);
        orderPanel.add(orderBtnPanel, BorderLayout.SOUTH);

        // 添加标签页
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("所有车辆", allVehiclePanel);
        tabbedPane.addTab("可租赁车辆", availableVehiclePanel);
        tabbedPane.addTab("我的订单", orderPanel);

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // 底部按钮面板
        JPanel buttonPanel = new JPanel();
        JButton logoutBtn = new JButton("退出登录");
        logoutBtn.addActionListener(e -> {
            dispose();
            new MainFrame();
        });
        buttonPanel.add(logoutBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void setupListeners() {
        // 刷新按钮事件
        refreshAllBtn.addActionListener(e -> {
            loadAllVehicles();
        });
        refreshAvailableBtn.addActionListener(e -> {
            loadAvailableVehicles();
        });
        refreshOrderBtn.addActionListener(e -> {
            loadMyOrders();
        });

        // 租赁按钮事件
        rentAllBtn.addActionListener(e -> {
            rentalService.rentVehicle(allVehicleTable, customer.getUserId());
            loadAllVehicles();
            loadAvailableVehicles();
            loadMyOrders();
        });
        rentAvailableBtn.addActionListener(e -> {
            rentalService.rentVehicle(availableVehicleTable, customer.getUserId());
            loadAllVehicles();
            loadAvailableVehicles();
            loadMyOrders();
        });

        // 归还按钮事件
        returnVehicleBtn.addActionListener(e -> {
            returnVehicle();
        });

        // 支付订单按钮事件
        payOrderBtn.addActionListener(e -> {
            payOrder();
        });
    }

    // 加载所有车辆
    private void loadAllVehicles() {
        if (queryUtil != null) {
            queryUtil.queryTable(allVehicleTable, "vehicle");
        }
    }

    // 加载可租赁车辆
    private void loadAvailableVehicles() {
        if (queryUtil != null) {
            String sql = "SELECT * FROM vehicle WHERE Vstatus='待租'";
            queryUtil.queryAndFillTable(availableVehicleTable, sql, "vehicle");
        }
    }

    // 加载我的订单
    private void loadMyOrders() {
        if (queryUtil != null) {
            String sql = "SELECT * FROM orders WHERE Cno='" + customer.getUserId() + "'";
            queryUtil.queryAndFillTable(orderTable, sql, "orders");
        }
    }

    // 支付订单
    private void payOrder() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择要支付的订单",
                    "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String orderNo = orderTable.getValueAt(selectedRow, 0).toString();
        boolean success = paymentService.payOrder(orderNo);
        if (success) {
            loadMyOrders();
        }
    }

    // 归还车辆
    private void returnVehicle() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择要归还的订单",
                    "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String orderNo = orderTable.getValueAt(selectedRow, 0).toString();
        String vehicleNo = orderTable.getValueAt(selectedRow, 2).toString();

        JDateChooser returnDateChooser = new JDateChooser();
        JPanel panel = new JPanel(new GridLayout(1, 2));
        panel.add(new JLabel("归还日期:"));
        panel.add(returnDateChooser);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "归还 - " + vehicleNo,
                JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            Date returnDate = returnDateChooser.getDate();

            if (returnDate == null) {
                JOptionPane.showMessageDialog(this, "日期不能为空",
                        "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String returnDateStr = sdf.format(returnDate);
            boolean success = returnService.returnVehicle(orderNo, vehicleNo, returnDateStr);
            if (success) {
                loadMyOrders();
                loadAvailableVehicles();
            }
        }
    }
}