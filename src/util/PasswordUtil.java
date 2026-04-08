package util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {
    // 1. Hàm dùng để mã hóa mật khẩu (dùng khi Đăng ký)
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }

    // 2. Hàm dùng để kiểm tra mật khẩu (dùng khi Đăng nhập)
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }
}
