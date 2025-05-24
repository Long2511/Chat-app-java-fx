package com.ouroboros.chatapp.chatapp.serverside;

import java.sql.*;
import java.util.*;

public class UserApiHandler {
    // Simple string-based get users: expects command = "GET_USERS"
    public static String handleGetUsers() {
        List<Map<String, Object>> users = new ArrayList<>();
        try (Connection conn = DatabaseUtils.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, avatar, email, status, username FROM users")) {
            while (rs.next()) {
                Map<String, Object> user = new LinkedHashMap<>();
                user.put("id", rs.getLong("id"));
                user.put("avatar", rs.getString("avatar"));
                user.put("email", rs.getString("email"));
                user.put("status", rs.getString("status"));
                user.put("username", rs.getString("username"));
                users.add(user);
            }
        } catch (Exception e) {
            return "{\"error\":\"" + handleJson(e.getMessage()) + "\"}";
        }
        return ApiUtils.toJsonArray(users);
    }

    private static String handleJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
