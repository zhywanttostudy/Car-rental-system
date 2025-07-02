package entity;

// �ͻ�ʵ����
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
        System.out.println("��ӭ�ͻ� " + cname + " �����⳵ϵͳ��");
        System.out.println("1. �鿴���⳵�� | 2. �⳵ | 3. �鿴���� | 4. �˳�");
    }
}