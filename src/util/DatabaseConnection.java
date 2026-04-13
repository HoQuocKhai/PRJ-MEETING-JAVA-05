package util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * I-E fix: Đọc thông tin kết nối từ db.properties (classpath) thay vì hardcode trong source.
 * File db.properties phải nằm trong classpath (thư mục src/ hoặc resources/).
 * KHÔNG commit db.properties lên Git — thêm vào .gitignore.
 */
public class DatabaseConnection {

    private static final Properties props = loadProperties();

    private static Properties loadProperties() {
        Properties p = new Properties();
        try (InputStream is = DatabaseConnection.class.getClassLoader()
                .getResourceAsStream("db.properties")) {
            if (is == null) {
                throw new RuntimeException(
                    "[DB Config] Không tìm thấy file db.properties trong classpath! " +
                    "Hãy đặt file db.properties vào thư mục src/ của dự án.");
            }
            p.load(is);
        } catch (IOException e) {
            throw new RuntimeException("[DB Config] Lỗi đọc db.properties: " + e.getMessage(), e);
        }
        return p;
    }

    public static Connection getConnection() {
        String driver = "com.mysql.cj.jdbc.Driver";
        String url  = props.getProperty("db.url");
        String user = props.getProperty("db.user");
        String pass = props.getProperty("db.password");

        try {
            Class.forName(driver);
            return DriverManager.getConnection(url, user, pass);
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("[DB] Không thể kết nối CSDL: " + e.getMessage(), e);
        }
    }
}
