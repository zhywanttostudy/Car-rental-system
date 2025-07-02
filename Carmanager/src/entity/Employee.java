package entity;

// Ա��ʵ����
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
        System.out.println("��ӭԱ�� " + ename + " �������ϵͳ��");
        System.out.println("1. �鿴���� | 2. �鿴���� | 3. �鿴�ͻ� | 4. ��ӳ��� | 5. �˳�");
    }
}
