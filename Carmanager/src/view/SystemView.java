package view;

import java.util.Scanner;
import entity.*;

// ϵͳ��ͼ��
public class SystemView {
    private Scanner scanner = new Scanner(System.in);
    
    public void showCustomerMenu(Customer customer) {
        boolean running = true;
        while (running) {
            customer.enterSystem();
            System.out.print("��ѡ�������");
            String choice = scanner.nextLine();
            
            switch (choice) {
                case "1": System.out.println("�鿴���⳵��..."); break;
                case "2": System.out.println("�⳵����..."); break;
                case "3": System.out.println("�鿴����..."); break;
                case "4": running = false; break;
                default: System.out.println("��Чѡ��");
            }
        }
    }
    
    public void showEmployeeMenu(Employee employee) {
        boolean running = true;
        while (running) {
            employee.enterSystem();
            System.out.print("��ѡ�������");
            String choice = scanner.nextLine();
            
            switch (choice) {
                case "1": System.out.println("�鿴����..."); break;
                case "2": System.out.println("�鿴����..."); break;
                case "3": System.out.println("�鿴�ͻ�..."); break;
                case "4": System.out.println("��ӳ���..."); break;
                case "5": running = false; break;
                default: System.out.println("��Чѡ��");
            }
        }
    }
}
