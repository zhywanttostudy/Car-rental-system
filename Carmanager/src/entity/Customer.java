package entity;

// 客户实体类
public class Customer implements User {
    private String cno;
    private String cname;
    private String cpass;
    
    public Customer(String cno, String cname, String cpass) {
        this.cno = cno;
        this.cname = cname;
        this.cpass = cpass;
    }
    
    @Override public String getUserId() { return cno; }
    @Override public String getPassword() { return cpass; }
    
    @Override
    public void enterSystem() {
        System.out.println("欢迎客户 " + cname + " 进入租车系统！");
        System.out.println("1. 查看可租车辆 | 2. 租车 | 3. 查看订单 | 4. 退出");
    }
}