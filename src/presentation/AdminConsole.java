package presentation;

import model.User;
import util.InputValidation;

public class AdminConsole {
    public static void displayMenu(User admin) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- QUẢN TRỊ VIÊN: " + admin.getUsername() + " ---");
            System.out.println("1. Quản lý người dùng (Thêm Admin/Support Staff)");
            System.out.println("2. Quản lý phòng họp");
            System.out.println("3. Xem báo cáo hệ thống");
            System.out.println("4. Xem/Cập nhật hồ sơ cá nhân");
            System.out.println("0. Đăng xuất");
            System.out.print("Lựa chọn: ");

            int choice = InputValidation.inputInt();
            switch (choice) {
                case 1 -> System.out.println("Chức năng quản lý người dùng...");
                case 4 -> ProfileConsole.manageProfile(admin);
                case 0 -> back = true;
                default -> System.out.println("Lựa chọn không hợp lệ!");
            }
        }
    }
}
