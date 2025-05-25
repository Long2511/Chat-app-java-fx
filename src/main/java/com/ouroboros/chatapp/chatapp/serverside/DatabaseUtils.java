package com.ouroboros.chatapp.chatapp.serverside;

import com.ouroboros.chatapp.chatapp.datatype.User;
import org.postgresql.Driver;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static List<User> searchUsersByName(String query) {
        List<User> users = new java.util.ArrayList<>();
        String sql = "SELECT id, username, email, avatar, status FROM users WHERE username ILIKE ?";
        try (Connection conn = getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + query + "%");
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    User user = new User();
                    user.setId(rs.getLong("id"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    user.setAvatar(rs.getString("avatar"));
                    user.setStatus(rs.getString("status"));
                    users.add(user);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public static void saveChat(com.ouroboros.chatapp.chatapp.datatype.Chat chat, List<Integer> userIds) {
        String insertChatSql = "INSERT INTO chats (name, type, created_at, updated_at) VALUES (?, ?, ?, ?) RETURNING id";
        String insertParticipantSql = "INSERT INTO chat_participants (chat_id, user_id) VALUES (?, ?)";
        try (Connection conn = getConnection();
             java.sql.PreparedStatement chatStmt = conn.prepareStatement(insertChatSql)) {
            chatStmt.setString(1, chat.getName());
            chatStmt.setString(2, chat.getType());
            String createdAt = chat.getCreatedAt() != null ? chat.getCreatedAt() : java.time.LocalDateTime.now().toString();
            String updatedAt = chat.getUpdatedAt() != null ? chat.getUpdatedAt() : createdAt;
            chatStmt.setTimestamp(3, java.sql.Timestamp.valueOf(createdAt.replace("T", " ").substring(0, 19)));
            chatStmt.setTimestamp(4, java.sql.Timestamp.valueOf(updatedAt.replace("T", " ").substring(0, 19)));
            // Get generated chat id
            try (java.sql.ResultSet rs = chatStmt.executeQuery()) {
                if (rs.next()) {
                    long chatId = rs.getLong(1);
                    chat.setId(chatId); // update the Chat object with the real DB id
                    // Create a new PreparedStatement for participants inside this block
                    try (java.sql.PreparedStatement participantStmt = conn.prepareStatement(insertParticipantSql)) {
                        for (Integer userId : userIds) {
                            participantStmt.setLong(1, chatId);
                            participantStmt.setInt(2, userId);
                            participantStmt.addBatch();
                        }
                        participantStmt.executeBatch();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
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