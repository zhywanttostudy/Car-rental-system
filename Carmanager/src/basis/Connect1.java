package basis;

import java.sql.*;

public class Connect1 {
    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        // 加载数据库驱动
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        System.out.println("数据库驱动加载成功");
        
        // 修改为Class数据库
        String url = "jdbc:sqlserver://localhost:1433;DatabaseName=CarPark;trustServerCertificate=true";//DatabaseName=数据库名
        String user = "sa";
        String password = "75122138";
        
        // 建立数据库连接
        Connection con = DriverManager.getConnection(url, user, password);
        System.out.println("数据库连接成功");
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
            
            // 获取所有用户表
            DatabaseMetaData metaData = con.getMetaData();
            rs = metaData.getTables(null, null, null, new String[]{"TABLE"});
            
            // 遍历所有表
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                System.out.println("\n=== 表名: " + tableName + " ===");
                
                // 查询当前表的数据
                try (Statement tableStmt = con.createStatement();
                     ResultSet tableRs = tableStmt.executeQuery("SELECT * FROM " + tableName)) {
                    
                    // 获取列信息
                    ResultSetMetaData tableMetaData = tableRs.getMetaData();
                    int columnCount = tableMetaData.getColumnCount();
                    
                    // 打印表头
                    for (int i = 1; i <= columnCount; i++) {
                        System.out.print(tableMetaData.getColumnName(i) + "\t");
                    }
                    System.out.println();
                    
                    // 打印数据
                    while (tableRs.next()) {
                        for (int i = 1; i <= columnCount; i++) {
                            System.out.print(tableRs.getString(i) + "\t");
                        }
                        System.out.println();
                    }
                } catch (SQLException e) {
                    System.out.println("查询表 " + tableName + " 失败: " + e.getMessage());
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(con, stmt, rs);
        }
    }
}