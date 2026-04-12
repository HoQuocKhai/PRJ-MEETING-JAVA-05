package presentation;

import model.Booking;
import model.User;
import model.Enum.PreparationStatus;
import service.SupportStaffService;
import util.InputValidation;
import java.util.List;

public class SupportStaffConsole {
    private static final SupportStaffService supportService = new SupportStaffService();

    public static void displayMenu(User support) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- NHÂN VIÊN HỖ TRỢ: " + support.getUsername() + " ---");
            System.out.println("1. Xem và xử lý danh sách nhiệm vụ được phân công.");
            System.out.println("0. Đăng xuất");
            System.out.print("Lựa chọn: ");

            int choice = InputValidation.inputInt();
            switch (choice) {
                case 1 -> handleSupportTasks(support);
                case 0 -> back = true;
                default -> System.out.println("Lựa chọn không hợp lệ!");
            }
        }
    }

    private static void handleSupportTasks(User support) {
        try {
            List<Booking> tasks = supportService.getTasksByStaffId(support.getUserId());
            if (tasks.isEmpty()) {
                System.out.println("=> Tuyệt vời! Hiện tại bạn không có nhiệm vụ nào cần chuẩn bị.");
                return;
            }

            System.out.println("\n--- BẢNG DANH SÁCH NHIỆM VỤ ĐANG CHỜ ---");
            System.out.printf("%-10s | %-10s | %-20s | %-20s\n", "Booking ID", "Room ID", "Bắt đầu", "Trạng thái Prep");
            for (Booking b : tasks) {
                System.out.printf("%-10d | %-10d | %-20s | %-20s\n", 
                        b.getBookingId(), b.getRoomId(), b.getStartTime(), b.getPreparationStatus());
            }

            System.out.print("\nNhập ID Booking bạn muốn xử lý (hoặc 0 để thoát): ");
            int bookingId = InputValidation.inputInt();
            if (bookingId == 0) return;

            boolean valid = tasks.stream().anyMatch(t -> t.getBookingId() == bookingId);
            if (!valid) {
                System.out.println("=> Lỗi: ID không nằm trong danh sách nhiệm vụ của bạn.");
                return;
            }

            // In chi tiết
            supportService.printTaskDetails(bookingId);

            // Hỏi cập nhật trạng thái
            System.out.println("\nBạn muốn cập nhật màn hình trạng thái chuẩn bị?");
            System.out.println("1. Bắt đầu vào việc (Chuyển sang PREPARING)");
            System.out.println("2. Đã xong tất cả (Chuyển sang READY)");
            System.out.println("3. Báo sự cố / Thiếu thiết bị (MISSING_EQUIPMENT)");
            System.out.print("Lựa chọn: ");
            
            int action = InputValidation.inputInt();
            PreparationStatus newStatus = null;
            switch (action) {
                case 1 -> newStatus = PreparationStatus.PREPARING;
                case 2 -> newStatus = PreparationStatus.READY;
                case 3 -> newStatus = PreparationStatus.MISSING_EQUIPMENT;
                default -> {
                    System.out.println("=> Lựa chọn không hợp lệ. Hủy thay đổi.");
                    return;
                }
            }

            if (supportService.updateTaskStatus(bookingId, newStatus)) {
                System.out.println("=> CẬP NHẬT TRẠNG THÁI THÀNH " + newStatus + " THÀNH CÔNG!");
            } else {
                System.out.println("=> Cập nhật thất bại, vui lòng thử lại.");
            }

        } catch (Exception e) {
            System.out.println("=> Lỗi hệ thống: " + e.getMessage());
        }
    }
}
