package basis;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// ���ݿ����ӹ�����
public class Connect {
    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        // �������ݿ�����
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        System.out.println("���ݿ��������سɹ�");

        // �޸�ΪCarPark���ݿ�
        String url = "jdbc:sqlserver://localhost:1433;DatabaseName=Car;trustServerCertificate=true";
        String user = "sa";
        String password = "tangyuan999";

        // �������ݿ�����
        Connection con = DriverManager.getConnection(url, user, password);
        System.out.println("���ݿ����ӳɹ�");
        return con;
    }

    // �ر����ݿ���Դ
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