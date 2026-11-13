import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
public class Database {
    public static Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/berry";
        String username = "";
        String password = "";
        return DriverManager.getConnection(url, username, password);
    }
    public static void main(String[] args) {
        try (Connection connection = getConnection()) {
            System.out.println("Connected to Berry database! 🍓");
        } catch (SQLException e) {
            System.out.println("Database connection failed!");
            System.out.println(e.getMessage());
        }
    }
}
