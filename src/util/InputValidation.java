package util;

import java.util.Scanner;

public class InputValidation {
    private static final Scanner scanner = new Scanner(System.in);
    public static String inputString() {
        while (true) {
            String result = scanner.nextLine().trim();

            if (result.isEmpty()) {
                System.out.print("Input không được để trống. Vui lòng nhập lại: ");
            } else {
                return result;
            }
        }
    }
    
    public static int inputInt() {
        while (true) {
            try {
                String input = scanner.nextLine().trim();

                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.print("Định dạng không hợp lệ! Vui lòng nhập một số nguyên: ");
            }
        }
    }

    public static String inputEmail() {
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
        while (true) {
            System.out.print("Nhập email liên hệ: ");
            String email = inputString();
            if (email.matches(emailRegex)) {
                return email;
            }
            System.out.println("Email không hợp lệ! Vui lòng nhập đúng định dạng (VD: tenban@gmail.com)");
        }
    }

    public static String inputPhoneNumber() {
        String regexPhoneNumber = "^(0|\\+84)[35789][0-9]{8}$";
        while (true) {
            System.out.print("Nhập số điện thoại: ");
            String phoneNumber = inputString();
            if (phoneNumber.matches(regexPhoneNumber)) {
                return phoneNumber;
            }
            System.out.println("Phone number không hợp lệ! Vui lòng nhập đúng (VD: 0345832941)");
        }
    }

    public static String inputStringAllowEmpty() {
        return scanner.nextLine().trim();
    }

    public static String inputPhoneNumberAllowEmpty() {
        String regexPhoneNumber = "^(0|\\+84)[35789][0-9]{8}$";
        while (true) {
            String phoneNumber = scanner.nextLine().trim();
            if (phoneNumber.isEmpty()) {
                return "";
            }
            if (phoneNumber.matches(regexPhoneNumber)) {
                return phoneNumber;
            }
            System.out.print("Phone number không hợp lệ! Vui lòng nhập đúng hoặc nhấn Enter để bỏ qua (VD: 0345832941): ");
        }
    }
}
