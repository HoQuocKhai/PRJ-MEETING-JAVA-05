package presentation;

import model.User;
import util.InputValidation;

public class EmployeeConsole {
    public static void displayMenu(User employee) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- NHÂN VIÊN: " + employee.getUsername() + " ---");
            System.out.println("1. Đặt phòng họp");
            System.out.println("2. Yêu cầu dịch vụ văn phòng");
            System.out.println("3. Lịch sử đặt phòng");
            System.out.println("4. Xem/Cập nhật hồ sơ cá nhân");
            System.out.println("0. Đăng xuất");
            System.out.print("Lựa chọn: ");

            int choice = InputValidation.inputInt();
            switch (choice) {
                case 1 -> System.out.println("Chức năng đặt phòng...");
                case 4 -> ProfileConsole.manageProfile(employee);
                case 0 -> back = true;
                default -> System.out.println("Lựa chọn không hợp lệ!");
            }
        }
    }
}
