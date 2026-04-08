package service;

import dao.UserDAO;
import model.Role;
import model.User;
import util.PasswordUtil;

public class UserService {
    private final UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAO();
    }

    // Cung cấp hàm kiểm tra username cho giao diện sử dụng
    public boolean checkUsername(String username) throws Exception {
        return userDAO.isUsernameExist(username);
    }

    // Hàm xử lý nghiệp vụ đăng ký
    public void registerEmployee(String username, String password, String contact, String phone) throws Exception {
        User newUser = new User();

        newUser.setUsername(username);

        String hashedPass = PasswordUtil.hashPassword(password);
        newUser.setPassword(hashedPass);

        newUser.setRole(Role.EMPLOYEE);
        newUser.setContact(contact);
        newUser.setPhoneNumber(phone);

        userDAO.insertUser(newUser);
    }
}
