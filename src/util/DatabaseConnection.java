package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    public static Connection getConnection() {
        String driver = "com.mysql.cj.jdbc.Driver";
        String url = "jdbc:mysql://localhost:3306/PRJ_MEETING_JAVA_05";
        String user = "root";
        String pass = "Syfzj@7932";

        try {
            Class.forName(driver);
            return DriverManager.getConnection(url,user,pass);
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
