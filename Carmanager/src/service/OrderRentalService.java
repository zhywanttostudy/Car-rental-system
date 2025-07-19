package service;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import basis.Connect;
import basis.DBUtil;

public class OrderRentalService {
    private JFrame parentFrame;
    private Connection connection;

    public OrderRentalService(JFrame parentFrame, Connection connection) {
        this.parentFrame = parentFrame;
        this.connection = connection;
    }

    public boolean rentOrder(String orderNo, String vehicleNo) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = Connect.getConnection();

            // 查询订单状态
            String sql = "SELECT Ostatus FROM orders WHERE Ono = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, orderNo);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                String orderStatus = rs.getString("Ostatus").trim();
                if ("预约中".equals(orderStatus)) {
                    // 执行租赁操作，这里可以添加具体的租赁逻辑，例如更新订单状态等
                    String updateSql = "UPDATE orders SET Ostatus = '进行中' WHERE Ono = ?";
                    try (PreparedStatement updatePstmt = conn.prepareStatement(updateSql)) {
                        updatePstmt.setString(1, orderNo);
                        int rowsAffected = updatePstmt.executeUpdate();
                        if (rowsAffected == 1) {
                            // 同时更新车辆状态为已租赁
                            String updateVehicleSql = "UPDATE vehicle SET Vstatus = '已租' WHERE Vno = ?";
                            try (PreparedStatement updateVehiclePstmt = conn.prepareStatement(updateVehicleSql)) {
                                updateVehiclePstmt.setString(1, vehicleNo);
                                updateVehiclePstmt.executeUpdate();
                            }
                            return true;
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(parentFrame, "该订单当前状态为[" + orderStatus + "], 无法租赁",
                            "租赁失败", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame, "数据库操作出错: " + e.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        } finally {
            DBUtil.close(rs);
            DBUtil.close(pstmt);
            DBUtil.close(conn);
        }
        return false;
    }
}