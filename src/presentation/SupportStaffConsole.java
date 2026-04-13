package presentation;

import model.User;
import presentation.staff.TaskManagement;
import util.InputValidation;

/**
 * SupportStaffConsole — Menu router chính cho Support Staff.
 * Chỉ chịu trách nhiệm hiển thị menu và điều hướng.
 *
 * Áp dụng: Single Responsibility Principle (SRP)
 */
public class SupportStaffConsole {

    public static void displayMenu(User support) {
        boolean back = false;
        while (!back) {
            System.out.println("\n========================================");
            System.out.println("  NHÂN VIÊN HỖ TRỢ: " + support.getUsername());
            System.out.println("========================================");
            System.out.println("1. Xem và xử lý danh sách nhiệm vụ được phân công");
            System.out.println("0. Đăng xuất");
            System.out.print("Lựa chọn: ");

            int choice = InputValidation.inputInt();
            switch (choice) {
                case 1 -> TaskManagement.manage(support);
                case 0 -> back = true;
                default -> System.out.println("Lựa chọn không hợp lệ!");
            }
        }
    }
}
