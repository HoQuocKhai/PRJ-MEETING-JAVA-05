package service;

import dao.UserDAO;
import model.Enum.Role;
import model.User;
import util.PasswordUtil;

public class UserService implements IUserService {
    private final UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAO();
    }

    // Cung cấp hàm kiểm tra username cho giao diện sử dụng
    public boolean checkUsername(String username) throws Exception {
        return userDAO.isUsernameExist(username);
    }

    // Hàm xử lý nghiệp vụ đăng ký
    private void createUser(String username, String password, Role role, String department, String contact, String phone) throws Exception {
        User newUser = new User();
        newUser.setUsername(username);

        String hashedPass = PasswordUtil.hashPassword(password);
        newUser.setPassword(hashedPass);
        newUser.setDepartment(department);
        newUser.setRole(role);
        newUser.setContact(contact);
        newUser.setPhoneNumber(phone);

        userDAO.insertUser(newUser);
    }

    // Hàm dành cho nhân viên tự đăng ký
    public void registerEmployee(String username, String password, String department, String contact, String phone) throws Exception {
        createUser(username, password, Role.EMPLOYEE, department, contact, phone);
    }

    // Hàm dành cho Admin tạo tài khoản nội bộ
    public void createStaffAdmin(String username, String password, Role selectedRole, String department, String contact, String phone) throws Exception {
        createUser(username, password, selectedRole, department, contact, phone);
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

    // Hàm lấy danh sách Support Staff
    public java.util.List<User> getSupportStaffs() throws Exception {
        return userDAO.getSupportStaffs();
    }

    // ========== Admin User Management ==========

    // Hàm lấy toàn bộ danh sách người dùng
    public java.util.List<User> getAllUsers() throws Exception {
        return userDAO.getAllUsers();
    }

    // Hàm xóa người dùng theo ID
    public boolean deleteUser(int userId) throws Exception {
        return userDAO.deleteUser(userId);
    }

    // Hàm cập nhật thông tin user do admin thực hiện (department, contact, phone, role)
    public boolean updateUserByAdmin(User user) throws Exception {
        return userDAO.updateUserByAdmin(user);
    }

    // Hàm lấy user theo ID
    public User getUserById(int userId) throws Exception {
        return userDAO.getUserById(userId);
    }
}
