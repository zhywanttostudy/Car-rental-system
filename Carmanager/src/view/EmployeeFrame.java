// Ա��ϵͳ
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
    private DeleteUtil deleteUtil; // ����ɾ�����ܵĹ�����

    public EmployeeFrame(Employee employee) {
        this.employee = employee;
        setTitle("Ա������ϵͳ - " + employee.getUserId());
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // �������ݿⲢ��ʼ��queryUtil
        connectToDatabase();
        if (connection != null) {
            queryUtil = new QueryUtil(connection);
            addUtil = new AddEntityUtil(connection, this);
            modifyUtil = new DataModifyUtil(connection, this, queryUtil);
            deleteUtil = new DeleteUtil(connection, this); // ��ʼ��DeleteUtil

            // �����
            JPanel mainPanel = new JPanel(new BorderLayout());

            // ��ӭ��Ϣ
            JLabel welcomeLabel = new JLabel("��ӭ " + employee.getUserId() + " ����Ա������ϵͳ��");
            welcomeLabel.setFont(new Font("����", Font.BOLD, 16));
            welcomeLabel.setHorizontalAlignment(JLabel.CENTER);
            welcomeLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            mainPanel.add(welcomeLabel, BorderLayout.NORTH);

            // ��ǩҳ���
            tabbedPane = new JTabbedPane();
            tabbedPane.addTab("������Ϣ", createVehiclePanel());
            tabbedPane.addTab("������Ϣ", createOrderPanel());
            tabbedPane.addTab("վ����Ϣ", createStationPanel());
            tabbedPane.addTab("�ͻ���Ϣ", createCustomerPanel());
            tabbedPane.addTab("Ա����Ϣ", createEmployeePanel());

            mainPanel.add(tabbedPane, BorderLayout.CENTER);

            // �ײ���ť���
            JPanel buttonPanel = new JPanel();
            JButton logoutBtn = new JButton("�˳���¼");
            logoutBtn.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "ȷ��Ҫ�˳���¼��",
                        "ȷ���˳�",
                        JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    dispose();
                    try {
                        // ȷ��MainFrame�����
                        Class.forName("view.MainFrame");
                        java.lang.reflect.Constructor<?> constructor = Class.forName("view.MainFrame").getConstructor();
                        constructor.newInstance();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(this,
                                "���ص�¼����ʧ��: " + ex.getMessage(),
                                "����",
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

    // �޸�createVehiclePanel������Ϊ��ť���ͼ��
    private JPanel createVehiclePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        // ��ʼ��������Ϣ��
        String[] columnNames = {"�������", "��������", "�ͺ�", "�۸�", "״̬", "վ������"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        vehicleTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(vehicleTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // ���ܰ�ť
        JPanel btnPanel = new JPanel();
        JButton refreshBtn = new JButton("ˢ�³����б�");
        JButton addBtn = new JButton("��ӳ���");
        JButton modifyBtn = new JButton("�޸ĳ�����Ϣ");
        JButton deleteBtn = new JButton("ɾ������");

        // Ϊ��ť���ͼ��
        service.IconUtils.addIconToButton(refreshBtn, "/view/ˢ��.png");
        service.IconUtils.addIconToButton(addBtn, "/view/����.png");
        service.IconUtils.addIconToButton(modifyBtn, "/view/�޸�.png");
        service.IconUtils.addIconToButton(deleteBtn, "/view/ɾ��.png");

        // ��Ӱ�ť�¼�
        refreshBtn.addActionListener(e -> queryUtil.queryTable(vehicleTable, "vehicle"));
        addBtn.addActionListener(e -> {
            addUtil.showVehicleForm();
            queryUtil.queryTable(vehicleTable, "vehicle");
        });
        modifyBtn.addActionListener(e -> modifyUtil.modifyVehicle(vehicleTable));
        deleteBtn.addActionListener(e -> {
            int selectedRow = vehicleTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "��ѡ��Ҫɾ���ĳ���", "��ʾ", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String vehicleId = (String) vehicleTable.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "ȷ��Ҫɾ������ " + vehicleId + " ��",
                    "ȷ��ɾ��",
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

        // ��ӳ�����ѯ���
        VehicleQueryPanel queryPanel = new VehicleQueryPanel(queryUtil, vehicleTable, connection);
        panel.add(queryPanel, BorderLayout.NORTH);

        // ��ʼ��������Ϣ
        queryUtil.queryTable(vehicleTable, "vehicle");
        return panel;
    }


    // ��createOrderPanel������Ϊ������尴ť���ͼ��
    private JPanel createOrderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        // ��ʼ��������Ϣ�� '����״̬' �ֶ�
        String[] columnNames = {"�������", "�ͻ����", "�������", "��ʼ����", "�黹����", "����״̬", "����"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        orderTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(orderTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // ����ͳ�Ʋ�ѯ���Ͳ�ѯ��ť
        JPanel queryPanel = new JPanel();
        JDateChooser startDateChooser = new JDateChooser();
        JDateChooser endDateChooser = new JDateChooser();
        JButton queryBtn = new JButton("ͳ�Ʒ���");
        
        // Ϊͳ�ư�ť���ͼ��
        service.IconUtils.addIconToButton(queryBtn, "/view/ˢ��.png");
        
        queryPanel.add(new JLabel("��ʼ����:"));
        queryPanel.add(startDateChooser);
        queryPanel.add(new JLabel("��������:"));
        queryPanel.add(endDateChooser);
        queryPanel.add(queryBtn);
        panel.add(queryPanel, BorderLayout.NORTH);
        
        // ͳ�Ʒ��ð�ť�ĵ���¼�����
        queryBtn.addActionListener(e -> {
            Date startDate = startDateChooser.getDate();
            Date endDate = endDateChooser.getDate();
            // ��������Ƿ�Ϊ��
            if (startDate == null || endDate == null) {
                JOptionPane.showMessageDialog(this, "���ڲ���Ϊ�գ�������ѡ��", "����ѡ�����", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // ��鿪ʼ�����Ƿ����ڽ�������
            if (startDate.after(endDate)) {
                JOptionPane.showMessageDialog(this, "��ʼ���ڲ������ڽ������ڣ�������ѡ��", "����ѡ�����", JOptionPane.ERROR_MESSAGE);
                return;
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String startDateStr = sdf.format(startDate);
            String endDateStr = sdf.format(endDate);
            try {
                RentStatisticsService statisticsService = new RentStatisticsService(connection);
                double totalFee = statisticsService.calculateRentFee(startDateStr, endDateStr);
                JOptionPane.showMessageDialog(this, "��ʱ��ε������ܷ���: " + totalFee + " Ԫ", "ͳ�ƽ��", JOptionPane.INFORMATION_MESSAGE);
            } catch (ParseException | SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "ͳ�Ʒ���ʱ��������: " + ex.getMessage(), "����", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        // ˢ�°�ť
        JButton refreshBtn = new JButton("ˢ�¶����б�");
        
        // Ϊˢ�°�ť���ͼ��
        service.IconUtils.addIconToButton(refreshBtn, "/view/ˢ��.png");
        
        refreshBtn.addActionListener(e -> queryUtil.queryTable(orderTable, "orders"));
        JPanel btnPanel = new JPanel();
        btnPanel.add(refreshBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);
        
        // ��ʼ��������Ϣ
        queryUtil.queryTable(orderTable, "orders");
        return panel;
    }

    // ��createStationPanel������Ϊվ����尴ť���ͼ��
    private JPanel createStationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        // ��ʼ��վ����Ϣ��
        String[] columnNames = {"վ����", "վ������", "��ַ"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        stationTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(stationTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // ���վ�㰴ť
        JButton addStationBtn = new JButton("���վ��");
        JButton modifyBtn = new JButton("�޸�վ����Ϣ");
        JButton deleteBtn = new JButton("ɾ��վ��");
        JButton refreshBtn = new JButton("ˢ��վ���б�");
        
        // Ϊ��ť���ͼ��
        service.IconUtils.addIconToButton(addStationBtn, "/view/����.png");
        service.IconUtils.addIconToButton(modifyBtn, "/view/�޸�.png");
        service.IconUtils.addIconToButton(deleteBtn, "/view/ɾ��.png");
        service.IconUtils.addIconToButton(refreshBtn, "/view/ˢ��.png");
        
        addStationBtn.addActionListener(e -> {
            addUtil.showStationForm();
            queryUtil.queryTable(stationTable, "station");
        });
        modifyBtn.addActionListener(e -> modifyUtil.modifyStation(stationTable));
        deleteBtn.addActionListener(e -> {
            int selectedRow = stationTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "��ѡ��Ҫɾ����վ��", "��ʾ", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String stationId = (String) stationTable.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "ȷ��Ҫɾ��վ�� " + stationId + " ��",
                    "ȷ��ɾ��",
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
        
        // ���վ���ѯ���
        StationQueryPanel queryPanel = new StationQueryPanel(queryUtil, stationTable, connection);
        panel.add(queryPanel, BorderLayout.NORTH);
        
        // ��ʼ��վ����Ϣ
        queryUtil.queryTable(stationTable, "station");
        return panel;
    }

    // �����ͻ���Ϣ���
    private JPanel createCustomerPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // ��ʼ���ͻ���Ϣ��
        String[] columnNames = {"�ͻ����", "����", "��ϵ��ʽ", "ע��ʱ��"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        customerTable = new JTable(model);

        JButton modifyBtn = new JButton("�޸Ŀͻ���Ϣ");
        modifyBtn.addActionListener(e -> modifyUtil.modifyCustomer(customerTable));

        JScrollPane scrollPane = new JScrollPane(customerTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // ˢ�°�ť
        JButton refreshBtn = new JButton("ˢ�¿ͻ��б�");
        refreshBtn.addActionListener(e -> queryUtil.queryTable(customerTable, "customer"));

        // ���ɾ����ť
        JButton deleteBtn = new JButton("ɾ���ͻ�");
        deleteBtn.addActionListener(e -> {
            int selectedRow = customerTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "��ѡ��Ҫɾ���Ŀͻ�", "��ʾ", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String customerId = (String) customerTable.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "ȷ��Ҫɾ���ͻ� " + customerId + " ��",
                    "ȷ��ɾ��",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (deleteUtil.deleteCustomer(customerId)) {
                    queryUtil.queryTable(customerTable, "customer"); // ˢ���б�
                }
            }
        });

        // Ϊ��ť���ͼ��
        service.IconUtils.addIconToButton(modifyBtn, "/view/�޸�.png");
        service.IconUtils.addIconToButton(deleteBtn, "/view/ɾ��.png");
        service.IconUtils.addIconToButton(refreshBtn, "/view/ˢ��.png");

        JPanel btnPanel = new JPanel();
        btnPanel.add(refreshBtn);
        btnPanel.add(modifyBtn); // ���޸İ�ť��ӵ����
        btnPanel.add(deleteBtn); // ��ɾ����ť��ӵ���ť���
        panel.add(btnPanel, BorderLayout.SOUTH);

        // ��ʼ���ͻ���Ϣ
        queryUtil.queryTable(customerTable, "customer");

        return panel;
    }

    // ����Ա����Ϣ���
    private JPanel createEmployeePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // ��ʼ��Ա����Ϣ��
        String[] columnNames = {"Ա�����", "����", "ְλ"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        employeeTable = new JTable(model);

        JButton modifyBtn = new JButton("�޸�Ա����Ϣ");
        modifyBtn.addActionListener(e -> modifyUtil.modifyEmployee(employeeTable));

        JScrollPane scrollPane = new JScrollPane(employeeTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // ˢ�°�ť
        JButton refreshBtn = new JButton("ˢ��Ա���б�");
        refreshBtn.addActionListener(e -> queryUtil.queryTable(employeeTable, "employee"));

        // ��Ӱ�ť
        JButton addBtn = new JButton("���Ա��");
        addBtn.addActionListener(e -> {
            addUtil.showEmployeeForm();
            queryUtil.queryTable(employeeTable, "employee"); // ˢ���б�
        });

        // Ϊ��ť���ͼ��
        service.IconUtils.addIconToButton(addBtn, "/view/����.png");
        service.IconUtils.addIconToButton(modifyBtn, "/view/�޸�.png");
        service.IconUtils.addIconToButton(refreshBtn, "/view/ˢ��.png");

        JPanel btnPanel = new JPanel();
        btnPanel.add(refreshBtn);
        btnPanel.add(addBtn);  // ������Ա����ť
        btnPanel.add(modifyBtn); // ���޸İ�ť��ӵ����
        panel.add(btnPanel, BorderLayout.SOUTH);

        // ��ʼ��Ա����Ϣ
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
                    "���ݿ�����ʧ��: " + e.getMessage(),
                    "����",
                    JOptionPane.ERROR_MESSAGE);
            // �رմ���
            dispose();
        }
    }
}