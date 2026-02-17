package db;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String DEFAULT_DB_NAME = "cab_booking";
    private static final Dotenv DOTENV = Dotenv.configure()
            .ignoreIfMissing()
            .ignoreIfMalformed()
            .load();

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found", e);
        }
    }

    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            value = DOTENV.get(key);
        }
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private static String buildJdbcUrl() {
        String url = getEnvOrDefault("DB_URL", "");
        if (!url.isBlank()) {
            return url;
        }

        String host = getEnvOrDefault("DB_HOST", "localhost");
        String port = getEnvOrDefault("DB_PORT", "3306");
        String dbName = getEnvOrDefault("DB_NAME", DEFAULT_DB_NAME);

        return "jdbc:mysql://" + host + ":" + port + "/" + dbName
                + "?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true";
    }

    public static Connection getConnection() throws SQLException {
        String username = getEnvOrDefault("DB_USER", "root");
        String password = getEnvOrDefault("DB_PASS", "root");

        return DriverManager.getConnection(buildJdbcUrl(), username, password);
    }

    public static void ensureSchema() throws SQLException {
        String usersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    name VARCHAR(100) NOT NULL,
                    email VARCHAR(150) NOT NULL UNIQUE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;

        String ridesTable = """
                CREATE TABLE IF NOT EXISTS rides (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    owner_id INT NOT NULL,
                    source VARCHAR(80) NOT NULL,
                    destination VARCHAR(80) NOT NULL,
                    seats INT NOT NULL,
                    fare_per_seat INT NOT NULL,
                    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (owner_id) REFERENCES users(id)
                )
                """;

        String bookingsTable = """
                CREATE TABLE IF NOT EXISTS bookings (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    ride_id INT NOT NULL,
                    user_id INT NOT NULL,
                    seats_booked INT NOT NULL,
                    total_fare INT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (ride_id) REFERENCES rides(id),
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
                """;

        try {
            try (Connection connection = getConnection()) {
                connection.createStatement().execute(usersTable);
                connection.createStatement().execute(ridesTable);
                connection.createStatement().execute(bookingsTable);
            }
        } catch (SQLException e) {
            throw new SQLException("Failed to initialize schema", e);
        }
    }
}
