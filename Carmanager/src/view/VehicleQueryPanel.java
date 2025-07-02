//查询面板
package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 车辆查询面板 - 封装车辆查询功能的UI组件和查询逻辑
 */
public class VehicleQueryPanel extends JPanel {
    private JComboBox<String> columnComboBox;
    private JTextField keywordField;
    private JButton queryBtn;
    private String[] vehicleColumns = {"全部", "车辆编号", "车辆名称", "型号", "价格", "状态", "站点编号"}; // 修改为查询站点名称
    private QueryUtil queryUtil;
    private JTable resultTable;
    private Connection connection;

    /**
     * 构造函数
     * @param queryUtil 查询工具类
     * @param resultTable 结果表格
     * @param connection 数据库连接
     */
    public VehicleQueryPanel(QueryUtil queryUtil, JTable resultTable, Connection connection) {
        this.queryUtil = queryUtil;
        this.resultTable = resultTable;
        this.connection = connection;
        initComponents();
        setupLayout();
        setupListeners();
    }

    private void initComponents() {
        columnComboBox = new JComboBox<>(vehicleColumns);
        columnComboBox.setSelectedIndex(0); // 默认选择"全部"
        keywordField = new JTextField(15);
        queryBtn = new JButton("查询");
    }

    private void setupLayout() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));
        add(new JLabel("查询列:"));
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
                    JOptionPane.showMessageDialog(VehicleQueryPanel.this, 
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
     * @param column 查询列
     * @param keyword 关键字
     */
    private void performQuery(String column, String keyword) {
        String sql;
        if ("全部".equals(column)) {
            // SQL Server 数据的全量查询，使用+进行字符串拼接
            sql = "SELECT * FROM vehicLe WHERE " +
                    "(Vno + Vname + ISNULL(VmodeL, '')+ CONVERT(VARCHAR, Vprice) + Vstatus + Sno) LIKE '%"+ keyword + "%'";
        } else {
            // 按指定列查询
            String dbColumn = columnToDBColumn(column);
            sql = "SELECT * FROM vehicle WHERE " + dbColumn + " LIKE '%" + keyword + "%'";

        }
        queryUtil.queryAndFillTable(resultTable, sql, "vehicle");
    }

    /**
     * 列名转换 - 将显示列名转换为数据库列名
     */
    private String columnToDBColumn(String displayName) {
        switch (displayName) {
            case "车辆编号": return "Vno";
            case "车辆名称": return "Vname";
            case "型号": return "Vmodel";
            case "价格": return "Vprice";
            case "状态": return "Vstatus";
            case "站点编号": return "Sno";
            default: return null;
        }
    }
}