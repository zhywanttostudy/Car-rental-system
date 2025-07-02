//租车功能
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
            JOptionPane.showMessageDialog(parentFrame, "请选择要租赁的车辆",
                    "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String vehicleNo = table.getValueAt(selectedRow, 0).toString();
        String vehicleName = table.getValueAt(selectedRow, 1).toString();
        String vehicleStatus = table.getValueAt(selectedRow, 4).toString().trim();

        // 修改状态判断，确保车辆为可租赁状态
        if (!"待租".equals(vehicleStatus)) {
            JOptionPane.showMessageDialog(parentFrame, "该车辆当前状态为[" + vehicleStatus + "], 无法租赁",
                    "租赁失败", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDateChooser startDateChooser = new JDateChooser();
        JDateChooser endDateChooser = new JDateChooser();

        JPanel panel = new JPanel(new GridLayout(2, 2));
        panel.add(new JLabel("开始日期:"));
        panel.add(startDateChooser);
        panel.add(new JLabel("结束日期:"));
        panel.add(endDateChooser);

        int result = JOptionPane.showConfirmDialog(parentFrame, panel,
                "租赁 - " + vehicleName,
                JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            Date startDate = startDateChooser.getDate();
            Date endDate = endDateChooser.getDate();

            if (startDate == null || endDate == null) {
                JOptionPane.showMessageDialog(parentFrame, "日期不能为空",
                        "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String startDateStr = sdf.format(startDate);
                String endDateStr = sdf.format(endDate);

                if (endDate.before(startDate)) {
                    JOptionPane.showMessageDialog(parentFrame, "结束日期不能早于开始日期",
                            "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                long diffInMillis = endDate.getTime() - startDate.getTime();
                int days = (int) (diffInMillis / (1000 * 60 * 60 * 24));
                if (days == 0) days = 1;

                String priceStr = table.getValueAt(selectedRow, 3).toString();
                if (priceStr == null || priceStr.isEmpty()) {
                    JOptionPane.showMessageDialog(parentFrame, "车辆价格信息异常，请联系管理员",
                            "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                double price = Double.parseDouble(priceStr);
                double totalFee = price * days;

                int confirm = JOptionPane.showConfirmDialog(parentFrame,
                        "确认要租赁 " + vehicleName + " 吗？\n" +
                                "租赁时长为" + days + "天\n" +
                                "总费用：" + totalFee + "元",
                        "确认租赁",
                        JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    String orderNo = "O" + System.currentTimeMillis();
                    orderNo = orderNo.substring(0, Math.min(orderNo.length(), 10));

                    boolean success = createOrder(orderNo, customerId,
                            vehicleNo, startDateStr, endDateStr, totalFee);

                    if (success) {
                        JOptionPane.showMessageDialog(parentFrame, "租赁成功，订单号：" + orderNo,
                                "成功", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(parentFrame, "车辆价格转换失败，请联系管理员",
                        "错误", JOptionPane.ERROR_MESSAGE);
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

            // 修改SQL语句，添加Ostatus字段
            String sql1 = "INSERT INTO orders (Ono, Cno, Vno, Ostart, Oreturn, Ostatus, Ofee) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
            pstmt1 = conn.prepareStatement(sql1);
            pstmt1.setString(1, orderNo);
            pstmt1.setString(2, customerNo);
            pstmt1.setString(3, vehicleNo);
            pstmt1.setDate(4, startDate);
            pstmt1.setDate(5, endDate);
            pstmt1.setString(6, "进行中");  
            pstmt1.setDouble(7, fee);

            int rowsAffected = pstmt1.executeUpdate();
            if (rowsAffected != 1) {
                throw new SQLException("订单插入失败，影响行数: " + rowsAffected);
            }

            String sql2 = "UPDATE vehicle SET Vstatus='已租' WHERE Vno=?";
            pstmt2 = conn.prepareStatement(sql2);
            pstmt2.setString(1, vehicleNo);
            rowsAffected = pstmt2.executeUpdate();
            if (rowsAffected != 1) {
                throw new SQLException("车辆状态更新失败，影响行数: " + rowsAffected);
            }

            conn.commit();
            System.out.println("订单创建及车辆状态更新成功，订单号: " + orderNo);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (conn != null) {
                    conn.rollback();
                    System.err.println("事务回滚，租借失败，数据未保存");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                System.err.println("事务回滚失败。");
            }
            JOptionPane.showMessageDialog(parentFrame, "租借失败：" + e.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            DBUtil.close(pstmt1);
            DBUtil.close(pstmt2);
            DBUtil.close(conn);
        }
    }

    // 支付订单方法
    public boolean payOrder(String orderNo) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = Connect.getConnection();

            // 查询订单状态和费用信息
            String sql = "SELECT Ostatus, Ofee FROM orders WHERE Ono = ?"; // 添加Ofee字段查询
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, orderNo);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                String orderStatus = rs.getString("Ostatus");
                double fee = rs.getDouble("Ofee"); // 获取费用信息
                
                // 检查订单状态是否为"待支付"
                if ("待支付".equals(orderStatus)) {
                    // 添加用户确认支付提示
                    int confirm = JOptionPane.showConfirmDialog(parentFrame,
                            "确认支付订单 " + orderNo + " 吗？\n" +
                            "支付金额：" + fee + "元",
                            "确认支付",
                            JOptionPane.YES_NO_OPTION);
                    
                    if (confirm != JOptionPane.YES_OPTION) {
                        return false; // 用户取消支付
                    }
                    
                    // 更新订单状态为"已完成"
                    String updateSql = "UPDATE orders SET Ostatus = '已完成' WHERE Ono = ?";
                    try (PreparedStatement updatePstmt = conn.prepareStatement(updateSql)) {
                        updatePstmt.setString(1, orderNo);
                        int rowsAffected = updatePstmt.executeUpdate();
                        
                        if (rowsAffected == 1) {
                            System.out.println("订单支付成功，订单号: " + orderNo);
                            
                            // 添加成功支付提示
                            JOptionPane.showMessageDialog(parentFrame, 
                                    "支付成功！订单已完成",
                                    "支付成功", 
                                    JOptionPane.INFORMATION_MESSAGE);
                            return true;
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(parentFrame, 
                            "该订单不是待支付状态，无法完成支付\n当前状态: " + orderStatus,
                            "错误", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(parentFrame, "未找到该订单，订单号: " + orderNo,
                        "错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame, "支付订单时出现数据库错误: " + e.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        } finally {
            DBUtil.close(rs);
            DBUtil.close(pstmt);
            DBUtil.close(conn);
        }
        return false;
    }
}