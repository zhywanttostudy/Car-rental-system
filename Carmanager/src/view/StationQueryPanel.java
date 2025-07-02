package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 站点查询面板 - 封装站点查询功能的UI组件和查询逻辑
 */
public class StationQueryPanel extends JPanel {
    private JComboBox<String> columnComboBox;
    private JTextField keywordField;
    private JButton queryBtn;
    private String[] stationColumns = {"全部", "站点名称", "车辆数量"}; // 修改为查询站点名称
    private QueryUtil queryUtil;
    private JTable resultTable;
    private Connection connection;

    /**
     * 构造函数
     * @param queryUtil 查询工具类
     * @param resultTable 结果表格
     * @param connection 数据库连接
     */
    public StationQueryPanel(QueryUtil queryUtil, JTable resultTable, Connection connection) {
        this.queryUtil = queryUtil;
        this.resultTable = resultTable;
        this.connection = connection;
        initComponents();
        setupLayout();
        setupListeners();
    }

    private void initComponents() {
        columnComboBox = new JComboBox<>(stationColumns);
        columnComboBox.setSelectedIndex(0); // 默认选择"全部"
        keywordField = new JTextField(15);
        queryBtn = new JButton("查询");
    }

    private void setupLayout() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));
        add(new JLabel("查询字段:"));
        add(columnComboBox);
        add(new JLabel("关键字:"));
        add(keywordField);
        add(queryBtn);
    }

    private void setupListeners() {
        queryBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String keyword = keywordField.getText().trim();
                if (keyword.isEmpty()) {
                    JOptionPane.showMessageDialog(StationQueryPanel.this, 
                                                  "关键字不能为空", 
                                                  "提示", 
                                                  JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                String selectedColumn = (String) columnComboBox.getSelectedItem();
                performQuery(selectedColumn, keyword);
            }
        });
    }

    /**
     * 执行查询
     * @param column 查询字段
     * @param keyword 关键字
     */
    private void performQuery(String column, String keyword) {
        String sql;
        if ("全部".equals(column)) {
            // SQL Server 字符串拼接全量查询，使用+进行拼接
            sql = "SELECT * FROM station WHERE " +
                  "(Sname + CONVERT(VARCHAR, Vcount)) LIKE '%" + keyword + "%'"; // 修改为查询站点名称
        } else {
            // 按指定字段查询
            String dbColumn = columnToDBColumn(column);
            if (dbColumn == null) return;
            sql = "SELECT * FROM station WHERE " + dbColumn + " LIKE '%" + keyword + "%'";
        }
        queryUtil.queryAndFillTable(resultTable, sql, "station");
    }

    /**
     * 列名转换 - 将显示列名转换为数据库列名
     */
    private String columnToDBColumn(String displayName) {
        switch (displayName) {
            case "站点名称": return "Sname";
            case "车辆数量": return "Vcount";
            default: return null;
        }
    }
}