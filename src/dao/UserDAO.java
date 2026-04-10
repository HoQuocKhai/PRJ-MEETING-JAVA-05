package dao;

import model.User;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
    // Hàm kiểm tra username đã tồn tại chưa
    public boolean isUsernameExist(String username) throws Exception {
        String sql = "SELECT userId FROM users WHERE username = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new Exception("Lỗi Database khi kiểm tra username: " + e.getMessage());
        }
    }

    // Hàm thêm User mới vào Database
    public boolean insertUser(User user) throws Exception {
        String sql = "INSERT INTO users (username, password, department, roleUser, contact, phoneNumber) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getDepartment());
            pstmt.setString(4, user.getRole().name());
            pstmt.setString(5, user.getContact());
            pstmt.setString(6, user.getPhoneNumber());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new Exception("Lỗi Database khi thêm user: " + e.getMessage());
        }
    }

    // Hàm lấy thông tin User theo username
    public User getUserByUsername(String username) throws Exception {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
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
                return null;
            }
        } catch (SQLException e) {
            throw new Exception("Lỗi Database khi lấy thộng tin user: " + e.getMessage());
        }
    }

    // Hàm cập nhật thông tin User
    public boolean updateUserProfile(User user) throws Exception {
        String sql = "UPDATE users SET department = ?, contact = ?, phoneNumber = ? WHERE userId = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, user.getDepartment());
            pstmt.setString(2, user.getContact());
            pstmt.setString(3, user.getPhoneNumber());
            pstmt.setInt(4, user.getUserId());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new Exception("Lỗi Database khi cập nhật user: " + e.getMessage());
        }
    }
}
