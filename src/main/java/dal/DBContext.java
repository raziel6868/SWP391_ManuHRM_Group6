package dal;

import util.AppConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBContext {
    /**
     * Tạo connection tới Database MySQL bằng cách sử dụng các biến môi trường từ AppConfig.
     */
    public static Connection getConnection() {
        String driver = AppConfig.get("DB_DRIVER", "com.mysql.cj.jdbc.Driver");
        String url = AppConfig.get("DB_URL");
        String user = AppConfig.get("DB_USER");
        String pass = AppConfig.get("DB_PASS");

        if (url == null || user == null) {
            System.err.println("==========================================================================");
            System.err.println("FATAL ERROR: MISSING DATABASE CONFIGURATION!");
            System.err.println("Please copy '.env.example' to '.env' and fill in your DB credentials.");
            System.err.println("==========================================================================");
            return null;
        }

        try {
            Class.forName(driver);
            return DriverManager.getConnection(url, user, pass);
        } catch (ClassNotFoundException e) {
            System.err.println("ERROR: MySQL JDBC Driver not found in pom.xml");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("ERROR: Cannot connect to Database. Check your .env file and ensure MySQL Server is running.");
            e.printStackTrace();
        }
        return null;
    }
}
