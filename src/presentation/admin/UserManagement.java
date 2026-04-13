package presentation.admin;

import model.Enum.Role;
import model.User;
import service.IUserService;
import service.UserService;
import util.InputValidation;

import java.util.List;

/**
 * Quản lý người dùng — CRUD đầy đủ dành cho Admin.
 * - Xem danh sách (username, role, department, contact, phone)
 * - Thêm tài khoản Admin hoặc Support Staff
 * - Sửa thông tin (department, contact, phone, role)
 * - Xóa tài khoản (không được xóa chính mình)
 *
 * Tuân thủ Single Responsibility Principle — tách khỏi AdminConsole.
 */
public class UserManagement {

    private static final IUserService userService = new UserService();

    /**
     * Entry point, cần truyền vào admin đang đăng nhập
     * để ngăn việc admin tự xóa tài khoản của mình.
     */
    public static void manage(User currentAdmin) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- QUẢN LÝ NGƯỜI DÙNG ---");
            System.out.println("1. Xem danh sách người dùng");
            System.out.println("2. Thêm tài khoản (Admin / Support Staff)");
            System.out.println("3. Sửa thông tin người dùng");
            System.out.println("4. Xóa tài khoản");
            System.out.println("0. Quay lại menu chính");
            System.out.print("Lựa chọn: ");
            int choice = InputValidation.inputInt();

