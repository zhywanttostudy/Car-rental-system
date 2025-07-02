// �������

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

    // ����ĳһʱ��ε����
    public double calculateRentFee(String startDateStr, String endDateStr) throws ParseException, SQLException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = sdf.parse(startDateStr);
        Date endDate = sdf.parse(endDateStr);

        if (endDate.before(startDate)) {
            throw new IllegalArgumentException("�������ڲ������ڿ�ʼ����");
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
                // ���Ը���ʵ��������д��������׳�һ���Զ����쳣���߷���һ��Ĭ��ֵ
                throw new SQLException("�������ݿ�����ʱ�����쳣: " + e.getMessage());
            }
            String sql = "SELECT o.Ostart, o.Oreturn, o.Ofee, v.Vprice " +
                    "FROM orders o " +
                    "JOIN vehicle v ON o.Vno = v.Vno " +
                    "WHERE o.Ostatus = '�����'";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                java.sql.Date orderStartDate = rs.getDate("Ostart");
                java.sql.Date orderEndDate = rs.getDate("Oreturn");
                double orderFee = rs.getDouble("Ofee");
                double dailyPrice = rs.getDouble("Vprice");

                // ���㶩��ʱ����ͳ��ʱ��Ľ���
                Date overlapStart = orderStartDate.after(startDate) ? orderStartDate : startDate;
                Date overlapEnd = orderEndDate.before(endDate) ? orderEndDate : endDate;

                if (overlapEnd.after(overlapStart)) {
                    long diffInMillis = overlapEnd.getTime() - overlapStart.getTime();
                    int overlapDays = (int) (diffInMillis / (1000 * 60 * 60 * 24));
                    if (overlapDays == 0) overlapDays = 1;

                    // ����ö�����ͳ��ʱ����ڵ����
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