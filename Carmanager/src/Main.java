import javax.swing.SwingUtilities;

import view.MainFrame;

public class Main {
    public static void main(String[] args) {
        // ʹ��SwingUtilitiesȷ��UI���¼������߳��д���
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainFrame();
            }
        });
    }
}