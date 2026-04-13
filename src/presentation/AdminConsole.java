package presentation;

import model.User;
import presentation.admin.BookingManagement;
import presentation.admin.EquipmentManagement;
import presentation.admin.ReportManagement;
import presentation.admin.RoomManagement;
import presentation.admin.ServiceManagement;
import presentation.admin.UserManagement;
import util.InputValidation;

/**
 * AdminConsole — Menu router chính cho Admin.
 * Chỉ chịu trách nhiệm điều hướng, KHÔNG chứa business logic CRUD.
 * Mỗi chức năng được tách thành class riêng trong package presentation.admin.
 *
 * Áp dụng: Single Responsibility Principle (SRP)
 */
public class AdminConsole {

    public static void displayMenu(User admin) {
        boolean back = false;
        while (!back) {
            System.out.println("\n========================================");
            System.out.println("  QUẢN TRỊ VIÊN: " + admin.getUsername());
            System.out.println("========================================");
            System.out.println("1. Quản lý người dùng (Xem / Thêm / Sửa / Xóa)");
            System.out.println("2. Quản lý phòng họp");
            System.out.println("3. Quản lý thiết bị di động");
            System.out.println("4. Quản lý Đặt phòng (Duyệt/Từ chối)");
            System.out.println("5. Quản lý Dịch vụ đi kèm");
            System.out.println("6. Báo cáo Thống kê & Xuất Hóa đơn");
            System.out.println("7. Xem/Cập nhật hồ sơ cá nhân");
            System.out.println("0. Đăng xuất");
            System.out.print("Lựa chọn: ");

            int choice = InputValidation.inputInt();
            switch (choice) {
                case 1 -> UserManagement.manage(admin);
                case 2 -> RoomManagement.manage();
                case 3 -> EquipmentManagement.manage();
                case 4 -> BookingManagement.manage();
                case 5 -> ServiceManagement.manage();
                case 6 -> ReportManagement.manage();
                case 7 -> ProfileConsole.manageProfile(admin);
                case 0 -> back = true;
                default -> System.out.println("Lựa chọn không hợp lệ!");
            }
        }
    }

}
