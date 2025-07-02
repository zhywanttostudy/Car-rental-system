package view;

import javax.swing.*;
import java.awt.*;
import entity.Customer;

public class CustomerFrameUIInitializer {
    private Customer customer;
    private JTabbedPane tabbedPane;
    private JTable allVehicleTable;
    private JButton refreshAllBtn, rentAllBtn;
    private JTable availableVehicleTable;
    private JButton refreshAvailableBtn, rentAvailableBtn;
    private JTable orderTable;
    private JButton refreshOrderBtn, returnVehicleBtn;
    private JButton returnVehicleWithDateBtn; // 新增还车按钮

    public CustomerFrameUIInitializer(Customer customer) {
        this.customer = customer;
    }

    public JPanel initializeUI() {
        // 初始化表格
        allVehicleTable = createTable();
        availableVehicleTable = createTable();
        orderTable = createTable();

        // 初始化按钮
        refreshAllBtn = new JButton("刷新");
        rentAllBtn = new JButton("租车");
        refreshAvailableBtn = new JButton("刷新");
        rentAvailableBtn = new JButton("租车");
        refreshOrderBtn = new JButton("刷新订单列表");
        returnVehicleBtn = new JButton("还车");
        returnVehicleWithDateBtn = new JButton("输入日期还车"); // 新增还车按钮

        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout());

        // 欢迎信息
        JLabel welcomeLabel = new JLabel("欢迎 " + customer.getUserId() + " 进入客户系统！");
        welcomeLabel.setFont(new Font("宋体", Font.BOLD, 16));
        welcomeLabel.setHorizontalAlignment(JLabel.CENTER);
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        mainPanel.add(welcomeLabel, BorderLayout.NORTH);

        // 标签页面板
        tabbedPane = new JTabbedPane();

        // 所有车辆面板
        JPanel allVehiclePanel = new JPanel(new BorderLayout());
        allVehiclePanel.add(new JScrollPane(allVehicleTable), BorderLayout.CENTER);

        JPanel allBtnPanel = new JPanel();
        allBtnPanel.add(refreshAllBtn);
        allBtnPanel.add(rentAllBtn);
        allVehiclePanel.add(allBtnPanel, BorderLayout.SOUTH);

        // 可租车辆面板
        JPanel availableVehiclePanel = new JPanel(new BorderLayout());
        availableVehiclePanel.add(new JScrollPane(availableVehicleTable), BorderLayout.CENTER);

        JPanel availableBtnPanel = new JPanel();
        availableBtnPanel.add(refreshAvailableBtn);
        availableBtnPanel.add(rentAvailableBtn);
        availableVehiclePanel.add(availableBtnPanel, BorderLayout.SOUTH);

        // 订单面板
        JPanel orderPanel = new JPanel(new BorderLayout());
        orderPanel.add(new JScrollPane(orderTable), BorderLayout.CENTER);

        JPanel orderBtnPanel = new JPanel();
        orderBtnPanel.add(refreshOrderBtn);
        orderBtnPanel.add(returnVehicleBtn);
        orderBtnPanel.add(returnVehicleWithDateBtn); // 新增还车按钮
        orderPanel.add(orderBtnPanel, BorderLayout.SOUTH);

        // 添加标签页
        tabbedPane.addTab("所有车辆", allVehiclePanel);
        tabbedPane.addTab("可租车辆", availableVehiclePanel);
        tabbedPane.addTab("我的订单", orderPanel);

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // 底部按钮面板
        JPanel buttonPanel = new JPanel();
        JButton logoutBtn = new JButton("退出登录");
        logoutBtn.addActionListener(e -> {
            // 实现退出登录逻辑
        });
        buttonPanel.add(logoutBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        return mainPanel;
    }

    private JTable createTable() {
        JTable table = new JTable();
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        return table;
    }

    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    public JTable getAllVehicleTable() {
        return allVehicleTable;
    }

    public JButton getRefreshAllBtn() {
        return refreshAllBtn;
    }

    public JButton getRentAllBtn() {
        return rentAllBtn;
    }

    public JTable getAvailableVehicleTable() {
        return availableVehicleTable;
    }

    public JButton getRefreshAvailableBtn() {
        return refreshAvailableBtn;
    }

    public JButton getRentAvailableBtn() {
        return rentAvailableBtn;
    }

    public JTable getOrderTable() {
        return orderTable;
    }

    public JButton getRefreshOrderBtn() {
        return refreshOrderBtn;
    }

    public JButton getReturnVehicleBtn() {
        return returnVehicleBtn;
    }

    public JButton getReturnVehicleWithDateBtn() {
        return returnVehicleWithDateBtn;
    }
}
