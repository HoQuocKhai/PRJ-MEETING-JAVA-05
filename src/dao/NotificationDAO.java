package dao;

import model.Notification;

import java.sql.SQLException;
import java.util.List;

public class NotificationDAO extends BaseDAO<Notification> {

    @Override
    protected Notification mapResultSetToObject(java.sql.ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setId(rs.getInt("id"));
        n.setUserId(rs.getInt("userId"));
        n.setMessage(rs.getString("message"));
        n.setRead(rs.getBoolean("isRead"));
        n.setCreatedAt(rs.getTimestamp("createdAt"));
        return n;
    }

    // Gửi thông báo mới
    public boolean insertNotification(int userId, String message) throws SQLException {
        String sql = "INSERT INTO notifications (userId, message) VALUES (?, ?)";
        return executeUpdate(sql, userId, message);
    }

    // Lấy danh sách thông báo CHƯA ĐỌC
    public List<Notification> getUnreadNotifications(int userId) throws SQLException {
        String sql = "SELECT * FROM notifications WHERE userId = ? AND isRead = FALSE ORDER BY createdAt DESC";
        return executeQuery(sql, userId);
    }

    // Đánh dấu tất cả là đã đọc
    public boolean markAllAsRead(int userId) throws SQLException {
        String sql = "UPDATE notifications SET isRead = TRUE WHERE userId = ? AND isRead = FALSE";
        return executeUpdate(sql, userId);
    }
}
