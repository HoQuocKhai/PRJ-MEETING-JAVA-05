package presentation;

import model.User;
import util.InputValidation;

public class SupportStaffConsole {
    public static void displayMenu(User support) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- NHÂN VIÊN HỖ TRỢ: " + support.getUsername() + " ---");
            System.out.println("1. Xem danh sách cuộc họp được phân công.");
            System.out.println("2. Cập nhật trạng thái chuẩn bị phòng, thiết bị.\n");
            System.out.print("Lựa chọn: ");

            int choice = InputValidation.inputInt();
            switch (choice) {
                case 1 -> System.out.println("1. Xem danh sách cuộc họp được phân công.");
                case 2 -> System.out.println("2. Cập nhật trạng thái chuẩn bị phòng, thiết bị.\n");
                case 0 -> back = true;
                default -> System.out.println("Lựa chọn không hợp lệ!");
            }
        }
    }
}
