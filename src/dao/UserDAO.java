package dao;

import model.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UserDAO extends BaseDAO<User> {

    @Override
    protected User mapResultSetToObject(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("userId"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setDepartment(rs.getString("department"));
        
        String roleStr = rs.getString("roleUser");
        if (roleStr != null) {
            user.setRole(model.Enum.Role.valueOf(roleStr));
        }
        
        user.setContact(rs.getString("contact"));
        user.setPhoneNumber(rs.getString("phoneNumber"));
        return user;
    }

    // Hàm kiểm tra username đã tồn tại chưa
    public boolean isUsernameExist(String username) throws Exception {
        User u = executeQueryForSingleObject("SELECT * FROM users WHERE username = ?", username);
        return u != null;
    }

    // Hàm thêm User mới vào Database
    public boolean insertUser(User user) throws Exception {
        String sql = "INSERT INTO users (username, password, department, roleUser, contact, phoneNumber) VALUES (?, ?, ?, ?, ?, ?)";
        return executeUpdate(sql, user.getUsername(), user.getPassword(), user.getDepartment(), user.getRole().name(), user.getContact(), user.getPhoneNumber());
    }

    // Hàm lấy thông tin User theo username
    public User getUserByUsername(String username) throws Exception {
        return executeQueryForSingleObject("SELECT * FROM users WHERE username = ?", username);
    }

    // Hàm lấy User theo ID
    public User getUserById(int userId) throws Exception {
        return executeQueryForSingleObject("SELECT * FROM users WHERE userId = ?", userId);
    }

    // Hàm lấy toàn bộ danh sách người dùng (sắp xếp theo role rồi username)
    public List<User> getAllUsers() throws Exception {
        return executeQuery("SELECT * FROM users ORDER BY roleUser, username");
    }

    // Hàm cập nhật thông tin User do Admin chỉnh (department, contact, phone, role)
    public boolean updateUserByAdmin(User user) throws Exception {
        String sql = "UPDATE users SET department = ?, contact = ?, phoneNumber = ?, roleUser = ? WHERE userId = ?";
        return executeUpdate(sql, user.getDepartment(), user.getContact(), user.getPhoneNumber(), user.getRole().name(), user.getUserId());
    }

    // Hàm cập nhật thông tin User (dành cho chính user tự sửa profile)
    public boolean updateUserProfile(User user) throws Exception {
        String sql = "UPDATE users SET department = ?, contact = ?, phoneNumber = ? WHERE userId = ?";
        return executeUpdate(sql, user.getDepartment(), user.getContact(), user.getPhoneNumber(), user.getUserId());
    }

    // Hàm xóa User theo ID
    public boolean deleteUser(int userId) throws Exception {
        return executeUpdate("DELETE FROM users WHERE userId = ?", userId);
    }

    // Hàm lấy danh sách Support Staff (dùng cho booking)
    public List<User> getSupportStaffs() throws Exception {
        return executeQuery("SELECT * FROM users WHERE roleUser = 'SUPPORT_STAFF'");
    }
}
