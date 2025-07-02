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
    private JButton returnVehicleWithDateBtn; // ����������ť

    public CustomerFrameUIInitializer(Customer customer) {
        this.customer = customer;
    }

    public JPanel initializeUI() {
        // ��ʼ�����
        allVehicleTable = createTable();
        availableVehicleTable = createTable();
        orderTable = createTable();

        // ��ʼ����ť
        refreshAllBtn = new JButton("ˢ��");
        rentAllBtn = new JButton("�⳵");
        refreshAvailableBtn = new JButton("ˢ��");
        rentAvailableBtn = new JButton("�⳵");
        refreshOrderBtn = new JButton("ˢ�¶����б�");
        returnVehicleBtn = new JButton("����");
        returnVehicleWithDateBtn = new JButton("�������ڻ���"); // ����������ť

        // �����
        JPanel mainPanel = new JPanel(new BorderLayout());

        // ��ӭ��Ϣ
        JLabel welcomeLabel = new JLabel("��ӭ " + customer.getUserId() + " ����ͻ�ϵͳ��");
        welcomeLabel.setFont(new Font("����", Font.BOLD, 16));
        welcomeLabel.setHorizontalAlignment(JLabel.CENTER);
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        mainPanel.add(welcomeLabel, BorderLayout.NORTH);

        // ��ǩҳ���
        tabbedPane = new JTabbedPane();

        // ���г������
        JPanel allVehiclePanel = new JPanel(new BorderLayout());
        allVehiclePanel.add(new JScrollPane(allVehicleTable), BorderLayout.CENTER);

        JPanel allBtnPanel = new JPanel();
        allBtnPanel.add(refreshAllBtn);
        allBtnPanel.add(rentAllBtn);
        allVehiclePanel.add(allBtnPanel, BorderLayout.SOUTH);

        // ���⳵�����
        JPanel availableVehiclePanel = new JPanel(new BorderLayout());
        availableVehiclePanel.add(new JScrollPane(availableVehicleTable), BorderLayout.CENTER);

        JPanel availableBtnPanel = new JPanel();
        availableBtnPanel.add(refreshAvailableBtn);
        availableBtnPanel.add(rentAvailableBtn);
        availableVehiclePanel.add(availableBtnPanel, BorderLayout.SOUTH);

        // �������
        JPanel orderPanel = new JPanel(new BorderLayout());
        orderPanel.add(new JScrollPane(orderTable), BorderLayout.CENTER);

        JPanel orderBtnPanel = new JPanel();
        orderBtnPanel.add(refreshOrderBtn);
        orderBtnPanel.add(returnVehicleBtn);
        orderBtnPanel.add(returnVehicleWithDateBtn); // ����������ť
        orderPanel.add(orderBtnPanel, BorderLayout.SOUTH);

        // ��ӱ�ǩҳ
        tabbedPane.addTab("���г���", allVehiclePanel);
        tabbedPane.addTab("���⳵��", availableVehiclePanel);
        tabbedPane.addTab("�ҵĶ���", orderPanel);

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // �ײ���ť���
        JPanel buttonPanel = new JPanel();
        JButton logoutBtn = new JButton("�˳���¼");
        logoutBtn.addActionListener(e -> {
            // ʵ���˳���¼�߼�
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
