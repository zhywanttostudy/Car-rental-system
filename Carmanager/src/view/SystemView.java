package view;

import java.util.Scanner;
import entity.*;

// 系统视图类
public class SystemView {
    private Scanner scanner = new Scanner(System.in);
    
    public void showCustomerMenu(Customer customer) {
        boolean running = true;
        while (running) {
            customer.enterSystem();
            System.out.print("请选择操作：");
            String choice = scanner.nextLine();
            
            switch (choice) {
                case "1": System.out.println("查看可租车辆..."); break;
                case "2": System.out.println("租车功能..."); break;
                case "3": System.out.println("查看订单..."); break;
                case "4": running = false; break;
                default: System.out.println("无效选择");
            }
        }
    }
    
    public void showEmployeeMenu(Employee employee) {
        boolean running = true;
        while (running) {
            employee.enterSystem();
            System.out.print("请选择操作：");
            String choice = scanner.nextLine();
            
            switch (choice) {
                case "1": System.out.println("查看车辆..."); break;
                case "2": System.out.println("查看订单..."); break;
                case "3": System.out.println("查看客户..."); break;
                case "4": System.out.println("添加车辆..."); break;
                case "5": running = false; break;
                default: System.out.println("无效选择");
            }
        }
    }
}
