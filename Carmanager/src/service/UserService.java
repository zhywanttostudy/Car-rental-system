package service;

import entity.User;

// 用户服务接口
public interface UserService {
    User login(String userId, String password);
    boolean registerCustomer(String cno, String cname, String csex, int cage, 
                            String cphone, String caddress, String cpass);
}