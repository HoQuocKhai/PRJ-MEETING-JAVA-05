package presentation.staff;

import model.Booking;
import model.dto.BookingEquipmentDetail;
import model.dto.BookingServiceDetail;
import model.User;
import model.Enum.PreparationStatus;
import service.ISupportStaffService;
import service.SupportStaffService;
import util.InputValidation;
import java.util.List;

/**
 * Tách từ SupportStaffConsole để tuân thủ Single Responsibility Principle.
 * Chứa toàn bộ logic xem & cập nhật trạng thái nhiệm vụ được phân công.
 */
public class TaskManagement {
    private static final ISupportStaffService supportService = new SupportStaffService();

    public static void manage(User support) {
        try {
            // 1. Lấy và hiển thị danh sách nhiệm vụ
            List<Booking> tasks = supportService.getTasksByStaffId(support.getUserId());
            if (tasks.isEmpty()) {
                System.out.println("=> Tuyệt vời! Hiện tại bạn không có nhiệm vụ nào cần chuẩn bị.");
                return;
            }
            printTaskList(tasks);

            // 2. Chọn Booking cần xử lý
            System.out.print("\nNhập ID Booking bạn muốn xử lý (hoặc 0 để thoát): ");
            int bookingId = InputValidation.inputInt();
            if (bookingId == 0) return;

            boolean valid = tasks.stream().anyMatch(t -> t.getBookingId() == bookingId);
            if (!valid) {
                System.out.println("=> Lỗi: ID không nằm trong danh sách nhiệm vụ của bạn.");
                return;
            }

            // 3. In chi tiết nhiệm vụ
            printTaskDetail(bookingId);

            // 4. Cập nhật trạng thái
            updateStatus(bookingId);

        } catch (Exception e) {
            System.out.println("=> Lỗi hệ thống: " + e.getMessage());
        }
    }

    // ---- Private helpers ----

    private static void printTaskList(List<Booking> tasks) {
        System.out.println("\n--- BẢNG DANH SÁCH NHIỆM VỤ ĐANG CHỜ ---");
        System.out.printf("%-12s | %-10s | %-22s | %-20s\n", "Booking ID", "Room ID", "Bắt đầu", "Trạng thái Prep");
        System.out.println("----------------------------------------------------------------------");
        for (Booking b : tasks) {
            System.out.printf("%-12d | %-10d | %-22s | %-20s\n",
                    b.getBookingId(), b.getRoomId(), b.getStartTime(), b.getPreparationStatus());
        }
    }

    private static void printTaskDetail(int bookingId) throws Exception {
        Booking b = supportService.getBookingInfo(bookingId);
        if (b == null) {
            System.out.println("=> Không tìm thấy Booking ID!");
            return;
        }

        System.out.println("\n--- CHI TIẾT NHIỆM VỤ (BOOKING ID: " + bookingId + ") ---");
        System.out.println("- Phòng (Room ID):       " + b.getRoomId());
        System.out.println("- Thời gian bắt đầu:     " + b.getStartTime());
        System.out.println("- Trạng thái hiện tại:   " + b.getPreparationStatus());

        // Thiết bị cần chuẩn bị
        List<BookingEquipmentDetail> equipments = supportService.getTaskEquipments(bookingId);
        if (equipments.isEmpty()) {
            System.out.println("- Thiết bị cần chuẩn bị: KHÔNG CÓ");
        } else {
            System.out.println("- Thiết bị cần chuẩn bị:");
            equipments.forEach(eq ->
                    System.out.println("   + " + eq.getEquipmentName()
                            + " (Số lượng mượn: " + eq.getBorrowedQuantity() + ")"));
        }

        // Dịch vụ kèm theo
        List<BookingServiceDetail> services = supportService.getTaskServices(bookingId);
        if (services.isEmpty()) {
            System.out.println("- Dịch vụ kèm theo:      KHÔNG CÓ");
        } else {
            System.out.println("- Dịch vụ kèm theo:");
            services.forEach(s ->
                    System.out.println("   + " + s.getServiceName()
                            + " (Số lượng: " + s.getOrderedQuantity() + " " + s.getUnit() + ")"));
        }
        System.out.println("----------------------------------------------");
    }

    private static void updateStatus(int bookingId) throws Exception {
        System.out.println("\nBạn muốn cập nhật trạng thái chuẩn bị?");
        System.out.println("1. Bắt đầu vào việc     (PREPARING)");
        System.out.println("2. Đã xong tất cả       (READY)");
        System.out.println("3. Báo sự cố / Thiếu TB (MISSING_EQUIPMENT)");
        System.out.println("0. Bỏ qua, quay lại");
        System.out.print("Lựa chọn: ");

        int action = InputValidation.inputInt();
        PreparationStatus newStatus = switch (action) {
            case 1 -> PreparationStatus.PREPARING;
            case 2 -> PreparationStatus.READY;
            case 3 -> PreparationStatus.MISSING_EQUIPMENT;
            default -> null;
        };

        if (newStatus == null) {
            System.out.println("=> Lựa chọn không hợp lệ hoặc bỏ qua. Không thay đổi trạng thái.");
            return;
        }

        if (supportService.updateTaskStatus(bookingId, newStatus)) {
            System.out.println("=> CẬP NHẬT TRẠNG THÁI THÀNH " + newStatus + " THÀNH CÔNG!");
        } else {
            System.out.println("=> Cập nhật thất bại, vui lòng thử lại.");
        }
    }
}
