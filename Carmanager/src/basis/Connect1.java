package basis;

import java.sql.*;

public class Connect1 {
    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        // �������ݿ�����
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        System.out.println("���ݿ��������سɹ�");
        
        // �޸�ΪClass���ݿ�
        String url = "jdbc:sqlserver://localhost:1433;DatabaseName=CarPark;trustServerCertificate=true";//DatabaseName=���ݿ���
        String user = "sa";
        String password = "75122138";
        
        // �������ݿ�����
        Connection con = DriverManager.getConnection(url, user, password);
        System.out.println("���ݿ����ӳɹ�");
        return con;
    }
    
    public static void closeResources(Connection con, Statement stmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (con != null) con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            con = getConnection();
            
            // ��ȡ�����û���
            DatabaseMetaData metaData = con.getMetaData();
            rs = metaData.getTables(null, null, null, new String[]{"TABLE"});
            
            // �������б�
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                System.out.println("\n=== ����: " + tableName + " ===");
                
                // ��ѯ��ǰ�������
                try (Statement tableStmt = con.createStatement();
                     ResultSet tableRs = tableStmt.executeQuery("SELECT * FROM " + tableName)) {
                    
                    // ��ȡ����Ϣ
                    ResultSetMetaData tableMetaData = tableRs.getMetaData();
                    int columnCount = tableMetaData.getColumnCount();
                    
                    // ��ӡ��ͷ
                    for (int i = 1; i <= columnCount; i++) {
                        System.out.print(tableMetaData.getColumnName(i) + "\t");
                    }
                    System.out.println();
                    
                    // ��ӡ����
                    while (tableRs.next()) {
                        for (int i = 1; i <= columnCount; i++) {
                            System.out.print(tableRs.getString(i) + "\t");
                        }
                        System.out.println();
                    }
                } catch (SQLException e) {
                    System.out.println("��ѯ�� " + tableName + " ʧ��: " + e.getMessage());
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(con, stmt, rs);
        }
    }
}