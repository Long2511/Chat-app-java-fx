package com.ouroboros.chatapp.chatapp.serverside;

import java.sql.*;
import java.io.*;
import org.mindrot.jbcrypt.BCrypt;

import static com.ouroboros.chatapp.chatapp.serverside.ApiUtils.handleJson;

public class AuthApiHandler {
    // Simple string-based registration: expects command = "REGISTER:{json}"
    public static String handleRegister(String json) {
        String username = ApiUtils.extractJsonField(json, "username");
        String email = ApiUtils.extractJsonField(json, "email");
        String password = ApiUtils.extractJsonField(json, "password");
        if (username == null || email == null || password == null) {
            return "{\"error\":\"Missing required fields\"}";
        }
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        try (Connection conn = DatabaseUtils.getConnection()) {
            try (PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE email = ?")) {
                checkStmt.setString(1, email);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    return "{\"error\":\"Email already exists\"}";
                }
            }
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO users (username, email, password) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, username);
                stmt.setString(2, email);
                stmt.setString(3, hashedPassword);
                stmt.executeUpdate();
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    long userId = generatedKeys.getLong(1);
                    String token = ApiUtils.createJwtToken(userId, username, email);
                    return String.format("{\"token\":\"%s\",\"username\":\"%s\",\"email\":\"%s\",\"userId\":%d}", token, username, email, userId);
                } else {
                    return "{\"error\":\"Failed to retrieve user ID\"}";
                }
            }
        } catch (Exception e) {
            return "{\"error\":\"" + handleJson(e.getMessage()) + "\"}";
        }
    }

    // Simple string-based login: expects command = "LOGIN:{json}"
    public static String handleLogin(String json) {
        String email = ApiUtils.extractJsonField(json, "email");
        String password = ApiUtils.extractJsonField(json, "password");
        if (email == null || password == null) {
            return "{\"error\":\"Missing required fields\"}";
        }
        try (Connection conn = DatabaseUtils.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement("SELECT id, username, password FROM users WHERE email = ?")) {
                stmt.setString(1, email);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    long userId = rs.getLong("id");
                    String username = rs.getString("username");
                    String hashedPassword = rs.getString("password");
                    if (BCrypt.checkpw(password, hashedPassword)) {
                        String token = ApiUtils.createJwtToken(userId, username, email);
                        return String.format("{\"token\":\"%s\",\"username\":\"%s\",\"email\":\"%s\",\"userId\":%d}", token, username, email, userId);
                    } else {
                        return "{\"error\":\"Invalid credentials\"}";
                    }
                } else {
                    return "{\"error\":\"User not found\"}";
                }
            }
        } catch (Exception e) {
            return "{\"error\":\"" + handleJson(e.getMessage()) + "\"}";
        }
    }

    // Simple string-based logout: expects command = "LOGOUT"
    public static String handleLogout() {
        return "{\"message\":\"Logged out successfully\"}";
    }
}

