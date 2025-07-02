// 计算租金

package service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import basis.Connect;
import basis.DBUtil;

public class RentStatisticsService {
    private Connection connection;

    public RentStatisticsService(Connection connection) {
        this.connection = connection;
    }

    // 计算某一时间段的租金
    public double calculateRentFee(String startDateStr, String endDateStr) throws ParseException, SQLException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = sdf.parse(startDateStr);
        Date endDate = sdf.parse(endDateStr);

        if (endDate.before(startDate)) {
            throw new IllegalArgumentException("结束日期不能早于开始日期");
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        double totalFee = 0;

        try {
            try {
                conn = Connect.getConnection();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                // 可以根据实际情况进行处理，例如抛出一个自定义异常或者返回一个默认值
                throw new SQLException("加载数据库驱动时出现异常: " + e.getMessage());
            }
            String sql = "SELECT o.Ostart, o.Oreturn, o.Ofee, v.Vprice " +
                    "FROM orders o " +
                    "JOIN vehicle v ON o.Vno = v.Vno " +
                    "WHERE o.Ostatus = '已完成'";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                java.sql.Date orderStartDate = rs.getDate("Ostart");
                java.sql.Date orderEndDate = rs.getDate("Oreturn");
                double orderFee = rs.getDouble("Ofee");
                double dailyPrice = rs.getDouble("Vprice");

                // 计算订单时间与统计时间的交集
                Date overlapStart = orderStartDate.after(startDate) ? orderStartDate : startDate;
                Date overlapEnd = orderEndDate.before(endDate) ? orderEndDate : endDate;

                if (overlapEnd.after(overlapStart)) {
                    long diffInMillis = overlapEnd.getTime() - overlapStart.getTime();
                    int overlapDays = (int) (diffInMillis / (1000 * 60 * 60 * 24));
                    if (overlapDays == 0) overlapDays = 1;

                    // 计算该订单在统计时间段内的租金
                    double overlapFee = dailyPrice * overlapDays;
                    totalFee += overlapFee;
                }
            }
        } finally {
            DBUtil.close(rs);
            DBUtil.close(pstmt);
            DBUtil.close(conn);
        }

        return totalFee;
    }
}