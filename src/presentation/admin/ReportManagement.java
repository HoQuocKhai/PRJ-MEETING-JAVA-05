package presentation.admin;

import model.Room;
import service.IBookingService;
import service.BookingService;
import util.InputValidation;
import java.util.Map;

/**
 * Quản lý Báo cáo thống kê & Xuất hóa đơn.
 * Tách từ AdminConsole để tuân thủ Single Responsibility Principle.
 */
public class ReportManagement {
    private static final IBookingService bookingService = new BookingService();

    public static void manage() {
        System.out.println("\n--- TỔNG QUAN BÁO CÁO & THỐNG KÊ ---");
        try {
            // 1. Thống kê tần suất sử dụng phòng
            printRoomUsageStats();

            // 2. Doanh thu tháng hiện tại
            printMonthlyRevenue();

            // 3. Sub-menu xuất hóa đơn
            exportBillMenu();

        } catch (Exception e) {
            System.out.println("=> Lỗi hệ thống Báo cáo: " + e.getMessage());
        }
    }

    private static void printRoomUsageStats() throws Exception {
        Map<Room, Integer> stats = bookingService.getRoomUsageStatistics();
        System.out.println("\n--- THỐNG KÊ TẦN SUẤT SỬ DỤNG PHÒNG ---");
        System.out.printf("%-10s | %-20s | %-15s\n", "Room ID", "Tên Phòng", "Số Lần Mượn");
        System.out.println("--------------------------------------------------");
        if (stats.isEmpty()) {
            System.out.println("Chưa có dữ liệu thống kê.");
        } else {
            for (Map.Entry<Room, Integer> entry : stats.entrySet()) {
                Room r = entry.getKey();
                System.out.printf("%-10d | %-20s | %-15d\n", r.getRoomId(), r.getRoomName(), entry.getValue());
            }
        }
    }

    private static void printMonthlyRevenue() throws Exception {
        java.time.LocalDate now = java.time.LocalDate.now();
        double monthlyRevenue = bookingService.calculateCompletedRevenue(now.getMonthValue(), now.getYear());
        System.out.printf("\n- Tổng doanh thu Dịch vụ (Tháng %d/%d): %,.0f VNĐ\n",
                now.getMonthValue(), now.getYear(), monthlyRevenue);
    }

    private static void exportBillMenu() throws Exception {
        System.out.println("\nBạn có muốn XUẤT HÓA ĐƠN cho một cuộc họp đã hoàn tất (READY) không?");
        System.out.println("1. Xuất hóa đơn ra File (.txt)");
        System.out.println("0. Quay Lại");
        System.out.print("Lựa chọn: ");
        int option = InputValidation.inputInt();

        if (option == 1) {
            System.out.print("Nhập Booking ID cần xuất hóa đơn: ");
            int bId = InputValidation.inputInt();
            if (bookingService.exportBill(bId)) {
                System.out.println("=> Xuất hóa đơn thành công! Kiểm tra thư mục out/bills/");
            } else {
                System.out.println("=> Xuất hóa đơn thất bại.");
            }
        }
    }
}
