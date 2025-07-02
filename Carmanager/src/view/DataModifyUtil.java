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
 * �����޸Ĺ����� - ���й�������ʵ�����ݵ��޸Ĺ���
 */
public class DataModifyUtil {
    private Connection connection;
    private JFrame parentFrame;
    private QueryUtil queryUtil;

    // �й� 34 ��ʡ��������
    private static final String[] PROVINCES = {
            "������", "�����", "�Ϻ���", "������",
            "�ӱ�ʡ", "ɽ��ʡ", "����ʡ", "����ʡ",
            "������ʡ", "����ʡ", "�㽭ʡ", "����ʡ",
            "����ʡ", "����ʡ", "ɽ��ʡ", "����ʡ",
            "����ʡ", "����ʡ", "�㶫ʡ", "����ʡ",
            "�Ĵ�ʡ", "����ʡ", "����ʡ", "����ʡ",
            "����ʡ", "�ຣʡ", "̨��ʡ", "���ɹ�������",
            "����׳��������", "����������", "���Ļ���������",
            "�½�ά���������", "����ر�������", "�����ر�������"
    };

    public DataModifyUtil(Connection connection, JFrame parentFrame, QueryUtil queryUtil) {
        this.connection = connection;
        this.parentFrame = parentFrame;
        this.queryUtil = queryUtil;
    }

