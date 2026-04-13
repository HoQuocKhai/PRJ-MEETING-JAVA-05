package service;

import model.Notification;
import java.util.List;

/**
 * Interface cho NotificationService.
 * Áp dụng Dependency Inversion Principle (DIP).
 */
public interface INotificationService {
    List<Notification> getUnreadNotifications(int userId) throws Exception;
    /** I-H fix: trả về boolean thay vì void để caller biết thao tác có thành công không */
    boolean markAllAsRead(int userId) throws Exception;
    /** I-C: thêm insertNotification để BookingService không cần inject NotificationDAO trực tiếp */
    boolean insertNotification(int userId, String message) throws Exception;
}
