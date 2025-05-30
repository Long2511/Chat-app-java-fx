package com.ouroboros.chatapp.chatapp.serverside;

import com.ouroboros.chatapp.chatapp.datatype.User;
import org.postgresql.Driver;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.*;

import com.ouroboros.chatapp.chatapp.datatype.Chat;

public class DatabaseUtils {
    private static final Map<String, String> ENV_VARS = new HashMap<>();
    private static final String ENV_FILE = "src/main/java/com/ouroboros/chatapp/chatapp/env";
    private static Connection connection = null;

    static {
        try {
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
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, username, email, avatar, status FROM users WHERE username ILIKE ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + query + "%");
            try (ResultSet rs = stmt.executeQuery()) {
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

    public static long saveChatAndReturnId(Chat chat, List<Integer> userIds) {
        String insertChatSql = "INSERT INTO chats (name, type, created_at, updated_at) VALUES (?, ?, ?, ?) RETURNING id";
        String insertParticipantSql = "INSERT INTO chat_participants (chat_id, user_id) VALUES (?, ?)";
        long chatId = -1;

        try (Connection conn = getConnection();
             PreparedStatement chatStmt = conn.prepareStatement(insertChatSql)) {

            String createdAt = chat.getCreatedAt() != null ? chat.getCreatedAt() : java.time.LocalDateTime.now().toString();
            String updatedAt = chat.getUpdatedAt() != null ? chat.getUpdatedAt() : createdAt;

            chatStmt.setString(1, chat.getName());
            chatStmt.setString(2, chat.getType());
            chatStmt.setTimestamp(3, Timestamp.valueOf(createdAt.replace("T", " ").substring(0, 19)));
            chatStmt.setTimestamp(4, Timestamp.valueOf(updatedAt.replace("T", " ").substring(0, 19)));

            try (ResultSet rs = chatStmt.executeQuery()) {
                if (rs.next()) {
                    chatId = rs.getLong(1);
                    chat.setId(chatId);

                    try (PreparedStatement participantStmt = conn.prepareStatement(insertParticipantSql)) {
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
        return chatId;
    }
    
    // Finds an existing private chat between two users
    public static Long findExistingPrivateChat(List<Integer> userIds) {
        if (userIds.size() != 2) return null;
        String sql = """
            SELECT chat_id
            FROM chat_participants
            WHERE user_id = ? OR user_id = ?
            GROUP BY chat_id
            HAVING COUNT(*) = 2 AND
               EVERY(user_id = ? OR user_id = ?) AND
               (SELECT type FROM chats WHERE id = chat_id) = 'PRIVATE'
            LIMIT 1
        """;
        try (Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userIds.get(0));
            stmt.setInt(2, userIds.get(1));
            stmt.setInt(3, userIds.get(0));
            stmt.setInt(4, userIds.get(1));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getLong("chat_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void loadEnvVars() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(ENV_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("//") || line.startsWith("#")) continue;
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
                "jdbc:postgresql://aws-0-ap-southeast-1.pooler.supabase.com:6543/postgres?user=postgres.irbxtfznezpoxueldryq&password=%s&prepareThreshold=0",
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

    public static List<Chat> loadChatsForUser(int userId) {
        List<Chat> userChats = new ArrayList<>();
        String sql = """
                SELECT c.id, c.name, c.type, c.created_at, c.updated_at,
                       u.id AS participant_id, u.username
                FROM chats c
                JOIN chat_participants cp ON cp.chat_id = c.id
                JOIN users u ON u.id = cp.user_id
                WHERE c.id IN (SELECT chat_id FROM chat_participants WHERE user_id = ?)
                ORDER BY c.id, u.id
                """;
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                Map<Long, Chat> chatMap = new HashMap<>();
                while (rs.next()) {
                    long chatId = rs.getLong("id");
                    Chat chat = chatMap.get(chatId);
                    if (chat == null) {
                        chat = new Chat();
                        chat.setId(chatId);
                        chat.setName(rs.getString("name"));
                        chat.setType(rs.getString("type"));
                        chat.setCreatedAt(rs.getTimestamp("created_at").toString());
                        chat.setUpdatedAt(rs.getTimestamp("updated_at").toString());
                        chat.setParticipants(new ArrayList<>());
                        chatMap.put(chatId, chat);
                    }
                    long pid = rs.getLong("participant_id");
                    if (pid != userId) {
                        User u = new User();
                        u.setId(pid);
                        u.setUsername(rs.getString("username"));
                        chat.getParticipants().add(u);
                    }
                }
                userChats.addAll(chatMap.values());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userChats;
    }

    public static List<Integer> getUserIdsInChat(int chatId) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT user_id FROM chat_participants WHERE chat_id = ?")) {
            stmt.setInt(1, chatId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<Integer> userIds = new ArrayList<>();
                while (rs.next()) {
                    userIds.add(rs.getInt("user_id"));
                }
                return userIds;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}

