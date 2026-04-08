package presentation;

import exception.InvalidRegisterException;
import service.UserService;
import static util.Input.inputString;


public class RegisterEmployee {
    private static final UserService userService = new UserService();
    public static void register() throws Exception {
        System.out.println("============= Đăng ký =============");
        String userName = inputUsername();
        String passWord = inputPassword();
        String email = inputEmail();
        System.out.print("Nhập số điện thoại: ");
        String phoneNumber = inputString();

        userService.registerEmployee(userName, passWord, email, phoneNumber);
    }

    public static String inputUsername() throws Exception {
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

    public static String inputPassword() throws InvalidRegisterException {
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

    public static String inputEmail() {
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";

        while (true) {
            String email = inputString();

            if (email.matches(emailRegex)) {
                return email;
            } else {
                System.out.print("Email không hợp lệ! Vui lòng nhập đúng định dạng (VD: tenban@gmail.com): ");
            }
        }
    }
}
