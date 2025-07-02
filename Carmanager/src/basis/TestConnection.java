package basis;
import java.sql.*;

public class TestConnection {
	public static void main(String[] args) {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=Car;trustServerCertificate=true";
        String user = "sa";
        String password = "tangyuan999";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("连接成功！");
        } catch (SQLException e) {
            System.out.println("连接失败：" + e.getMessage());
            e.printStackTrace();
        }
    }
}
