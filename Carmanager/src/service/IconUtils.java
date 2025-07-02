package service;

import javax.swing.*;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;

public class IconUtils {
    /**
     * Ϊ��ť���ͼ�꣨�Ż�·�����쳣����
     */
    public static void addIconToButton(JButton button, String iconPath) {
        try {
            // �Ż�ͼ��·�����ط�ʽ��ʹ�����������ȡ����·��
            URL iconUrl = IconUtils.class.getResource(iconPath);
            if (iconUrl == null) {
                // ���ԴӸ�Ŀ¼���أ�ȥ����ͷ��'/'��
                iconUrl = IconUtils.class.getResource(iconPath.substring(1));
            }
            
            if (iconUrl == null) {
                System.err.println("����ͼ���ļ�δ�ҵ���·����" + iconPath);
                return;
            }
            
            ImageIcon originalIcon = new ImageIcon(iconUrl);
            if (originalIcon.getIconWidth() <= 0 || originalIcon.getIconHeight() <= 0) {
                System.err.println("����ͼ���ļ���Ч��·����" + iconPath);
                return;
            }
            
            // ����Ĭ��ͼ���С�����ⰴť�߶�Ϊ0ʱ�����⣩
            int iconSize = Math.max(button.getHeight(), 16); // ����16x16����
            Image scaledImage = originalIcon.getImage().getScaledInstance(
                    iconSize, iconSize, Image.SCALE_SMOOTH);
            ImageIcon scaledIcon = new ImageIcon(scaledImage);
            
            button.setIcon(scaledIcon);
            button.setText(button.getText());
            button.setHorizontalTextPosition(SwingConstants.RIGHT);
            button.setVerticalTextPosition(SwingConstants.CENTER);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("ͼ������쳣��·����" + iconPath + "������" + e.getMessage());
        }
    }
}
