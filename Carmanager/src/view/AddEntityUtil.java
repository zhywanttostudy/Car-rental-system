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
    private JFrame parentFrame; // ���������游��������

    public AddEntityUtil(Connection connection, JFrame parentFrame) {
        this.connection = connection;
        this.parentFrame = parentFrame; // ��ʼ��������
    }

    // ��ӳ����ķ���
    public boolean addVehicle(String vno, String vname, String vmodel, double vprice, String vstatus, String sno) {
        // �����������ȷ����ӳ����͸���վ��������ԭ����
        try {
            connection.setAutoCommit(false); // �ر��Զ��ύ

            // 1. ��ִ����ӳ����Ĳ���
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
                    connection.rollback(); // ����ʧ����ع�
                    throw new SQLException("�������ʧ��");
                }
            }

            // 2. ��ִ�и���վ�㳵�������Ĳ���
            String updateSql = "UPDATE station SET Vcount = Vcount + 1 WHERE Sno = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(updateSql)) {
                pstmt.setString(1, sno);
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected <= 0) {
                    connection.rollback(); // ����ʧ����ع�
                    throw new SQLException("δ�ҵ���Ӧ��վ����: " + sno);
                }
            }

            connection.commit(); // ȫ���ɹ����ύ����
            return true;
        } catch (SQLException e) {
            try {
                connection.rollback(); // �����쳣ʱ�ع�����
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame,
                    "��ӳ���ʧ��: " + e.getMessage(),
                    "����",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            try {
                connection.setAutoCommit(true); // �ָ��Զ��ύģʽ
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    // ���վ��ķ���
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
            JOptionPane.showMessageDialog(parentFrame, // ʹ�ø�����
                    "���վ��ʧ��: " + e.getMessage(),
                    "����",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    // ��������ʾ��ӳ����ı��Ի���
    public void showVehicleForm() {
        JDialog dialog = new JDialog(parentFrame, "����³���", true);
        dialog.setSize(500, 300);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setLayout(new GridLayout(7, 2, 10, 10));
        dialog.setResizable(false);

        // ���������
        JLabel vnoLabel = new JLabel("�������:");
        JTextField vnoField = new JTextField();

        JLabel vnameLabel = new JLabel("��������:");
        JTextField vnameField = new JTextField();

        JLabel vmodelLabel = new JLabel("����:");
        JTextField vmodelField = new JTextField();

        JLabel vpriceLabel = new JLabel("�����:");
        JTextField vpriceField = new JTextField();

        JLabel vstatusLabel = new JLabel("����״̬:");
        String[] statusOptions = {"����", "����"};
        JComboBox<String> vstatusCombo = new JComboBox<>(statusOptions);

        JLabel snoLabel = new JLabel("����վ����:");
        JTextField snoField = new JTextField();

        JButton submitBtn = new JButton("�ύ");
        JButton cancelBtn = new JButton("ȡ��");

        // ���������Ի���
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

        // ȡ����ť�¼�
        cancelBtn.addActionListener(e -> dialog.dispose());

        // �ύ��ť�¼�
        submitBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String vno = vnoField.getText().trim();
                String vname = vnameField.getText().trim();
                String vmodel = vmodelField.getText().trim();
                String vpriceStr = vpriceField.getText().trim();
                String vstatus = (String) vstatusCombo.getSelectedItem();
                String sno = snoField.getText().trim();

                // ��֤����
                if (vno.isEmpty() || vname.isEmpty() || vmodel.isEmpty() ||
                        vpriceStr.isEmpty() || sno.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog,
                            "�����ֶξ�Ϊ������!", "�������", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                double vprice;
                try {
                    vprice = Double.parseDouble(vpriceStr);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog,
                            "��������������!", "�������", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // ������ӷ���
                if (addVehicle(vno, vname, vmodel, vprice, vstatus, sno)) {
                    dialog.dispose();
                }
            }
        });

        dialog.setVisible(true);
    }

    // ��������ʾ���վ��ı��Ի���
    public void showStationForm() {
        JDialog dialog = new JDialog(parentFrame, "�����վ��", true);
        dialog.setSize(500, 250);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setLayout(new GridLayout(5, 2, 10, 10));
        dialog.setResizable(false);

        // ���������
        JLabel snoLabel = new JLabel("վ����:");
        JTextField snoField = new JTextField();

        JLabel snameLabel = new JLabel("վ������:");
        JTextField snameField = new JTextField();

        JLabel vcountLabel = new JLabel("��������:");
        JSpinner vcountSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));

        JButton submitBtn = new JButton("�ύ");
        JButton cancelBtn = new JButton("ȡ��");

        // ���������Ի���
        dialog.add(snoLabel);
        dialog.add(snoField);
        dialog.add(snameLabel);
        dialog.add(snameField);
        dialog.add(vcountLabel);
        dialog.add(vcountSpinner);
        dialog.add(submitBtn);
        dialog.add(cancelBtn);

        // ȡ����ť�¼�
        cancelBtn.addActionListener(e -> dialog.dispose());

        // �ύ��ť�¼�
        submitBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String sno = snoField.getText().trim();
                String sname = snameField.getText().trim();
                int vcount = (Integer) vcountSpinner.getValue();

                // ��֤����
                if (sno.isEmpty() || sname.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog,
                            "�����ֶξ�Ϊ������!", "�������", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // ������ӷ���
                if (addStation(sno, sname, vcount)) {
                    dialog.dispose();
                }
            }
        });

        dialog.setVisible(true);
    }

    // ���Ա���ķ���
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
                    "���Ա��ʧ��: " + e.getMessage(),
                    "����",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    // ��������ʾ���Ա���ı��Ի���
    public void showEmployeeForm() {
        JDialog dialog = new JDialog(parentFrame, "�����Ա��", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setLayout(new GridLayout(5, 2, 10, 10));
        dialog.setResizable(false);

        // ���������
        JLabel enoLabel = new JLabel("Ա�����:");
        JTextField enoField = new JTextField();

        JLabel enameLabel = new JLabel("Ա������:");
        JTextField enameField = new JTextField();

        JLabel epassLabel = new JLabel("��¼����:");
        JPasswordField epassField = new JPasswordField();

        JButton submitBtn = new JButton("�ύ");
        JButton cancelBtn = new JButton("ȡ��");

        // ���������Ի���
        dialog.add(enoLabel);
        dialog.add(enoField);
        dialog.add(enameLabel);
        dialog.add(enameField);
        dialog.add(epassLabel);
        dialog.add(epassField);
        dialog.add(submitBtn);
        dialog.add(cancelBtn);

        // ȡ����ť�¼�
        cancelBtn.addActionListener(e -> dialog.dispose());

        // �ύ��ť�¼�
        submitBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String eno = enoField.getText().trim();
                String ename = enameField.getText().trim();
                String epass = new String(epassField.getPassword()).trim();

                // ��֤����
                if (eno.isEmpty() || ename.isEmpty() || epass.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog,
                            "�����ֶξ�Ϊ������!", "�������", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // ������ӷ���
                if (addEmployee(eno, ename, epass)) {
                    JOptionPane.showMessageDialog(dialog,
                            "Ա����ӳɹ�!", "�ɹ�", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                }
            }
        });

        dialog.setVisible(true);
    }

}