package service;

import java.sql.*;
import entity.*;
import basis.*;

// 用户服务实现类
public class UserServiceImpl implements UserService {
    @Override
    public User login(String userId, String password) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        User user = null;
        
        try {
            conn = Connect.getConnection();
            
            // 检查员工表
            String empSql = "SELECT Eno, Ename, Epass FROM employee WHERE Eno=? AND Epass=?";
            stmt = conn.prepareStatement(empSql);
            stmt.setString(1, userId);
            stmt.setString(2, password);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                user = new Employee(rs.getString("Eno"), rs.getString("Ename"), rs.getString("Epass"));
                return user;
            }
            
            // 检查客户表
            rs.close();
            stmt.close();
            
            String custSql = "SELECT Cno, Cname, Cpass FROM customer WHERE Cno=? AND Cpass=?";
            stmt = conn.prepareStatement(custSql);
            stmt.setString(1, userId);
            stmt.setString(2, password);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                user = new Customer(rs.getString("Cno"), rs.getString("Cname"), rs.getString("Cpass"));
                return user;
            }
            
            System.out.println("登录失败：用户名或密码错误");
        } catch (Exception e) {
            System.out.println("登录异常：" + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        
        return null;
    }
    
    @Override
    public boolean registerCustomer(String cno, String cname, String csex, int cage, 
                                  String cphone, String caddress, String cpass) {
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean result = false;
        
        try {
            conn = Connect.getConnection();
            String sql = "INSERT INTO customer VALUES (?, ?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, cno);
            stmt.setString(2, cname);
            stmt.setString(3, csex);
            stmt.setInt(4, cage);
            stmt.setString(5, cphone);
            stmt.setString(6, caddress);
            stmt.setString(7, cpass);
            
            result = stmt.executeUpdate() > 0;
            System.out.println(result ? "注册成功！" : "注册失败：客户编号可能已存在");
        } catch (Exception e) {
            System.out.println("注册异常：" + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, stmt, null);
        }
        
        return result;
    }
}
