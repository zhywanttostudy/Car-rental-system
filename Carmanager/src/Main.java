import javax.swing.SwingUtilities;

import view.MainFrame;

public class Main {
    public static void main(String[] args) {
        // 使用SwingUtilities确保UI在事件调度线程中创建
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainFrame();
            }
        });
    }
}