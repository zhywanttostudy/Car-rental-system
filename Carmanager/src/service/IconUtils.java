package service;

import javax.swing.*;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;

public class IconUtils {
    /**
     * 为按钮添加图标（优化路径和异常处理）
     */
    public static void addIconToButton(JButton button, String iconPath) {
        try {
            // 优化图标路径加载方式，使用类加载器获取绝对路径
            URL iconUrl = IconUtils.class.getResource(iconPath);
            if (iconUrl == null) {
                // 尝试从根目录加载（去掉开头的'/'）
                iconUrl = IconUtils.class.getResource(iconPath.substring(1));
            }
            
            if (iconUrl == null) {
                System.err.println("错误：图标文件未找到，路径：" + iconPath);
                return;
            }
            
            ImageIcon originalIcon = new ImageIcon(iconUrl);
            if (originalIcon.getIconWidth() <= 0 || originalIcon.getIconHeight() <= 0) {
                System.err.println("错误：图标文件无效，路径：" + iconPath);
                return;
            }
            
            // 设置默认图标大小（避免按钮高度为0时的问题）
            int iconSize = Math.max(button.getHeight(), 16); // 至少16x16像素
            Image scaledImage = originalIcon.getImage().getScaledInstance(
                    iconSize, iconSize, Image.SCALE_SMOOTH);
            ImageIcon scaledIcon = new ImageIcon(scaledImage);
            
            button.setIcon(scaledIcon);
            button.setText(button.getText());
            button.setHorizontalTextPosition(SwingConstants.RIGHT);
            button.setVerticalTextPosition(SwingConstants.CENTER);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("图标加载异常，路径：" + iconPath + "，错误：" + e.getMessage());
        }
    }
}