            switch (choice) {
                case 1 -> listUsers();
                case 2 -> createUser();
                case 3 -> updateUser();
                case 4 -> deleteUser(currentAdmin);
                case 0 -> back = true;
                default -> System.out.println("Lựa chọn không hợp lệ!");
            }
        }
    }

    // ===================================================
    // 1. HIỂN THỊ DANH SÁCH
    // ===================================================
    private static void listUsers() {
        try {
            List<User> users = userService.getAllUsers();
            if (users.isEmpty()) {
                System.out.println("Hiện tại chưa có người dùng nào trong hệ thống.");
                return;
            }
            System.out.println("\n--- DANH SÁCH NGƯỜI DÙNG ---");
            System.out.printf("%-5s | %-20s | %-15s | %-25s | %-25s | %-15s%n",
                    "ID", "Username", "Vai trò", "Phòng ban", "Email", "Điện thoại");
            System.out.println("-".repeat(115));
            for (User u : users) {
                System.out.printf("%-5d | %-20s | %-15s | %-25s | %-25s | %-15s%n",
                        u.getUserId(),
                        u.getUsername(),
                        u.getRole() != null ? u.getRole().name() : "N/A",
                        nvl(u.getDepartment()),
                        nvl(u.getContact()),
                        nvl(u.getPhoneNumber()));
            }
            System.out.println("Tổng: " + users.size() + " người dùng.");
        } catch (Exception e) {
            System.out.println("=> Lỗi khi tải danh sách: " + e.getMessage());
        }
    }

    // ===================================================
    // 2. THÊM NGƯỜI DÙNG
    // ===================================================
    private static void createUser() {
        System.out.println("\n--- THÊM TÀI KHOẢN MỚI ---");
        System.out.println("Chọn vai trò:");
        System.out.println("1. Quản trị viên (ADMIN)");
        System.out.println("2. Nhân viên hỗ trợ (SUPPORT_STAFF)");
        System.out.println("0. Hủy");
        System.out.print("Lựa chọn: ");
        int roleChoice = InputValidation.inputInt();

        Role selectedRole;
        if (roleChoice == 1) {
            selectedRole = Role.ADMIN;
        } else if (roleChoice == 2) {
            selectedRole = Role.SUPPORT_STAFF;
        } else if (roleChoice == 0) {
            System.out.println("=> Đã hủy thao tác.");
            return;
        } else {
            System.out.println("=> Lựa chọn không hợp lệ. Hủy thao tác.");
            return;
        }

        System.out.print("Nhập Username: ");
        String username = InputValidation.inputString();

        try {
            if (userService.checkUsername(username)) {
                System.out.println("=> Lỗi: Username '" + username + "' đã tồn tại! Vui lòng chọn tên khác.");
                return;
            }
        } catch (Exception e) {
            System.out.println("=> Lỗi kiểm tra hệ thống: " + e.getMessage());
            return;
        }

        System.out.print("Nhập Password: ");
        String password = InputValidation.inputString();
        System.out.print("Nhập Phòng ban: ");
        String department = InputValidation.inputString();
        System.out.print("Nhập Email liên hệ: ");
        String contact = InputValidation.inputEmail();
        System.out.print("Nhập Số điện thoại: ");
        String phone = InputValidation.inputPhoneNumber();

        try {
            userService.createStaffAdmin(username, password, selectedRole, department, contact, phone);
            System.out.println("=> Thêm tài khoản " + selectedRole.name() + " [" + username + "] thành công!");
        } catch (Exception e) {
            System.out.println("=> Thêm tài khoản thất bại: " + e.getMessage());
        }
    }

    // ===================================================
    // 3. SỬA THÔNG TIN NGƯỜI DÙNG
    // ===================================================
    private static void updateUser() {
        listUsers();
        System.out.print("\nNhập ID người dùng cần sửa: ");
        int userId = InputValidation.inputInt();

        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                System.out.println("=> Không tìm thấy người dùng với ID = " + userId);
                return;
            }

            System.out.println("Đang sửa thông tin của: [" + user.getUsername() + "] - " + user.getRole().name());
            System.out.println("(Nhấn Enter để giữ nguyên giá trị hiện tại)");

            System.out.print("Phòng ban hiện tại [" + nvl(user.getDepartment()) + "]: ");
            String department = InputValidation.inputStringAllowEmpty();
            if (!department.isBlank()) user.setDepartment(department);

            System.out.print("Email hiện tại [" + nvl(user.getContact()) + "]: ");
            String contact = InputValidation.inputStringAllowEmpty();
            if (!contact.isBlank()) user.setContact(contact);

            System.out.print("Điện thoại hiện tại [" + nvl(user.getPhoneNumber()) + "]: ");
            String phone = InputValidation.inputStringAllowEmpty();
            if (!phone.isBlank()) user.setPhoneNumber(phone);

            // Đổi vai trò
            System.out.println("Vai trò hiện tại: " + user.getRole().name());
            System.out.println("Đổi vai trò? (1=ADMIN, 2=SUPPORT_STAFF, 3=EMPLOYEE, 0=Giữ nguyên)");
            System.out.print("Lựa chọn: ");
            int roleChoice = InputValidation.inputInt();
            switch (roleChoice) {
                case 1 -> user.setRole(Role.ADMIN);
                case 2 -> user.setRole(Role.SUPPORT_STAFF);
                case 3 -> user.setRole(Role.EMPLOYEE);
                case 0 -> { /* giữ nguyên */ }
                default -> System.out.println("Vai trò không hợp lệ, giữ nguyên.");
            }

            if (userService.updateUserByAdmin(user)) {
                System.out.println("=> Cập nhật thông tin [" + user.getUsername() + "] thành công!");
            } else {
                System.out.println("=> Cập nhật thất bại.");
            }
        } catch (Exception e) {
            System.out.println("=> Lỗi khi cập nhật: " + e.getMessage());
        }
    }

    // ===================================================
    // 4. XÓA TÀI KHOẢN
    // ===================================================
    private static void deleteUser(User currentAdmin) {
        listUsers();
        System.out.print("\nNhập ID người dùng cần xóa: ");
        int userId = InputValidation.inputInt();

        // Bảo vệ: không cho xóa chính mình
        if (userId == currentAdmin.getUserId()) {
            System.out.println("=> Không thể xóa tài khoản của chính bạn!");
            return;
        }

        try {
            User target = userService.getUserById(userId);
            if (target == null) {
                System.out.println("=> Không tìm thấy người dùng với ID = " + userId);
                return;
            }

            System.out.println("Bạn sắp xóa tài khoản: [" + target.getUsername() + "] - " + target.getRole().name());
            System.out.print("Xác nhận xóa? (Y/N): ");
            String confirm = InputValidation.inputString();

            if (!confirm.equalsIgnoreCase("Y")) {
                System.out.println("=> Đã hủy thao tác xóa.");
                return;
            }

            if (userService.deleteUser(userId)) {
                System.out.println("=> Xóa tài khoản [" + target.getUsername() + "] thành công!");
            } else {
                System.out.println("=> Xóa tài khoản thất bại.");
            }
        } catch (Exception e) {
            System.out.println("=> Lỗi khi xóa tài khoản: " + e.getMessage());
        }
    }

    // ===================================================
    // Helper: tránh hiển thị null
    // ===================================================
    private static String nvl(String value) {
        return value != null ? value : "(chưa có)";
    }
}
