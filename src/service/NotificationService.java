package service;

import dao.NotificationDAO;
import model.Notification;

import java.util.List;

/**
 * Service layer cho Notification.
 * Presentation layer KHÔNG được gọi trực tiếp NotificationDAO.
 */
public class NotificationService implements INotificationService {
    private final NotificationDAO notificationDAO = new NotificationDAO();
    /**
     * Lấy danh sách thông báo chưa đọc của user.
     */
    public List<Notification> getUnreadNotifications(int userId) throws Exception {
        return notificationDAO.getUnreadNotifications(userId);
    }

    /**
     * I-H: Đánh dấu tất cả thông báo của user là đã đọc. Trả về boolean.
     */
    public boolean markAllAsRead(int userId) throws Exception {
        return notificationDAO.markAllAsRead(userId);
    }

    /**
     * I-C: Gửi thông báo mới. Cho phép BookingService dùng INotificationService
     * thay vì gọi NotificationDAO trực tiếp.
     */
    public boolean insertNotification(int userId, String message) throws Exception {
        return notificationDAO.insertNotification(userId, message);
    }
}