    /**
     * �޸ĳ������ݣ�����վ�����������߼���
     */
    public void modifyVehicle(JTable vehicleTable) {
        int selectedRow = vehicleTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(parentFrame, "����ѡ��Ҫ�޸ĵĳ�����¼",
                    "��ʾ", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ��ȡ��ǰ����
        DefaultTableModel model = (DefaultTableModel) vehicleTable.getModel();
        String vno = (String) model.getValueAt(selectedRow, 0);
        String vname = (String) model.getValueAt(selectedRow, 1);
        String vmodel = (String) model.getValueAt(selectedRow, 2);
        double vprice = Double.parseDouble(model.getValueAt(selectedRow, 3).toString());
        String vstatus = (String) model.getValueAt(selectedRow, 4);
        String oldSno = (String) model.getValueAt(selectedRow, 5); // ԭվ����

        // �����޸ĶԻ���
        JDialog dialog = new JDialog(parentFrame, "�޸ĳ�����Ϣ", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setLayout(new GridBagLayout());
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // �����
        JTextField vnoField = new JTextField(vno, 10);
        vnoField.setEditable(false);
        JTextField vnameField = new JTextField(vname, 10);
        JTextField vmodelField = new JTextField(vmodel, 10);
        JTextField vpriceField = new JTextField(String.valueOf(vprice), 10);
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"����", "����"});
        statusCombo.setSelectedItem(vstatus);
        JTextField snoField = new JTextField(oldSno, 10); // ��ʼ��ʾԭվ����

        // ���������Ի���
        addComponent(dialog, new JLabel("�������:"), gbc, 0, 0);
        addComponent(dialog, vnoField, gbc, 1, 0);
        addComponent(dialog, new JLabel("��������:"), gbc, 0, 1);
        addComponent(dialog, vnameField, gbc, 1, 1);
        addComponent(dialog, new JLabel("����:"), gbc, 0, 2);
        addComponent(dialog, vmodelField, gbc, 1, 2);
        addComponent(dialog, new JLabel("�����:"), gbc, 0, 3);
        addComponent(dialog, vpriceField, gbc, 1, 3);
        addComponent(dialog, new JLabel("״̬:"), gbc, 0, 4);
        addComponent(dialog, statusCombo, gbc, 1, 4);
        addComponent(dialog, new JLabel("����վ����:"), gbc, 0, 5);
        addComponent(dialog, snoField, gbc, 1, 5);

        // ��ť���
        JPanel btnPanel = new JPanel();
        JButton confirmBtn = new JButton("ȷ���޸�");
        JButton cancelBtn = new JButton("ȡ��");

        confirmBtn.addActionListener(e -> {
            try {
                String newVname = vnameField.getText().trim();
                String newVmodel = vmodelField.getText().trim();
                double newVprice = Double.parseDouble(vpriceField.getText().trim());
                String newVstatus = (String) statusCombo.getSelectedItem();
                String newSno = snoField.getText().trim();

                // ��֤����
                if (newVname.isEmpty() || newSno.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "�������ƺ�վ���Ų���Ϊ��",
                            "�������", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // ��������
                connection.setAutoCommit(false);

                try {
                    // 1. ���³�������վ��
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
                            throw new SQLException("δ�ҵ�Ҫ�޸ĵĳ�����¼");
                        }
                    }

                    // 2. ���վ���ű��������վ�㳵������
                    if (!oldSno.equals(newSno)) {
                        // ԭվ���1
                        String sqlOldStation = "UPDATE station SET Vcount = Vcount - 1 WHERE Sno = ?";
                        try (PreparedStatement pstmt = connection.prepareStatement(sqlOldStation)) {
                            pstmt.setString(1, oldSno);
                            int affectedRows = pstmt.executeUpdate();
                            if (affectedRows == 0) {
                                throw new SQLException("δ�ҵ�ԭվ���¼");
                            }
                        }

                        // ��վ���1
                        String sqlNewStation = "UPDATE station SET Vcount = Vcount + 1 WHERE Sno = ?";
                        try (PreparedStatement pstmt = connection.prepareStatement(sqlNewStation)) {
                            pstmt.setString(1, newSno);
                            int affectedRows = pstmt.executeUpdate();
                            if (affectedRows == 0) {
                                throw new SQLException("δ�ҵ���վ���¼");
                            }
                        }
                    }

                    // �ύ����
                    connection.commit();

                    // ˢ�±��
                    queryUtil.queryTable(vehicleTable, "vehicle");
                    dialog.dispose();
                } catch (SQLException ex) {
                    // ����ع�
                    connection.rollback();
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(dialog, "�޸�ʧ��: " + ex.getMessage(),
                            "����", JOptionPane.ERROR_MESSAGE);
                } finally {
                    // �ָ��Զ��ύģʽ
                    connection.setAutoCommit(true);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "��������������",
                        "�������", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "���ݿ����ʧ��: " + ex.getMessage(),
                        "����", JOptionPane.ERROR_MESSAGE);
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
     * �޸Ķ�������
     */
    public void modifyOrder(JTable orderTable) {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(parentFrame, "����ѡ��Ҫ�޸ĵĶ�����¼",
                    "��ʾ", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ��ȡ��ǰ����
        DefaultTableModel model = (DefaultTableModel) orderTable.getModel();
        String ono = (String) model.getValueAt(selectedRow, 0);
        String cno = (String) model.getValueAt(selectedRow, 1);
        String vno = (String) model.getValueAt(selectedRow, 2);
        String startDateStr = (String) model.getValueAt(selectedRow, 3);
        String returnDateStr = (String) model.getValueAt(selectedRow, 4);
        double ofee = Double.parseDouble(model.getValueAt(selectedRow, 5).toString());

        // �����޸ĶԻ���
        JDialog dialog = new JDialog(parentFrame, "�޸Ķ�����Ϣ", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setLayout(new GridBagLayout());
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // �����
        JTextField onoField = new JTextField(ono, 10);
        onoField.setEditable(false);
        JTextField cnoField = new JTextField(cno, 10);
        JTextField vnoField = new JTextField(vno, 10);
        JTextField startDateField = new JTextField(startDateStr, 10);
        JTextField returnDateField = new JTextField(returnDateStr, 10);
        JTextField ofeeField = new JTextField(String.valueOf(ofee), 10);

        // ���������Ի���
        addComponent(dialog, new JLabel("�������:"), gbc, 0, 0);
        addComponent(dialog, onoField, gbc, 1, 0);
        addComponent(dialog, new JLabel("�ͻ����:"), gbc, 0, 1);
        addComponent(dialog, cnoField, gbc, 1, 1);
        addComponent(dialog, new JLabel("�������:"), gbc, 0, 2);
        addComponent(dialog, vnoField, gbc, 1, 2);
        addComponent(dialog, new JLabel("��ʼ���� (yyyy-MM-dd):"), gbc, 0, 3);
        addComponent(dialog, startDateField, gbc, 1, 3);
        addComponent(dialog, new JLabel("�黹���� (yyyy-MM-dd):"), gbc, 0, 4);
        addComponent(dialog, returnDateField, gbc, 1, 4);
        addComponent(dialog, new JLabel("����:"), gbc, 0, 5);
        addComponent(dialog, ofeeField, gbc, 1, 5);

        // ��ť���
        JPanel btnPanel = new JPanel();
        JButton confirmBtn = new JButton("ȷ���޸�");
        JButton cancelBtn = new JButton("ȡ��");

        confirmBtn.addActionListener(e -> {
            try {
                String newCno = cnoField.getText().trim();
                String newVno = vnoField.getText().trim();
                String newStartDateStr = startDateField.getText().trim();
                String newReturnDateStr = returnDateField.getText().trim();
                double newOfee = Double.parseDouble(ofeeField.getText().trim());

                // ��֤����
                if (newCno.isEmpty() || newVno.isEmpty() ||
                        newStartDateStr.isEmpty() || newReturnDateStr.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "�ͻ���š�������ź����ڲ���Ϊ��",
                            "�������", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // ���ڸ�ʽ��֤
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date newStartDate = sdf.parse(newStartDateStr);
                Date newReturnDate = sdf.parse(newReturnDateStr);

                // ����˳����֤
                if (newReturnDate.before(newStartDate)) {
                    JOptionPane.showMessageDialog(dialog, "�黹���ڱ����ڿ�ʼ����֮��",
                            "�������", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // ִ�и���
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
                        // ˢ�±��
                        queryUtil.queryTable(orderTable, "orders");
                        dialog.dispose();
                    } else {
                        throw new SQLException("δ�ҵ�Ҫ�޸ĵļ�¼");
                    }
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "���ñ���������",
                        "�������", JOptionPane.ERROR_MESSAGE);
            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(dialog, "���ڸ�ʽ����Ϊ yyyy-MM-dd",
                        "�������", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "�޸�ʧ��: " + ex.getMessage(),
                        "����", JOptionPane.ERROR_MESSAGE);
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
     * �޸�վ������
     */
    public void modifyStation(JTable stationTable) {
        int selectedRow = stationTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(parentFrame, "����ѡ��Ҫ�޸ĵ�վ���¼",
                    "��ʾ", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ��ȡ��ǰ����
        DefaultTableModel model = (DefaultTableModel) stationTable.getModel();
        String sno = (String) model.getValueAt(selectedRow, 0);
        String sname = (String) model.getValueAt(selectedRow, 1);
        int vcount = Integer.parseInt(model.getValueAt(selectedRow, 2).toString());

        // �����޸ĶԻ���
        JDialog dialog = new JDialog(parentFrame, "�޸�վ����Ϣ", true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setLayout(new GridBagLayout());
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // �����
        JTextField snoField = new JTextField(sno, 10);
        snoField.setEditable(false);
        JTextField snameField = new JTextField(sname, 10);
        JTextField vcountField = new JTextField(String.valueOf(vcount), 10);

        // ���������Ի���
        addComponent(dialog, new JLabel("վ����:"), gbc, 0, 0);
        addComponent(dialog, snoField, gbc, 1, 0);
        addComponent(dialog, new JLabel("վ������:"), gbc, 0, 1);
        addComponent(dialog, snameField, gbc, 1, 1);
        addComponent(dialog, new JLabel("��������:"), gbc, 0, 2);
        addComponent(dialog, vcountField, gbc, 1, 2);

        // ��ť���
        JPanel btnPanel = new JPanel();
        JButton confirmBtn = new JButton("ȷ���޸�");
        JButton cancelBtn = new JButton("ȡ��");

        confirmBtn.addActionListener(e -> {
            try {
                String newSname = snameField.getText().trim();
                int newVcount = Integer.parseInt(vcountField.getText().trim());

                // ��֤����
                if (newSname.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "վ�����Ʋ���Ϊ��",
                            "�������", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // ִ�и���
                String sql = "UPDATE station SET Sname = ?, Vcount = ? WHERE Sno = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                    pstmt.setString(1, newSname);
                    pstmt.setInt(2, newVcount);
                    pstmt.setString(3, sno);
                    int affectedRows = pstmt.executeUpdate();
                    if (affectedRows > 0) {
                        // ˢ�±��
                        queryUtil.queryTable(stationTable, "station");
                        dialog.dispose();
                    } else {
                        throw new SQLException("δ�ҵ�Ҫ�޸ĵļ�¼");
                    }
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "������������������",
                        "�������", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "�޸�ʧ��: " + ex.getMessage(),
                        "����", JOptionPane.ERROR_MESSAGE);
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
     * �޸Ŀͻ�����
     */
    public void modifyCustomer(JTable customerTable) {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(parentFrame, "����ѡ��Ҫ�޸ĵĿͻ���¼",
                    "��ʾ", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ��ȡ��ǰ����
        DefaultTableModel model = (DefaultTableModel) customerTable.getModel();
        String cno = (String) model.getValueAt(selectedRow, 0);

        // �����ݿ��ȡ��������
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
            JOptionPane.showMessageDialog(parentFrame, "��ȡ����ʧ��: " + ex.getMessage(),
                    "����", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // �����޸ĶԻ���
        JDialog dialog = new JDialog(parentFrame, "�޸Ŀͻ���Ϣ", true);
        dialog.setSize(400, 450); // �����Ի���߶�
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setLayout(new GridBagLayout());
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // �����
        JTextField cnoField = new JTextField(cno, 10);
        cnoField.setEditable(false);
        JTextField cnameField = new JTextField(cname, 10);
        JComboBox<String> sexCombo = new JComboBox<>(new String[]{"", "��", "Ů"});
        sexCombo.setEditable(false);
        if (csex != null && !csex.isEmpty()) {
            sexCombo.setSelectedItem(csex);
        }
        JTextField ageField = new JTextField(cage != null ? String.valueOf(cage) : "", 10);

        // ��ַѡ����� - �������б�����ϸ��ַ�����
        JComboBox<String> addressCombo = new JComboBox<>(PROVINCES);
        addressCombo.setEditable(false); // �����򲻿ɱ༭
        // �����ݿ��ַ�в���ƥ���ʡ�ݲ�ѡ��
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

        // ���������Ի���
        addComponent(dialog, new JLabel("�ͻ����:"), gbc, 0, 0);
        addComponent(dialog, cnoField, gbc, 1, 0);
        addComponent(dialog, new JLabel("�ͻ�����:"), gbc, 0, 1);
        addComponent(dialog, cnameField, gbc, 1, 1);
        addComponent(dialog, new JLabel("�Ա�:"), gbc, 0, 2);
        addComponent(dialog, sexCombo, gbc, 1, 2);
        addComponent(dialog, new JLabel("����:"), gbc, 0, 3);
        addComponent(dialog, ageField, gbc, 1, 3);
        addComponent(dialog, new JLabel("��ַ:"), gbc, 0, 4);
        addComponent(dialog, addressCombo, gbc, 1, 4); // ֱ��ʹ�������б���Ϊ��ַѡ��
        addComponent(dialog, new JLabel("��ϵ�绰:"), gbc, 0, 5);
        addComponent(dialog, phoneField, gbc, 1, 5);
        addComponent(dialog, new JLabel("����:"), gbc, 0, 6);
        addComponent(dialog, passField, gbc, 1, 6);

        // ��ť���
        JPanel btnPanel = new JPanel();
        JButton confirmBtn = new JButton("ȷ���޸�");
        JButton cancelBtn = new JButton("ȡ��");

        confirmBtn.addActionListener(e -> {
            try {
                String newCname = cnameField.getText().trim();
                String newCsex = (String) sexCombo.getSelectedItem();
                Integer newCage = null;
                if (!ageField.getText().trim().isEmpty()) {
                    newCage = Integer.parseInt(ageField.getText().trim());
                }

                String newCaddress = (String) addressCombo.getSelectedItem(); // ֱ��ʹ��ѡ���ʡ����Ϊ��ַ
                String newCphone = phoneField.getText().trim();
                String newCpass = passField.getText().trim();

                // ��֤����
                if (newCname.isEmpty() || newCphone.isEmpty() || newCpass.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "��������ϵ�绰�����벻��Ϊ��",
                            "�������", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (newCaddress == null || newCaddress.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "��ѡ���ַ",
                            "�������", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // ִ�и���
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
                    pstmt.setString(4, newCaddress); // ֱ�Ӵ洢ѡ���ʡ��
                    pstmt.setString(5, newCphone);
                    pstmt.setString(6, newCpass);
                    pstmt.setString(7, cno);

                    int affectedRows = pstmt.executeUpdate();
                    if (affectedRows > 0) {
                        queryUtil.queryTable(customerTable, "customer");
                        dialog.dispose();
                        JOptionPane.showMessageDialog(parentFrame, "�ͻ���Ϣ�޸ĳɹ�",
                                "�ɹ�", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        throw new SQLException("δ�ҵ�Ҫ�޸ĵļ�¼");
                    }
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "�������������",
                        "�������", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "�޸�ʧ��: " + ex.getMessage(),
                        "����", JOptionPane.ERROR_MESSAGE);
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
     * �޸�Ա������
     */
    public void modifyEmployee(JTable employeeTable) {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(parentFrame, "����ѡ��Ҫ�޸ĵ�Ա����¼",
                    "��ʾ", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ��ȡ��ǰ����
        DefaultTableModel model = (DefaultTableModel) employeeTable.getModel();
        String eno = (String) model.getValueAt(selectedRow, 0);
        String ename = (String) model.getValueAt(selectedRow, 1);
        String epass = (String) model.getValueAt(selectedRow, 2);

        // �����޸ĶԻ���
        JDialog dialog = new JDialog(parentFrame, "�޸�Ա����Ϣ", true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setLayout(new GridBagLayout());
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // �����
        JTextField enoField = new JTextField(eno, 10);
        enoField.setEditable(false);
        JTextField enameField = new JTextField(ename, 10);
        JTextField epassField = new JTextField(epass, 10);

        // ���������Ի���
        addComponent(dialog, new JLabel("Ա�����:"), gbc, 0, 0);
        addComponent(dialog, enoField, gbc, 1, 0);
        addComponent(dialog, new JLabel("Ա������:"), gbc, 0, 1);
        addComponent(dialog, enameField, gbc, 1, 1);
        addComponent(dialog, new JLabel("Ա������:"), gbc, 0, 2);
        addComponent(dialog, epassField, gbc, 1, 2);

        // ��ť���
        JPanel btnPanel = new JPanel();
        JButton confirmBtn = new JButton("ȷ���޸�");
        JButton cancelBtn = new JButton("ȡ��");

        confirmBtn.addActionListener(e -> {
            try {
                String newEname = enameField.getText().trim();
                String newEpass = epassField.getText().trim();

                // ��֤����
                if (newEname.isEmpty() || newEpass.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "���������벻��Ϊ��",
                            "�������", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // ִ�и���
                String sql = "UPDATE employee SET Ename = ?, Epass = ? WHERE Eno = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                    pstmt.setString(1, newEname);
                    pstmt.setString(2, newEpass);
                    pstmt.setString(3, eno);
                    int affectedRows = pstmt.executeUpdate();
                    if (affectedRows > 0) {
                        // ˢ�±��
                        queryUtil.queryTable(employeeTable, "employee");
                        dialog.dispose();
                    } else {
                        throw new SQLException("δ�ҵ�Ҫ�޸ĵļ�¼");
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "�޸�ʧ��: " + ex.getMessage(),
                        "����", JOptionPane.ERROR_MESSAGE);
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
     * ������������Ի���������
     */
    private void addComponent(JDialog dialog, Component component, GridBagConstraints gbc, int gridx, int gridy) {
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        dialog.add(component, gbc);
    }
}