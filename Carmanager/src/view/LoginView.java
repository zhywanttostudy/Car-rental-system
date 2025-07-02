package view;

import java.util.Scanner;
import entity.User;
import service.UserService;
import service.UserServiceImpl;

// ��¼��ͼ��
public class LoginView {
    private UserService userService = new UserServiceImpl();
    private Scanner scanner = new Scanner(System.in);
    
    public User showLoginPage() {
        System.out.println("\n----- ��¼ϵͳ -----");
        System.out.print("�������˺ţ�");
        String userId = scanner.nextLine();
        System.out.print("���������룺");
        String password = scanner.nextLine();
        
        return userService.login(userId, password);
    }
    
    public void showRegisterPage() {
        System.out.println("\n----- �ͻ�ע�� -----");
        System.out.print("�ͻ���ţ�");
        String cno = scanner.nextLine();
        System.out.print("������");
        String cname = scanner.nextLine();
        System.out.print("�Ա�");
        String csex = scanner.nextLine();
        System.out.print("���䣺");
        int cage = Integer.parseInt(scanner.nextLine());
        System.out.print("�绰��");
        String cphone = scanner.nextLine();
        System.out.print("סַ��");
        String caddress = scanner.nextLine();
        System.out.print("���룺");
        String cpass = scanner.nextLine();
        
        userService.registerCustomer(cno, cname, csex, cage, cphone, caddress, cpass);
    }
}