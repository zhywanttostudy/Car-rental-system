//��ѯ���
package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * ������ѯ��� - ��װ������ѯ���ܵ�UI����Ͳ�ѯ�߼�
 */
public class VehicleQueryPanel extends JPanel {
    private JComboBox<String> columnComboBox;
    private JTextField keywordField;
    private JButton queryBtn;
    private String[] vehicleColumns = {"ȫ��", "�������", "��������", "�ͺ�", "�۸�", "״̬", "վ����"}; // �޸�Ϊ��ѯվ������
    private QueryUtil queryUtil;
    private JTable resultTable;
    private Connection connection;

    /**
     * ���캯��
     * @param queryUtil ��ѯ������
     * @param resultTable ������
     * @param connection ���ݿ�����
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
        columnComboBox.setSelectedIndex(0); // Ĭ��ѡ��"ȫ��"
        keywordField = new JTextField(15);
        queryBtn = new JButton("��ѯ");
    }

    private void setupLayout() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));
        add(new JLabel("��ѯ��:"));
        add(columnComboBox);
        add(new JLabel("�ؼ���:"));
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
                                                  "�ؼ��ֲ���Ϊ��", 
                                                  "��ʾ", 
                                                  JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                String selectedColumn = (String) columnComboBox.getSelectedItem();
                performQuery(selectedColumn, keyword);
            }
        });
    }

    /**
     * ִ�в�ѯ
     * @param column ��ѯ��
     * @param keyword �ؼ���
     */
    private void performQuery(String column, String keyword) {
        String sql;
        if ("ȫ��".equals(column)) {
            // SQL Server ���ݵ�ȫ����ѯ��ʹ��+�����ַ���ƴ��
            sql = "SELECT * FROM vehicLe WHERE " +
                    "(Vno + Vname + ISNULL(VmodeL, '')+ CONVERT(VARCHAR, Vprice) + Vstatus + Sno) LIKE '%"+ keyword + "%'";
        } else {
            // ��ָ���в�ѯ
            String dbColumn = columnToDBColumn(column);
            sql = "SELECT * FROM vehicle WHERE " + dbColumn + " LIKE '%" + keyword + "%'";

        }
        queryUtil.queryAndFillTable(resultTable, sql, "vehicle");
    }

    /**
     * ����ת�� - ����ʾ����ת��Ϊ���ݿ�����
     */
    private String columnToDBColumn(String displayName) {
        switch (displayName) {
            case "�������": return "Vno";
            case "��������": return "Vname";
            case "�ͺ�": return "Vmodel";
            case "�۸�": return "Vprice";
            case "״̬": return "Vstatus";
            case "վ����": return "Sno";
            default: return null;
        }
    }
}