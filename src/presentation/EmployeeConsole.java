package presentation;

import model.Notification;
import model.User;
import presentation.employee.BookingWizard;
import service.INotificationService;
import service.NotificationService;
import util.InputValidation;
import java.util.List;

/**
 * EmployeeConsole — Menu router chính cho Employee.
 * Chỉ chịu trách nhiệm hiển thị menu, thông báo và điều hướng.
 *
 * Áp dụng: Single Responsibility Principle (SRP)
 */
public class EmployeeConsole {
    private static final INotificationService notificationService = new NotificationService();

    public static void displayMenu(User employee) {
        // Hiển thị thông báo chưa đọc khi vừa đăng nhập
        showUnreadNotifications(employee.getUserId());

        boolean back = false;
        while (!back) {
            System.out.println("\n========================================");
            System.out.println("  NHÂN VIÊN: " + employee.getUsername());
            System.out.println("========================================");
            System.out.println("1. Đặt phòng họp");
            System.out.println("2. Yêu cầu dịch vụ văn phòng (Phát sinh)");
            System.out.println("3. Lịch sử đặt phòng");
            System.out.println("4. Xem/Cập nhật hồ sơ cá nhân");
            System.out.println("0. Đăng xuất");
            System.out.print("Lựa chọn: ");

            int choice = InputValidation.inputInt();
            switch (choice) {
                case 1 -> BookingWizard.start(employee);
                case 2 -> System.out.println("=> Tính năng đang phát triển. Vui lòng đặt dịch vụ đi kèm ngay trong luồng đặt phòng mới!");
                case 3 -> BookingWizard.viewHistory(employee);
                case 4 -> ProfileConsole.manageProfile(employee);
                case 0 -> back = true;
                default -> System.out.println("Lựa chọn không hợp lệ!");
            }
        }
    }

    private static void showUnreadNotifications(int userId) {
        try {
            List<Notification> unread = notificationService.getUnreadNotifications(userId);
            if (!unread.isEmpty()) {
                System.out.println("\n***************************************************");
                System.out.println("BẠN CÓ " + unread.size() + " THÔNG BÁO MỚI TỪ HỆ THỐNG!");
                unread.forEach(n -> System.out.println(" - " + n.getMessage() + " (" + n.getCreatedAt() + ")"));
                System.out.println("***************************************************");
                notificationService.markAllAsRead(userId);
            }
        } catch (Exception e) {
            System.err.println("=> Không thể lấy thông báo: " + e.getMessage());
        }
    }
}
