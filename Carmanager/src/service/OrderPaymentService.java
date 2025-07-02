//֧����������
package service;

import javax.swing.*;

import basis.Connect;
import basis.DBUtil;
import entity.OrderStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OrderPaymentService {
    private JFrame parentFrame;
    private Connection connection;

    public OrderPaymentService(JFrame parentFrame, Connection connection) {
        this.parentFrame = parentFrame;
        this.connection = connection;
    }

    public boolean payOrder(String orderNo) {
        Connection conn = null;
        PreparedStatement pstmt1 = null;
        PreparedStatement pstmt2 = null;
        ResultSet rs = null;

        try {
            conn = Connect.getConnection();
            conn.setAutoCommit(false);

            // 1. ��鶩��״̬�Ƿ�Ϊ��֧��
            String checkSql = "SELECT Ostatus, Ofee FROM orders WHERE Ono = ?";
            pstmt1 = conn.prepareStatement(checkSql);
            pstmt1.setString(1, orderNo);
            rs = pstmt1.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(parentFrame, "���������ڣ������ţ�" + orderNo,
                        "����", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            String status = rs.getString("Ostatus").trim(); // ȥ���ո�
            double fee = rs.getDouble("Ofee");

            if (!OrderStatus.PENDING_PAYMENT.equals(status)) {
                JOptionPane.showMessageDialog(parentFrame, "����״̬���Ǵ�֧�����޷�����֧��",
                        "����", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // 2. ��ʾ֧��ȷ�϶Ի���
            int confirm = JOptionPane.showConfirmDialog(parentFrame,
                    "ȷ��֧������ " + orderNo + " ��\n" +
                    "֧����" + fee + "Ԫ",
                    "ȷ��֧��",
                    JOptionPane.YES_NO_OPTION);
            
            if (confirm != JOptionPane.YES_OPTION) {
                return false;
            }

            // ���¶���״̬Ϊ�����
            String updateSql = "UPDATE orders SET Ostatus = ? WHERE Ono = ?";
            pstmt2 = conn.prepareStatement(updateSql);
            pstmt2.setString(1, OrderStatus.COMPLETED);
            pstmt2.setString(2, orderNo);
            int rowsAffected = pstmt2.executeUpdate();

            if (rowsAffected != 1) {
                throw new SQLException("����״̬����ʧ�ܣ�Ӱ��������" + rowsAffected);
            }

            conn.commit();
            System.out.println("����֧���ɹ��������ţ�" + orderNo);
            JOptionPane.showMessageDialog(parentFrame, "֧���ɹ��������ţ�" + orderNo,
                    "֧���ɹ�", JOptionPane.INFORMATION_MESSAGE);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (conn != null) {
                    conn.rollback();
                    System.err.println("�ع�֧��ʧ�ܣ�����δ�޸�");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                System.err.println("�ع�ʧ�ܡ�");
            }
            JOptionPane.showMessageDialog(parentFrame, "֧��ʧ�ܣ�" + e.getMessage(),
                    "����", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            DBUtil.close(rs);
            DBUtil.close(pstmt1);
            DBUtil.close(pstmt2);
            DBUtil.close(conn);
        }
    }
}