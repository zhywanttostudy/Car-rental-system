package view;


import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class QueryUtil {
    private Connection connection;
    private static final Map<String, String[]> COLUMN_MAPPING = new HashMap<>();

    static {
        COLUMN_MAPPING.put("customer", new String[]{"客户编号", "客户姓名", "性别", "年龄", "电话", "地址", "备注"});
        COLUMN_MAPPING.put("vehicle", new String[]{"车辆编号", "车辆名称", "型号", "价格", "状态", "站点编号"}); // 修改为显示站点名称
        COLUMN_MAPPING.put("station", new String[]{"站点编号","站点名称", "车辆数量"}); // 修改为显示站点名称
        COLUMN_MAPPING.put("orders", new String[]{"订单编号", "客户编号", "车辆编号", "开始日期", "归还日期", "订单状态", "金额"}); 
        COLUMN_MAPPING.put("employee", new String[]{"员工编号", "员工姓名", "员工密码"});
    }

    public QueryUtil(Connection connection) {
        this.connection = connection;
    }

    public boolean queryTable(JTable table, String tableName) {
        String sql;

            sql = "SELECT * FROM " + tableName;

        return queryAndFillTable(table, sql, tableName);
    }

    boolean queryAndFillTable(JTable table, String sql, String tableName) {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            DefaultTableModel model = new DefaultTableModel();
            table.setModel(model);

            // 使用映射的默认列名
            String[] columnNames = COLUMN_MAPPING.getOrDefault(tableName, new String[columnCount]);
            if (columnNames.length != columnCount) {
                columnNames = new String[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    columnNames[i-1] = metaData.getColumnName(i);
                }
            }

            model.setColumnIdentifiers(columnNames);

            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i-1] = rs.getObject(i);
                }
                model.addRow(row);
            }

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(table, "查询失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}