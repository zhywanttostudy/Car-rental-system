package entity;

// 员工实体类
public class Employee implements User {
    private String eno;
    private String ename;
    private String epass;
    
    public Employee(String eno, String ename, String epass) {
        this.eno = eno;
        this.ename = ename;
        this.epass = epass;
    }
    
    @Override public String getUserId() { return eno; }
    @Override public String getPassword() { return epass; }
    
    @Override
    public void enterSystem() {
        System.out.println("欢迎员工 " + ename + " 进入管理系统！");
        System.out.println("1. 查看车辆 | 2. 查看订单 | 3. 查看客户 | 4. 添加车辆 | 5. 退出");
    }
}
