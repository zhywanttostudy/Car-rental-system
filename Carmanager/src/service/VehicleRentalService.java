//�⳵����
package service;

import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.table.JTableHeader;

import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import basis.Connect;
import basis.DBUtil;

public class VehicleRentalService {
    private JFrame parentFrame;
    private Connection connection;

    public VehicleRentalService(JFrame parentFrame, Connection connection) {
        this.parentFrame = parentFrame;
        this.connection = connection;
    }

    public void rentVehicle(JTable table, String customerId) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(parentFrame, "��ѡ��Ҫ���޵ĳ���",
                    "��ʾ", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String vehicleNo = table.getValueAt(selectedRow, 0).toString();
        String vehicleName = table.getValueAt(selectedRow, 1).toString();
        String vehicleStatus = table.getValueAt(selectedRow, 4).toString().trim();

        // �޸�״̬�жϣ�ȷ������Ϊ������״̬
        if (!"����".equals(vehicleStatus)) {
            JOptionPane.showMessageDialog(parentFrame, "�ó�����ǰ״̬Ϊ[" + vehicleStatus + "], �޷�����",
                    "����ʧ��", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDateChooser startDateChooser = new JDateChooser();
        JDateChooser endDateChooser = new JDateChooser();

        JPanel panel = new JPanel(new GridLayout(2, 2));
        panel.add(new JLabel("��ʼ����:"));
        panel.add(startDateChooser);
        panel.add(new JLabel("��������:"));
        panel.add(endDateChooser);

        int result = JOptionPane.showConfirmDialog(parentFrame, panel,
                "���� - " + vehicleName,
                JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            Date startDate = startDateChooser.getDate();
            Date endDate = endDateChooser.getDate();

            if (startDate == null || endDate == null) {
                JOptionPane.showMessageDialog(parentFrame, "���ڲ���Ϊ��",
                        "����", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String startDateStr = sdf.format(startDate);
                String endDateStr = sdf.format(endDate);

                if (endDate.before(startDate)) {
                    JOptionPane.showMessageDialog(parentFrame, "�������ڲ������ڿ�ʼ����",
                            "����", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                long diffInMillis = endDate.getTime() - startDate.getTime();
                int days = (int) (diffInMillis / (1000 * 60 * 60 * 24));
                if (days == 0) days = 1;

                String priceStr = table.getValueAt(selectedRow, 3).toString();
                if (priceStr == null || priceStr.isEmpty()) {
                    JOptionPane.showMessageDialog(parentFrame, "�����۸���Ϣ�쳣������ϵ����Ա",
                            "����", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                double price = Double.parseDouble(priceStr);
                double totalFee = price * days;

                int confirm = JOptionPane.showConfirmDialog(parentFrame,
                        "ȷ��Ҫ���� " + vehicleName + " ��\n" +
                                "����ʱ��Ϊ" + days + "��\n" +
                                "�ܷ��ã�" + totalFee + "Ԫ",
                        "ȷ������",
                        JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    String orderNo = "O" + System.currentTimeMillis();
                    orderNo = orderNo.substring(0, Math.min(orderNo.length(), 10));

                    boolean success = createOrder(orderNo, customerId,
                            vehicleNo, startDateStr, endDateStr, totalFee);

                    if (success) {
                        JOptionPane.showMessageDialog(parentFrame, "���޳ɹ��������ţ�" + orderNo,
                                "�ɹ�", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(parentFrame, "�����۸�ת��ʧ�ܣ�����ϵ����Ա",
                        "����", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean createOrder(String orderNo, String customerNo, String vehicleNo,
                                String startDateStr, String endDateStr, double fee) {
        Connection conn = null;
        PreparedStatement pstmt1 = null;
        PreparedStatement pstmt2 = null;

        try {
            conn = Connect.getConnection();
            conn.setAutoCommit(false);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            java.sql.Date startDate = new java.sql.Date(sdf.parse(startDateStr).getTime());
            java.sql.Date endDate = new java.sql.Date(sdf.parse(endDateStr).getTime());

            // �޸�SQL��䣬���Ostatus�ֶ�
            String sql1 = "INSERT INTO orders (Ono, Cno, Vno, Ostart, Oreturn, Ostatus, Ofee) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
            pstmt1 = conn.prepareStatement(sql1);
            pstmt1.setString(1, orderNo);
            pstmt1.setString(2, customerNo);
            pstmt1.setString(3, vehicleNo);
            pstmt1.setDate(4, startDate);
            pstmt1.setDate(5, endDate);
            pstmt1.setString(6, "������");  
            pstmt1.setDouble(7, fee);

            int rowsAffected = pstmt1.executeUpdate();
            if (rowsAffected != 1) {
                throw new SQLException("��������ʧ�ܣ�Ӱ������: " + rowsAffected);
            }

            String sql2 = "UPDATE vehicle SET Vstatus='����' WHERE Vno=?";
            pstmt2 = conn.prepareStatement(sql2);
            pstmt2.setString(1, vehicleNo);
            rowsAffected = pstmt2.executeUpdate();
            if (rowsAffected != 1) {
                throw new SQLException("����״̬����ʧ�ܣ�Ӱ������: " + rowsAffected);
            }

            conn.commit();
            System.out.println("��������������״̬���³ɹ���������: " + orderNo);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (conn != null) {
                    conn.rollback();
                    System.err.println("����ع������ʧ�ܣ�����δ����");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                System.err.println("����ع�ʧ�ܡ�");
            }
            JOptionPane.showMessageDialog(parentFrame, "���ʧ�ܣ�" + e.getMessage(),
                    "����", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            DBUtil.close(pstmt1);
            DBUtil.close(pstmt2);
            DBUtil.close(conn);
        }
    }

    // ֧����������
    public boolean payOrder(String orderNo) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = Connect.getConnection();

            // ��ѯ����״̬�ͷ�����Ϣ
            String sql = "SELECT Ostatus, Ofee FROM orders WHERE Ono = ?"; // ���Ofee�ֶβ�ѯ
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, orderNo);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                String orderStatus = rs.getString("Ostatus");
                double fee = rs.getDouble("Ofee"); // ��ȡ������Ϣ
                
                // ��鶩��״̬�Ƿ�Ϊ"��֧��"
                if ("��֧��".equals(orderStatus)) {
                    // ����û�ȷ��֧����ʾ
                    int confirm = JOptionPane.showConfirmDialog(parentFrame,
                            "ȷ��֧������ " + orderNo + " ��\n" +
                            "֧����" + fee + "Ԫ",
                            "ȷ��֧��",
                            JOptionPane.YES_NO_OPTION);
                    
                    if (confirm != JOptionPane.YES_OPTION) {
                        return false; // �û�ȡ��֧��
                    }
                    
                    // ���¶���״̬Ϊ"�����"
                    String updateSql = "UPDATE orders SET Ostatus = '�����' WHERE Ono = ?";
                    try (PreparedStatement updatePstmt = conn.prepareStatement(updateSql)) {
                        updatePstmt.setString(1, orderNo);
                        int rowsAffected = updatePstmt.executeUpdate();
                        
                        if (rowsAffected == 1) {
                            System.out.println("����֧���ɹ���������: " + orderNo);
                            
                            // ��ӳɹ�֧����ʾ
                            JOptionPane.showMessageDialog(parentFrame, 
                                    "֧���ɹ������������",
                                    "֧���ɹ�", 
                                    JOptionPane.INFORMATION_MESSAGE);
                            return true;
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(parentFrame, 
                            "�ö������Ǵ�֧��״̬���޷����֧��\n��ǰ״̬: " + orderStatus,
                            "����", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(parentFrame, "δ�ҵ��ö�����������: " + orderNo,
                        "����", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame, "֧������ʱ�������ݿ����: " + e.getMessage(),
                    "����", JOptionPane.ERROR_MESSAGE);
        } finally {
            DBUtil.close(rs);
            DBUtil.close(pstmt);
            DBUtil.close(conn);
        }
        return false;
    }
}