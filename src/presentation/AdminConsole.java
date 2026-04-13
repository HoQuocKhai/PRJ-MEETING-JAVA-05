package presentation;

import model.Enum.Role;
import model.User;
import presentation.admin.BookingManagement;
import presentation.admin.EquipmentManagement;
import presentation.admin.ReportManagement;
import presentation.admin.RoomManagement;
import presentation.admin.ServiceManagement;
import service.IUserService;
import service.UserService;
import util.InputValidation;

/**
 * AdminConsole — Menu router chính cho Admin.
 * Chỉ chịu trách nhiệm điều hướng, KHÔNG chứa business logic CRUD.
 * Mỗi chức năng được tách thành class riêng trong package presentation.admin.
 *
 * Áp dụng: Single Responsibility Principle (SRP)
 */
public class AdminConsole {
    private static final IUserService userService = new UserService();

    public static void displayMenu(User admin) {
        boolean back = false;
        while (!back) {
            System.out.println("\n========================================");
            System.out.println("  QUẢN TRỊ VIÊN: " + admin.getUsername());
            System.out.println("========================================");
            System.out.println("1. Quản lý người dùng (Thêm Admin/Support Staff)");
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
                case 1 -> createStaff();
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

    /**
     * Tạo tài khoản Admin hoặc Support Staff mới.
     * Giữ lại ở đây vì liên quan đến UserService và logic tạo tài khoản nội bộ.
     */
    private static void createStaff() {
        System.out.println("\n--- THÊM NHÂN SỰ MỚI (ADMIN / SUPPORT STAFF) ---");
        System.out.println("Chọn vai trò muốn tạo:");
        System.out.println("1. Quản trị viên (ADMIN)");
        System.out.println("2. Nhân viên hỗ trợ (SUPPORT_STAFF)");
        System.out.println("0. Hủy");
        System.out.print("Lựa chọn: ");
        int choice = InputValidation.inputInt();

        Role selectedRole;
        if (choice == 1) {
            selectedRole = Role.ADMIN;
        } else if (choice == 2) {
            selectedRole = Role.SUPPORT_STAFF;
        } else if (choice == 0) {
            System.out.println("Thoát thao tác thêm mới!");
            return;
        } else {
            System.out.println("Lựa chọn không hợp lệ. Hủy thao tác thêm mới!");
            return;
        }

        System.out.print("Nhập Username: ");
        String username = InputValidation.inputString();

        try {
            if (userService.checkUsername(username)) {
                System.out.println("Lỗi: Username '" + username + "' đã tồn tại! Vui lòng chọn tên khác.");
                return;
            }
        } catch (Exception e) {
            System.out.println("Lỗi kiểm tra hệ thống: " + e.getMessage());
            return;
        }

        System.out.print("Nhập Password: ");
        String password = InputValidation.inputString();
        System.out.print("Nhập Phòng ban (Department): ");
        String department = InputValidation.inputString();
        String contact = InputValidation.inputEmail();
        String phone = InputValidation.inputPhoneNumber();

        try {
            userService.createStaffAdmin(username, password, selectedRole, department, contact, phone);
            System.out.println("=> Thêm " + selectedRole.name() + " thành công!");
        } catch (Exception e) {
            System.out.println("=> Thêm thất bại: " + e.getMessage());
        }
    }
}
