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

    // ���г������
    private JTable allVehicleTable;
    private JButton refreshAllBtn, rentAllBtn;

    // �����޳������
    private JTable availableVehicleTable;
    private JButton refreshAvailableBtn, rentAvailableBtn;

    // �ҵĶ������
    private JTable orderTable;
    private JButton refreshOrderBtn, returnVehicleBtn, payOrderBtn;

    // ��ѯ���
    private VehicleQueryPanel queryPanel;

    public CustomerFrame(Customer customer) {
        this.customer = customer;
        setTitle("�ͻ�ϵͳ - " + customer.getUserId());
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // �������ݿ�
        try {
            connection = Connect.getConnection();
            queryUtil = new QueryUtil(connection);
            rentalService = new VehicleRentalService(this, connection);
            paymentService = new OrderPaymentService(this, connection);
            returnService = new VehicleReturnService(this, connection);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "���ݿ�����ʧ��: " + e.getMessage(),
                    "����", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        // ��ʼ��UI
        initComponents();
        setupLayout();
        setupListeners();

        // ���س�ʼ����
        loadAllVehicles();
        loadAvailableVehicles();
        loadMyOrders();

        setVisible(true);
    }

    // ��initComponents�����У�Ϊ��ť���ͼ��
    private void initComponents() {
        // ��ʼ�����
        allVehicleTable = createTable();
        availableVehicleTable = createTable();
        orderTable = createTable();
        // ��ʼ����ť
        refreshAllBtn = new JButton("ˢ��");
        rentAllBtn = new JButton("����");
        refreshAvailableBtn = new JButton("ˢ��");
        rentAvailableBtn = new JButton("����");
        refreshOrderBtn = new JButton("ˢ�¶����б�");
        returnVehicleBtn = new JButton("�黹");
        payOrderBtn = new JButton("֧������");

        // Ϊ��ť���ͼ��
        service.IconUtils.addIconToButton(refreshAllBtn, "/view/ˢ��.png");
        service.IconUtils.addIconToButton(rentAllBtn, "/view/����.png");
        service.IconUtils.addIconToButton(refreshAvailableBtn, "/view/ˢ��.png");
        service.IconUtils.addIconToButton(rentAvailableBtn, "/view/����.png");
        service.IconUtils.addIconToButton(refreshOrderBtn, "/view/ˢ��.png");
        service.IconUtils.addIconToButton(returnVehicleBtn, "/view/����.png");
        service.IconUtils.addIconToButton(payOrderBtn, "/view/֧��.png");

        // ��ʼ����ѯ���
        queryPanel = new VehicleQueryPanel(queryUtil, allVehicleTable, connection);
    }

    private JTable createTable() {
        JTable table = new JTable();
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        return table;
    }

    private void setupLayout() {
        // �����
        JPanel mainPanel = new JPanel(new BorderLayout());

        // ��ӭ��Ϣ
        JLabel welcomeLabel = new JLabel("��ӭ " + customer.getUserId() + " ����ͻ�ϵͳ��");
        welcomeLabel.setFont(new Font("����", Font.BOLD, 16));
        welcomeLabel.setHorizontalAlignment(JLabel.CENTER);
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        mainPanel.add(welcomeLabel, BorderLayout.NORTH);

        // ���г������
        JPanel allVehiclePanel = new JPanel(new BorderLayout());
        allVehiclePanel.add(new JScrollPane(allVehicleTable), BorderLayout.CENTER);

        JPanel allBtnPanel = new JPanel();
        allBtnPanel.add(refreshAllBtn);
        allBtnPanel.add(rentAllBtn);
        allVehiclePanel.add(allBtnPanel, BorderLayout.SOUTH);
        allVehiclePanel.add(queryPanel, BorderLayout.NORTH);

        // �����޳������
        JPanel availableVehiclePanel = new JPanel(new BorderLayout());
        availableVehiclePanel.add(new JScrollPane(availableVehicleTable), BorderLayout.CENTER);

        JPanel availableBtnPanel = new JPanel();
        availableBtnPanel.add(refreshAvailableBtn);
        availableBtnPanel.add(rentAvailableBtn);
        availableVehiclePanel.add(availableBtnPanel, BorderLayout.SOUTH);

        // �ҵĶ������
        JPanel orderPanel = new JPanel(new BorderLayout());
        orderPanel.add(new JScrollPane(orderTable), BorderLayout.CENTER);

        JPanel orderBtnPanel = new JPanel();
        orderBtnPanel.add(refreshOrderBtn);
        orderBtnPanel.add(returnVehicleBtn);
        orderBtnPanel.add(payOrderBtn);
        orderPanel.add(orderBtnPanel, BorderLayout.SOUTH);

        // ��ӱ�ǩҳ
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("���г���", allVehiclePanel);
        tabbedPane.addTab("�����޳���", availableVehiclePanel);
        tabbedPane.addTab("�ҵĶ���", orderPanel);

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // �ײ���ť���
        JPanel buttonPanel = new JPanel();
        JButton logoutBtn = new JButton("�˳���¼");
        logoutBtn.addActionListener(e -> {
            dispose();
            new MainFrame();
        });
        buttonPanel.add(logoutBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void setupListeners() {
        // ˢ�°�ť�¼�
        refreshAllBtn.addActionListener(e -> {
            loadAllVehicles();
        });
        refreshAvailableBtn.addActionListener(e -> {
            loadAvailableVehicles();
        });
        refreshOrderBtn.addActionListener(e -> {
            loadMyOrders();
        });

        // ���ް�ť�¼�
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

        // �黹��ť�¼�
        returnVehicleBtn.addActionListener(e -> {
            returnVehicle();
        });

        // ֧��������ť�¼�
        payOrderBtn.addActionListener(e -> {
            payOrder();
        });
    }

    // �������г���
    private void loadAllVehicles() {
        if (queryUtil != null) {
            queryUtil.queryTable(allVehicleTable, "vehicle");
        }
    }

    // ���ؿ����޳���
    private void loadAvailableVehicles() {
        if (queryUtil != null) {
            String sql = "SELECT * FROM vehicle WHERE Vstatus='����'";
            queryUtil.queryAndFillTable(availableVehicleTable, sql, "vehicle");
        }
    }

    // �����ҵĶ���
    private void loadMyOrders() {
        if (queryUtil != null) {
            String sql = "SELECT * FROM orders WHERE Cno='" + customer.getUserId() + "'";
            queryUtil.queryAndFillTable(orderTable, sql, "orders");
        }
    }

    // ֧������
    private void payOrder() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "��ѡ��Ҫ֧���Ķ���",
                    "��ʾ", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String orderNo = orderTable.getValueAt(selectedRow, 0).toString();
        boolean success = paymentService.payOrder(orderNo);
        if (success) {
            loadMyOrders();
        }
    }

    // �黹����
    private void returnVehicle() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "��ѡ��Ҫ�黹�Ķ���",
                    "��ʾ", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String orderNo = orderTable.getValueAt(selectedRow, 0).toString();
        String vehicleNo = orderTable.getValueAt(selectedRow, 2).toString();

        JDateChooser returnDateChooser = new JDateChooser();
        JPanel panel = new JPanel(new GridLayout(1, 2));
        panel.add(new JLabel("�黹����:"));
        panel.add(returnDateChooser);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "�黹 - " + vehicleNo,
                JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            Date returnDate = returnDateChooser.getDate();

            if (returnDate == null) {
                JOptionPane.showMessageDialog(this, "���ڲ���Ϊ��",
                        "����", JOptionPane.ERROR_MESSAGE);
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