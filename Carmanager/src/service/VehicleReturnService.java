//��������
package service;

import javax.swing.*;

import basis.Connect;
import basis.DBUtil;
import entity.OrderStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class VehicleReturnService {
    private JFrame parentFrame;
    private Connection connection;

    public VehicleReturnService(JFrame parentFrame, Connection connection) {
        this.parentFrame = parentFrame;
        this.connection = connection;
    }

    public boolean returnVehicle(String orderNo, String vehicleNo, String returnDateStr) {
        Connection conn = null;
        PreparedStatement pstmt1 = null;
        PreparedStatement pstmt2 = null;
        PreparedStatement pstmt3 = null;
        ResultSet rs = null;

        try {
            conn = Connect.getConnection();
            conn.setAutoCommit(false);

            // 1. ��֤���ڸ�ʽ
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            Date returnDate = sdf.parse(returnDateStr);

            // 2. ��ѯ������Ϣ
            String sql = "SELECT Ostart, Oreturn, Ofee, Vprice, Ostatus " +
                    "FROM orders o JOIN vehicle v ON o.Vno = v.Vno " +
                    "WHERE Ono = ?";
            pstmt1 = conn.prepareStatement(sql);
            pstmt1.setString(1, orderNo);
            rs = pstmt1.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(parentFrame, "���������ڣ������ţ�" + orderNo,
                        "����", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            Date startDate = rs.getDate("Ostart");
            Date originalReturnDate = rs.getDate("Oreturn");
            double originalFee = rs.getDouble("Ofee");
            double price = rs.getDouble("Vprice");
            String status = rs.getString("Ostatus").trim(); // ȥ���ո�

            // 3. ��鶩��״̬�Ƿ�Ϊ������
            if (!OrderStatus.IN_PROGRESS.equals(status)) {
                JOptionPane.showMessageDialog(parentFrame, "����״̬���ǽ����У��޷���ɻ���",
                        "����", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // 4. ��黹�������Ƿ��ڿ�ʼ����֮��
            if (returnDate.before(startDate)) {
                JOptionPane.showMessageDialog(parentFrame, "�������ڲ��������⳵��ʼ����",
                        "����", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            double newFee = originalFee;
            // 5. ����������ڳ���ԭԤ�����ڣ�����������
            if (returnDate.after(originalReturnDate)) {
                long diffInMillis = returnDate.getTime() - originalReturnDate.getTime();
                int extraDays = (int) (diffInMillis / (1000 * 60 * 60 * 24));
                if (extraDays == 0) extraDays = 1;
                double extraFee = price * extraDays;
                newFee = originalFee + extraFee;

                // ���¶�������
                String updateFeeSql = "UPDATE orders SET Oreturn = ?, Ofee = ? WHERE Ono = ?";
                pstmt2 = conn.prepareStatement(updateFeeSql);
                pstmt2.setDate(1, new java.sql.Date(returnDate.getTime()));
                pstmt2.setDouble(2, newFee);
                pstmt2.setString(3, orderNo);
                pstmt2.executeUpdate();
            }

            // 6. ���¶���״̬Ϊ��֧��
            String updateOrderSql = "UPDATE orders SET Ostatus = ? WHERE Ono = ?";
            pstmt3 = conn.prepareStatement(updateOrderSql);
            pstmt3.setString(1, OrderStatus.PENDING_PAYMENT);
            pstmt3.setString(2, orderNo);
            pstmt3.executeUpdate();

            // 7. ���³���״̬Ϊ�ɳ���
            String updateVehicleSql = "UPDATE vehicle SET Vstatus = '�ɳ���' WHERE Vno = ?";
            pstmt3 = conn.prepareStatement(updateVehicleSql);
            pstmt3.setString(1, vehicleNo);
            pstmt3.executeUpdate();

            conn.commit();
            System.out.println("�����ɹ��������ţ�" + orderNo);
            JOptionPane.showMessageDialog(parentFrame, 
                    "�����ɹ��������ţ�" + orderNo + 
                    "\nԭ���ã�" + originalFee + "Ԫ" +
                    "\nʵ�ʷ��ã�" + newFee + "Ԫ" +
                    "\n�뼰ʱ֧����",  
                    "�����ɹ�", JOptionPane.INFORMATION_MESSAGE);
            return true;
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(parentFrame, "���ڸ�ʽ����ȷ����ʹ��YYYY-MM-DD��ʽ�����磺2025-06-24",
                    "����", JOptionPane.ERROR_MESSAGE);
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (conn != null) {
                    conn.rollback();
                    System.err.println("�ع�����ʧ�ܣ�����δ�޸�");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                System.err.println("�ع�ʧ�ܡ�");
            }
            JOptionPane.showMessageDialog(parentFrame, "����ʧ�ܣ�" + e.getMessage(),
                    "����", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            DBUtil.close(rs);
            DBUtil.close(pstmt1);
            DBUtil.close(pstmt2);
            DBUtil.close(pstmt3);
            DBUtil.close(conn);
        }
    }
}

