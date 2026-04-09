package presentation;

import exception.InvalidRegisterException;
import service.UserService;
import util.InputValidation;

import static util.InputValidation.inputString;


public class RegisterEmployee {
    private static final UserService userService = new UserService();
    public static void register() throws Exception {
        System.out.println("============= Đăng ký =============");
        String userName = inputUsername();
        String passWord = inputPassword();
        String email = InputValidation.inputEmail();

        System.out.print("Nhập phòng ban: ");
        String department = inputString();

        String phoneNumber = InputValidation.inputPhoneNumber();

        userService.registerEmployee(userName, passWord, department, email, phoneNumber);

        System.out.println("Đăng ký thành công.");
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

}
