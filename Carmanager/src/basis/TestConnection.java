package basis;
import java.sql.*;

public class TestConnection {
	public static void main(String[] args) {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=Car;trustServerCertificate=true";
        String user = "sa";
        String password = "tangyuan999";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("���ӳɹ���");
        } catch (SQLException e) {
            System.out.println("����ʧ�ܣ�" + e.getMessage());
            e.printStackTrace();
        }
    }
}
