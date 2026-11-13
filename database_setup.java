//A small one-click utility to create the demo_db database and users table automatically.
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class database_setup {
    private static final String SERVER_URL = "jdbc:mysql://localhost:3306/?allowPublicKeyRetrieval=true&useSSL=false";
    private static final String USER = "root";
    private static final String PASS = ""; // Set password if your local MySQL has one

    public static void main(String[] args) {
        System.out.println("Starting database initialization...");

        try {
            // 1. Explicitly load the MySQL JDBC driver class
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 2. Connect to MySQL server instance
            try (Connection conn = DriverManager.getConnection(SERVER_URL, USER, PASS);
                 Statement stmt = conn.createStatement()) {

                // 3. Create fresh database
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS demo_db");
                System.out.println(" Database 'demo_db' verified/created.");

                // 4. Switch to the database
                stmt.executeUpdate("USE demo_db");

                // 5. Create users table
                String createTableSQL = "CREATE TABLE IF NOT EXISTS users ("
                        + "id INT AUTO_INCREMENT PRIMARY KEY, "
                        + "username VARCHAR(50) NOT NULL UNIQUE, "
                        + "password VARCHAR(255) NOT NULL, "
                        + "role VARCHAR(20) NOT NULL, "
                        + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                        + ")";
                stmt.executeUpdate(createTableSQL);
                System.out.println(" Table 'users' verified/created.");

                System.out.println("\nSUCCESS: Database setup complete.");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Driver Error: MySQL Connector JAR not found in classpath.");
        } catch (Exception e) {
            System.err.println("SQL/Connection Error: " + e.getMessage());
        }
    }
}
