package presentation;

import model.User;
import service.UserService;
import util.InputValidation;

public class LoginConsole {
    private static final UserService userService = new UserService();

    public static User login() {
        System.out.println("============= Đăng nhập =============");
        System.out.print("Tên đăng nhập: ");
        String username = InputValidation.inputString();
        
        System.out.print("Mật khẩu: ");
        String password = InputValidation.inputString();

        try {
            User user = userService.login(username, password);
            if (user != null) {
                System.out.println("\nĐăng nhập thành công! Chào mừng " + user.getUsername() + " (" + user.getRole().name() + ")");
                return user;
            } else {
                System.out.println("\nTên đăng nhập hoặc mật khẩu không chính xác!");
            }
        } catch (Exception e) {
            System.out.println("\nCó lỗi xảy ra: " + e.getMessage());
        }
        return null;
    }
}
