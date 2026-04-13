package presentation;

import exception.InvalidRegisterException;
import model.User;
import service.IUserService;
import service.UserService;
import util.InputValidation;

import static util.InputValidation.inputString;

public class AuthConsole {
    private static final IUserService userService = new UserService();

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

    public static void register() {
        System.out.println("============= Đăng ký =============");
        try {
            String userName = inputUsername();
            String passWord = inputConfirmPassword();
            String email = InputValidation.inputEmail();

            System.out.print("Nhập phòng ban: ");
            String department = inputString();

            String phoneNumber = InputValidation.inputPhoneNumber();

            userService.registerEmployee(userName, passWord, department, email, phoneNumber);

            System.out.println("Đăng ký thành công.");
        } catch (Exception e) {
            System.out.println("=> Lỗi hệ thống khi đăng ký: " + e.getMessage());
        }
    }

    private static String inputUsername() throws Exception {
        while (true) {
            System.out.print("Nhập tên đăng nhập: ");
            String userName = inputString();

            if (userService.checkUsername(userName)) {
                System.out.println("Username đã tồn tại! Vui lòng chọn tên khác.\n");
            } else {
                return userName;
            }
        }
    }

    private static String inputConfirmPassword() throws InvalidRegisterException {
        while (true) {
            System.out.print("Nhập mật khẩu: ");
            String passWord = inputString();

            System.out.print("Xác nhận mật khẩu: ");
            String confirmPassword = inputString();

            if (passWord.equals(confirmPassword)) {
                return passWord;
            } else {
                System.out.println("Mật khẩu xác nhận không khớp! Vui lòng nhập lại.\n");
            }
        }
    }
}
