//还车功能
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

            // 1. 验证日期格式
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            Date returnDate = sdf.parse(returnDateStr);

            // 2. 查询订单信息
            String sql = "SELECT Ostart, Oreturn, Ofee, Vprice, Ostatus " +
                    "FROM orders o JOIN vehicle v ON o.Vno = v.Vno " +
                    "WHERE Ono = ?";
            pstmt1 = conn.prepareStatement(sql);
            pstmt1.setString(1, orderNo);
            rs = pstmt1.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(parentFrame, "订单不存在，订单号：" + orderNo,
                        "错误", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            Date startDate = rs.getDate("Ostart");
            Date originalReturnDate = rs.getDate("Oreturn");
            double originalFee = rs.getDouble("Ofee");
            double price = rs.getDouble("Vprice");
            String status = rs.getString("Ostatus").trim(); // 去除空格

            // 3. 检查订单状态是否为进行中
            if (!OrderStatus.IN_PROGRESS.equals(status)) {
                JOptionPane.showMessageDialog(parentFrame, "订单状态不是进行中，无法完成还车",
                        "错误", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // 4. 检查还车日期是否在开始日期之后
            if (returnDate.before(startDate)) {
                JOptionPane.showMessageDialog(parentFrame, "还车日期不能早于租车开始日期",
                        "错误", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            double newFee = originalFee;
            // 5. 如果还车日期超过原预计日期，计算额外费用
            if (returnDate.after(originalReturnDate)) {
                long diffInMillis = returnDate.getTime() - originalReturnDate.getTime();
                int extraDays = (int) (diffInMillis / (1000 * 60 * 60 * 24));
                if (extraDays == 0) extraDays = 1;
                double extraFee = price * extraDays;
                newFee = originalFee + extraFee;

                // 更新订单费用
                String updateFeeSql = "UPDATE orders SET Oreturn = ?, Ofee = ? WHERE Ono = ?";
                pstmt2 = conn.prepareStatement(updateFeeSql);
                pstmt2.setDate(1, new java.sql.Date(returnDate.getTime()));
                pstmt2.setDouble(2, newFee);
                pstmt2.setString(3, orderNo);
                pstmt2.executeUpdate();
            }

            // 6. 更新订单状态为待支付
            String updateOrderSql = "UPDATE orders SET Ostatus = ? WHERE Ono = ?";
            pstmt3 = conn.prepareStatement(updateOrderSql);
            pstmt3.setString(1, OrderStatus.PENDING_PAYMENT);
            pstmt3.setString(2, orderNo);
            pstmt3.executeUpdate();

            // 7. 更新车辆状态为可出租
            String updateVehicleSql = "UPDATE vehicle SET Vstatus = '待租' WHERE Vno = ?";
            pstmt3 = conn.prepareStatement(updateVehicleSql);
            pstmt3.setString(1, vehicleNo);
            pstmt3.executeUpdate();

            conn.commit();
            System.out.println("还车成功，订单号：" + orderNo);
            JOptionPane.showMessageDialog(parentFrame, 
                    "还车成功，订单号：" + orderNo + 
                    "\n原费用：" + originalFee + "元" +
                    "\n实际费用：" + newFee + "元" +
                    "\n请及时支付。",  
                    "还车成功", JOptionPane.INFORMATION_MESSAGE);
            return true;
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(parentFrame, "日期格式不正确，请使用YYYY-MM-DD格式，例如：2025-06-24",
                    "错误", JOptionPane.ERROR_MESSAGE);
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (conn != null) {
                    conn.rollback();
                    System.err.println("回滚还车失败，订单未修改");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                System.err.println("回滚失败。");
            }
            JOptionPane.showMessageDialog(parentFrame, "还车失败：" + e.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
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

