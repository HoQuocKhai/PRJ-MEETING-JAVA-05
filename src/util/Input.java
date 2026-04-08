package util;

import java.util.Scanner;

public class Input {
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
}
