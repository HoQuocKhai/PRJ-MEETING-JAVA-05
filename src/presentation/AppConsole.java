package presentation;

import util.Input;

public class AppConsole {
    static void main() throws Exception {
        menuLogin();
        int choice = Input.inputInt();
        switch (choice) {
            case 1 -> System.out.println("Đăng nhập");
            case 2 -> RegisterEmployee.register();
            default -> System.out.println("Nhập sai lựa chọn");
        }
    }

    public static void menuLogin() {
        System.out.println("""
                ============  Quản lý Đặt phòng họp & Dịch vụ Văn phòng =========
                1. Đăng Nhập
                2. Đăng ký
                =================================================================
                """);
    }
}
