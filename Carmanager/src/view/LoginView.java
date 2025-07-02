package view;

import java.util.Scanner;
import entity.User;
import service.UserService;
import service.UserServiceImpl;

// 登录视图类
public class LoginView {
    private UserService userService = new UserServiceImpl();
    private Scanner scanner = new Scanner(System.in);
    
    public User showLoginPage() {
        System.out.println("\n----- 登录系统 -----");
        System.out.print("请输入账号：");
        String userId = scanner.nextLine();
        System.out.print("请输入密码：");
        String password = scanner.nextLine();
        
        return userService.login(userId, password);
    }
    
    public void showRegisterPage() {
        System.out.println("\n----- 客户注册 -----");
        System.out.print("客户编号：");
        String cno = scanner.nextLine();
        System.out.print("姓名：");
        String cname = scanner.nextLine();
        System.out.print("性别：");
        String csex = scanner.nextLine();
        System.out.print("年龄：");
        int cage = Integer.parseInt(scanner.nextLine());
        System.out.print("电话：");
        String cphone = scanner.nextLine();
        System.out.print("住址：");
        String caddress = scanner.nextLine();
        System.out.print("密码：");
        String cpass = scanner.nextLine();
        
        userService.registerCustomer(cno, cname, csex, cage, cphone, caddress, cpass);
    }
}