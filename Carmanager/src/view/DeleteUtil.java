package view;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DeleteUtil {
    private final Connection connection;
    private final JFrame parentFrame;

    public DeleteUtil(Connection connection, JFrame parentFrame) {
        this.connection = connection;
        this.parentFrame = parentFrame;
    }

    // ɾ���ͻ�
    public boolean deleteCustomer(String customerId) {
        // ����Ƿ���ڹ�������
        if (hasActiveOrders(customerId, "customer")) {
            JOptionPane.showMessageDialog(parentFrame,
                    "�ÿͻ�����δ��ɶ������޷�ɾ����",
                    "ɾ��ʧ��",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }

        String sql = "DELETE FROM customer WHERE Cno = ?";
        return executeDelete(sql, customerId, "�ͻ�");
    }

    // ɾ��վ��
    public boolean deleteStation(String stationId) {
        // ���վ�����Ƿ��г���
        if (hasAssociatedVehicles(stationId)) {
            JOptionPane.showMessageDialog(parentFrame,
                    "��վ���´��ڳ������޷�ɾ����",
                    "ɾ��ʧ��",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }

        String sql = "DELETE FROM station WHERE Sno = ?";
        return executeDelete(sql, stationId, "վ��");
    }

    // ɾ������
    public boolean deleteVehicle(String vehicleId) {
        // ����Ƿ���ڹ�������
        if (hasActiveOrders(vehicleId, "vehicle")) {
            JOptionPane.showMessageDialog(parentFrame,
                    "�ó�������δ��ɶ������޷�ɾ����",
                    "ɾ��ʧ��",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // ��ȡ��������վ��
        String stationId = getVehicleStation(vehicleId);
        if (stationId == null) {
            return false;
        }

        try {
            connection.setAutoCommit(false);

            // ɾ������
            String deleteVehicleSQL = "DELETE FROM vehicle WHERE Vno = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(deleteVehicleSQL)) {
                pstmt.setString(1, vehicleId);
                pstmt.executeUpdate();
            }

            // ����վ�㳵������
            String updateStationSQL = "UPDATE station SET Vcount = Vcount - 1 WHERE Sno = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(updateStationSQL)) {
                pstmt.setString(1, stationId);
                pstmt.executeUpdate();
            }

            connection.commit();
            JOptionPane.showMessageDialog(parentFrame, "����ɾ���ɹ���", "�����ɹ�", JOptionPane.INFORMATION_MESSAGE);
            return true;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            JOptionPane.showMessageDialog(parentFrame,
                    "ɾ��ʧ��: " + e.getMessage(),
                    "���ݿ����",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // ����Ƿ����δ��ɶ���
    private boolean hasActiveOrders(String id, String type) {
        String sql = "SELECT COUNT(*) FROM orders WHERE ";
        sql += type.equals("customer") ? "Cno = ?" : "Vno = ?";
        sql += " AND Ostatus NOT IN ('�����')";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ���վ���Ƿ��г���
    private boolean hasAssociatedVehicles(String stationId) {
        String sql = "SELECT COUNT(*) FROM vehicle WHERE Sno = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, stationId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ��ȡ��������վ��
    private String getVehicleStation(String vehicleId) {
        String sql = "SELECT Sno FROM vehicle WHERE Vno = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, vehicleId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Sno");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(parentFrame,
                    "��ȡ������Ϣʧ��: " + e.getMessage(),
                    "���ݿ����",
                    JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }

    // ͨ��ɾ������
    private boolean executeDelete(String sql, String id, String entityName) {
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(parentFrame,
                        entityName + "ɾ���ɹ���",
                        "�����ɹ�",
                        JOptionPane.INFORMATION_MESSAGE);
                return true;
            } else {
                JOptionPane.showMessageDialog(parentFrame,
                        "δ�ҵ�ָ����" + entityName,
                        "ɾ��ʧ��",
                        JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(parentFrame,
                    "ɾ��ʧ��: " + e.getMessage(),
                    "���ݿ����",
                    JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }
}