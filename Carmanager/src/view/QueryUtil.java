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
        COLUMN_MAPPING.put("customer", new String[]{"�ͻ����", "�ͻ�����", "�Ա�", "����", "�绰", "��ַ", "��ע"});
        COLUMN_MAPPING.put("vehicle", new String[]{"�������", "��������", "�ͺ�", "�۸�", "״̬", "վ����"}); // �޸�Ϊ��ʾվ������
        COLUMN_MAPPING.put("station", new String[]{"վ����","վ������", "��������"}); // �޸�Ϊ��ʾվ������
        COLUMN_MAPPING.put("orders", new String[]{"�������", "�ͻ����", "�������", "��ʼ����", "�黹����", "����״̬", "���"}); 
        COLUMN_MAPPING.put("employee", new String[]{"Ա�����", "Ա������", "Ա������"});
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

            // ʹ��ӳ���Ĭ������
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
            JOptionPane.showMessageDialog(table, "��ѯʧ��: " + e.getMessage(), "����", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}