package basis;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// 数据库连接工具类
public class Connect {
    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        // 加载数据库驱动
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        System.out.println("数据库驱动加载成功");

        // 修改为CarPark数据库
        String url = "jdbc:sqlserver://localhost:1433;DatabaseName=Car;trustServerCertificate=true";
        String user = "sa";
        String password = "tangyuan999";

        // 建立数据库连接
        Connection con = DriverManager.getConnection(url, user, password);
        System.out.println("数据库连接成功");
        return con;
    }

    // 关闭数据库资源
    public static void closeResources(Connection con, java.sql.Statement stmt, java.sql.ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (con != null) con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}