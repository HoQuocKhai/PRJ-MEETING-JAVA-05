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
        String sql = "INSERT INTO users (username, password, roleUser, contact, phoneNumber) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRole().name());
            pstmt.setString(4, user.getContact());
            pstmt.setString(5, user.getPhoneNumber());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new Exception("Lỗi Database khi thêm user: " + e.getMessage());
        }
    }
}
