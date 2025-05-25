package com.ouroboros.chatapp.chatapp.serverside;



import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.postgresql.Driver;

public class DatabaseUtils {
    private static final Map<String, String> ENV_VARS = new HashMap<>();
    private static final String ENV_FILE = "src/main/java/com/ouroboros/chatapp/chatapp/env";
    private static Connection connection = null;

    static {
        try {
            // Register PostgreSQL driver
            DriverManager.registerDriver(new Driver());
        } catch (SQLException e) {
            System.err.println("Failed to register PostgreSQL driver: " + e.getMessage());
        }
        try {
            loadEnvVars();
        } catch (IOException e) {
            System.err.println("Error loading environment variables: " + e.getMessage());
        }
    }

    private static void loadEnvVars() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(ENV_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // Skip comments or empty lines
                if (line.isEmpty() || line.startsWith("//") || line.startsWith("#")) {
                    continue;
                }
                int eqIndex = line.indexOf('=');
                if (eqIndex > 0) {
                    String key = line.substring(0, eqIndex).trim();
                    String value = line.substring(eqIndex + 1).trim();
                    ENV_VARS.put(key, value);
                }
            }
        }
    }

    public static String getEnvVar(String key) {
        return ENV_VARS.get(key);
    }

    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            String password = getEnvVar("SUPABASE_DB_PASSWORD");
            if (password == null) {
                throw new SQLException("Missing database password. Check your env file.");
            }
            String url = String.format(
                    "jdbc:postgresql://aws-0-ap-southeast-1.pooler.supabase.com:6543/postgres?user=postgres.irbxtfznezpoxueldryq&password=%s",
                    password
            );
            connection = DriverManager.getConnection(url);
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }
}