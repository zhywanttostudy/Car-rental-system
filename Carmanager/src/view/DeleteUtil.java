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

    // 删除客户
    public boolean deleteCustomer(String customerId) {
        // 检查是否存在关联订单
        if (hasActiveOrders(customerId, "customer")) {
            JOptionPane.showMessageDialog(parentFrame,
                    "该客户存在未完成订单，无法删除！",
                    "删除失败",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }

        String sql = "DELETE FROM customer WHERE Cno = ?";
        return executeDelete(sql, customerId, "客户");
    }

    // 删除站点
    public boolean deleteStation(String stationId) {
        // 检查站点下是否有车辆
        if (hasAssociatedVehicles(stationId)) {
            JOptionPane.showMessageDialog(parentFrame,
                    "该站点下存在车辆，无法删除！",
                    "删除失败",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }

        String sql = "DELETE FROM station WHERE Sno = ?";
        return executeDelete(sql, stationId, "站点");
    }

    // 删除车辆
    public boolean deleteVehicle(String vehicleId) {
        // 检查是否存在关联订单
        if (hasActiveOrders(vehicleId, "vehicle")) {
            JOptionPane.showMessageDialog(parentFrame,
                    "该车辆存在未完成订单，无法删除！",
                    "删除失败",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // 获取车辆所属站点
        String stationId = getVehicleStation(vehicleId);
        if (stationId == null) {
            return false;
        }

        try {
            connection.setAutoCommit(false);

            // 删除车辆
            String deleteVehicleSQL = "DELETE FROM vehicle WHERE Vno = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(deleteVehicleSQL)) {
                pstmt.setString(1, vehicleId);
                pstmt.executeUpdate();
            }

            // 更新站点车辆数量
            String updateStationSQL = "UPDATE station SET Vcount = Vcount - 1 WHERE Sno = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(updateStationSQL)) {
                pstmt.setString(1, stationId);
                pstmt.executeUpdate();
            }

            connection.commit();
            JOptionPane.showMessageDialog(parentFrame, "车辆删除成功！", "操作成功", JOptionPane.INFORMATION_MESSAGE);
            return true;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            JOptionPane.showMessageDialog(parentFrame,
                    "删除失败: " + e.getMessage(),
                    "数据库错误",
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

    // 检查是否存在未完成订单
    private boolean hasActiveOrders(String id, String type) {
        String sql = "SELECT COUNT(*) FROM orders WHERE ";
        sql += type.equals("customer") ? "Cno = ?" : "Vno = ?";
        sql += " AND Ostatus NOT IN ('已完成')";

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

    // 检查站点是否有车辆
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

    // 获取车辆所属站点
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
                    "获取车辆信息失败: " + e.getMessage(),
                    "数据库错误",
                    JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }

    // 通用删除操作
    private boolean executeDelete(String sql, String id, String entityName) {
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(parentFrame,
                        entityName + "删除成功！",
                        "操作成功",
                        JOptionPane.INFORMATION_MESSAGE);
                return true;
            } else {
                JOptionPane.showMessageDialog(parentFrame,
                        "未找到指定的" + entityName,
                        "删除失败",
                        JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(parentFrame,
                    "删除失败: " + e.getMessage(),
                    "数据库错误",
                    JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }
}