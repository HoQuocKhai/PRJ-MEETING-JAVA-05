package presentation;

import model.User;
import service.IUserService;
import service.UserService;
import util.InputValidation;

public class ProfileConsole {
    private static final IUserService userService = new UserService();

    public static void manageProfile(User loggedInUser) {
        if (loggedInUser == null) return;
        
        while (true) {
            System.out.println("\n============ HỒ SƠ CÁ NHÂN ============");
            System.out.println("1. Xem thông tin hồ sơ");
            System.out.println("2. Cập nhật thông tin");
            System.out.println("0. Quay lại Menu chính");
            System.out.println("=======================================");
            System.out.print("Lựa chọn: ");
            
            int choice = InputValidation.inputInt();
            switch (choice) {
                case 1 -> showProfile(loggedInUser);
                case 2 -> updateProfile(loggedInUser);
                case 0 -> { return; }
                default -> System.out.println("Lựa chọn không hợp lệ!");
            }
        }
    }

    private static void showProfile(User user) {
        System.out.println("\n--- THÔNG TIN CỦA BẠN ---");
        System.out.println("ID: " + user.getUserId());
        System.out.println("Tài khoản: " + user.getUsername());
        System.out.println("Vai trò: " + user.getRole().name());
        System.out.println("Phòng ban: " + (user.getDepartment() != null ? user.getDepartment() : "Chưa cập nhật"));
        System.out.println("Liên hệ/Email: " + (user.getContact() != null ? user.getContact() : "Chưa cập nhật"));
        System.out.println("Số điện thoại: " + (user.getPhoneNumber() != null ? user.getPhoneNumber() : "Chưa cập nhật"));
        System.out.println("-------------------------");
    }

    private static void updateProfile(User user) {
        System.out.println("\n--- CẬP NHẬT HỒ SƠ ---");
        System.out.println("(Nhấn Enter tại mục không muốn thay đổi)");
        
        System.out.print("Phòng ban hiện tại (" + (user.getDepartment() != null ? user.getDepartment() : "Trống") + "): ");
        String dept = InputValidation.inputStringAllowEmpty();
        if (!dept.isEmpty()) {
            user.setDepartment(dept);
        }

        System.out.print("Email/Liên hệ hiện tại (" + (user.getContact() != null ? user.getContact() : "Trống") + "): ");
        String contact = InputValidation.inputStringAllowEmpty();
        if (!contact.isEmpty()) {
            user.setContact(contact);
        }

        System.out.print("Số điện thoại hiện tại (" + (user.getPhoneNumber() != null ? user.getPhoneNumber() : "Trống") + "): ");
        String phone = InputValidation.inputPhoneNumberAllowEmpty();
        if (!phone.isEmpty()) {
            user.setPhoneNumber(phone);
        }

        try {
            boolean success = userService.updateProfile(user);
            if (success) {
                System.out.println("\n[!] Cập nhật hồ sơ thành công!");
            } else {
                System.out.println("\n[!] Cập nhật thất bại, không có thay đổi nào được lưu.");
            }
        } catch (Exception e) {
            System.out.println("\n[X] Có lỗi xảy ra khi cập nhật hồ sơ: " + e.getMessage());
        }
    }
}
