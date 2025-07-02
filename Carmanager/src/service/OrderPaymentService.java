//支付订单功能
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

            // 1. 检查订单状态是否为待支付
            String checkSql = "SELECT Ostatus, Ofee FROM orders WHERE Ono = ?";
            pstmt1 = conn.prepareStatement(checkSql);
            pstmt1.setString(1, orderNo);
            rs = pstmt1.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(parentFrame, "订单不存在，订单号：" + orderNo,
                        "错误", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            String status = rs.getString("Ostatus").trim(); // 去除空格
            double fee = rs.getDouble("Ofee");

            if (!OrderStatus.PENDING_PAYMENT.equals(status)) {
                JOptionPane.showMessageDialog(parentFrame, "订单状态不是待支付，无法进行支付",
                        "错误", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // 2. 显示支付确认对话框
            int confirm = JOptionPane.showConfirmDialog(parentFrame,
                    "确认支付订单 " + orderNo + " 吗？\n" +
                    "支付金额：" + fee + "元",
                    "确认支付",
                    JOptionPane.YES_NO_OPTION);
            
            if (confirm != JOptionPane.YES_OPTION) {
                return false;
            }

            // 更新订单状态为已完成
            String updateSql = "UPDATE orders SET Ostatus = ? WHERE Ono = ?";
            pstmt2 = conn.prepareStatement(updateSql);
            pstmt2.setString(1, OrderStatus.COMPLETED);
            pstmt2.setString(2, orderNo);
            int rowsAffected = pstmt2.executeUpdate();

            if (rowsAffected != 1) {
                throw new SQLException("订单状态更新失败，影响行数：" + rowsAffected);
            }

            conn.commit();
            System.out.println("订单支付成功，订单号：" + orderNo);
            JOptionPane.showMessageDialog(parentFrame, "支付成功，订单号：" + orderNo,
                    "支付成功", JOptionPane.INFORMATION_MESSAGE);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (conn != null) {
                    conn.rollback();
                    System.err.println("回滚支付失败，订单未修改");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                System.err.println("回滚失败。");
            }
            JOptionPane.showMessageDialog(parentFrame, "支付失败：" + e.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            DBUtil.close(rs);
            DBUtil.close(pstmt1);
            DBUtil.close(pstmt2);
            DBUtil.close(conn);
        }
    }
}