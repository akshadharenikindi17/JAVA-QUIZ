import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Oracle JDBC connection settings.
 * Update URL, USERNAME, and PASSWORD to match your Oracle installation.
 */
public final class DBConnection {
    private static final String URL =
            "jdbc:oracle:thin:@localhost:1521/XEPDB1";
    private static final String USERNAME = "your_username";
    private static final String PASSWORD = "your_password";

    private DBConnection() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
}
