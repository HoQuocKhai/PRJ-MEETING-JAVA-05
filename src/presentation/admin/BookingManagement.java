package presentation.admin;

import model.Booking;
import model.User;
import service.IBookingService;
import service.IUserService;
import service.BookingService;
import service.UserService;
import util.InputValidation;
import java.util.List;

/**
 * Quản lý duyệt / từ chối Booking.
 * Tách từ AdminConsole để tuân thủ Single Responsibility Principle.
 */
public class BookingManagement {
    private static final IBookingService bookingService = new BookingService();
    private static final IUserService userService = new UserService();

    public static void manage() {
        System.out.println("\n--- QUẢN LÝ ĐẶT PHÒNG (DUYỆT/TỪ CHỐI) ---");
        try {
            List<Booking> pendingList = bookingService.getPendingBookings();
            if (pendingList.isEmpty()) {
                System.out.println("=> Không có yêu cầu đặt phòng nào đang chờ duyệt (PENDING).");
                return;
            }

            System.out.println("Danh sách các Booking đang chờ duyệt (PENDING):");
            System.out.printf("%-5s | %-10s | %-10s | %-20s | %-20s%n",
                    "ID", "User ID", "Room ID", "Bắt đầu", "Kết thúc");
            System.out.println("-".repeat(73));
            for (Booking b : pendingList) {
                System.out.printf("%-5d | %-10d | %-10d | %-20s | %-20s%n",
                        b.getBookingId(), b.getUserId(), b.getRoomId(),
                        b.getStartTime(), b.getEndTime());
            }
            System.out.println("Tổng: " + pendingList.size() + " booking đang chờ duyệt.");

            System.out.print("Nhập ID Booking muốn xử lý (hoặc 0 để thoát): ");
            int bookingId = InputValidation.inputInt();
            if (bookingId == 0)
                return;

            System.out.println("Bạn muốn (1) Duyệt hay (2) Từ chối?");
            System.out.print("Lựa chọn: ");
            int action = InputValidation.inputInt();

            if (action == 1) {
                handleApprove(bookingId);
            } else if (action == 2) {
                handleReject(bookingId);
            } else {
                System.out.println("=> Lựa chọn không hợp lệ. Hủy thao tác.");
            }
        } catch (Exception e) {
            System.out.println("=> Lỗi khi xử lý duyệt đặt phòng: " + e.getMessage());
        }
    }

    private static void handleApprove(int bookingId) throws Exception {
        List<User> staffList = userService.getSupportStaffs();
        if (staffList.isEmpty()) {
            System.out.println("=> Không có nhân viên Support Staff nào trong hệ thống! Không thể duyệt gán việc.");
            return;
        }

        System.out.println("\nDanh sách nhân viên Support Staff:");
        for (User staff : staffList) {
            System.out.println("ID: " + staff.getUserId() + " - Name: " + staff.getUsername() + " ("
                    + staff.getDepartment() + ")");
        }

        System.out.print("Nhập ID Nhân viên hỗ trợ để phân công: ");
        int staffId = InputValidation.inputInt();

        boolean validStaff = staffList.stream().anyMatch(s -> s.getUserId() == staffId);
        if (!validStaff) {
            System.out.println("=> Lỗi: ID nhân viên không hợp lệ. Hủy thao tác.");
            return;
        }

        if (bookingService.approveBooking(bookingId, staffId)) {
            System.out.println("=> DUYỆT ĐẶT PHÒNG THÀNH CÔNG! Đã phân công cho Support Staff ID " + staffId);
        }
    }

    private static void handleReject(int bookingId) throws Exception {
        if (bookingService.rejectBooking(bookingId)) {
            System.out.println("=> Đã TỪ CHỐI yêu cầu đặt phòng thành công.");
        } else {
            System.out.println("=> Từ chối thất bại.");
        }
    }
}
