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
    public void registerEmployee(String username, String password, String department, String contact, String phone) throws Exception {
        User newUser = new User();

        newUser.setUsername(username);

        String hashedPass = PasswordUtil.hashPassword(password);
        newUser.setPassword(hashedPass);
        newUser.setDepartment(department);
        newUser.setRole(Role.ADMIN);
        newUser.setContact(contact);
        newUser.setPhoneNumber(phone);

        userDAO.insertUser(newUser);
    }
    
    // Hàm xử lý nghiệp vụ đăng nhập
    public User login(String username, String password) throws Exception {
        User user = userDAO.getUserByUsername(username);
        if (user == null) {
            return null; // Không tồn tại user
        }
        
        // Kiểm tra mật khẩu (hash)
        boolean isMatch = PasswordUtil.checkPassword(password, user.getPassword());
        if (isMatch) {
            return user;
        }
        return null;
    }

    // Hàm xử lý cập nhật profile
    public boolean updateProfile(User user) throws Exception {
        return userDAO.updateUserProfile(user);
    }
}
