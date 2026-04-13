package presentation;

import model.User;
import util.InputValidation;

public class AppConsole {
    private static User loggedInUser = null;

    public static void main(String[] args) {
        try {
            while (true) {
                if (loggedInUser == null) {
                    menuLogin();
                    int choice = InputValidation.inputInt();
                    switch (choice) {
                        case 1 -> loggedInUser = AuthConsole.login();
                        case 2 -> AuthConsole.register();
                        case 0 -> System.exit(0);
                        default -> System.out.println("Chọn lại!");
                    }
                } else {
                    switch (loggedInUser.getRole()) {
                        case ADMIN        -> AdminConsole.displayMenu(loggedInUser);
                        case EMPLOYEE     -> EmployeeConsole.displayMenu(loggedInUser);
                        case SUPPORT_STAFF -> SupportStaffConsole.displayMenu(loggedInUser);
                    }
                    loggedInUser = null;
                }
            }
        } catch (Exception e) {
            System.err.println("[FATAL] Lỗi hệ thống không xử lý được: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void menuLogin() {
        System.out.print("""
                ============  Quản lý Đặt phòng họp & Dịch vụ Văn phòng =========
                | 1. Đăng Nhập.
                | 2. Đăng ký.
                | 0. Thoát.
                =================================================================
                """);
        System.out.print("Nhập vào lựa chọn của bạn: ");
    }
}
