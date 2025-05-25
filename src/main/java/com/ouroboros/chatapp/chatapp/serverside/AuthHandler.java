package com.ouroboros.chatapp.chatapp.serverside;

import com.ouroboros.chatapp.chatapp.datatype.STATUS;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.ouroboros.chatapp.chatapp.datatype.User;
import org.mindrot.jbcrypt.BCrypt;

public class AuthHandler {

    public static User RequestLogin(String email, String password) throws SQLException {
        // Compare with data from the database
        // return STATUS.SUCCESS; // or STATUS.FAILURE based on the comparison
        return AuthHandler.authenticateUser(email, password);
    }

    public static STATUS RequestRegister(String username, String email, String password) {
        try (Connection conn = DatabaseUtils.getConnection()) {
            // First check if email already exists
            try (PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE email = ?")) {
                checkStmt.setString(1, email);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()&& rs.getInt(1) > 0) {
                    System.out.println("Registration failed: Email " + email + " already exists");
                    return STATUS.FAILURE; // Email already exists
                }
            }

            // Hash the password
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

            // Insert new user
            try (PreparedStatement insertStmt = conn.prepareStatement(
                    "INSERT INTO users (username, email, password) VALUES (?, ?, ?)")) {
                insertStmt.setString(1, username);
                insertStmt.setString(2, email);
                insertStmt.setString(3, hashedPassword);
                insertStmt.executeUpdate();
                System.out.println("Registration successful for user: " + username);
                return STATUS.SUCCESS;
            }
        } catch (SQLException e) {
            System.err.println("Error during registration: " + e.getMessage());
            return STATUS.FAILURE;
        }
    }

    public static User authenticateUser(String email, String password) throws SQLException {
        try (Connection conn = DatabaseUtils.getConnection()) {
            // Check if email exists and retrieve user details
            try (PreparedStatement stmt = conn.prepareStatement("SELECT id, username, password, email FROM users WHERE email = ?")) {
                stmt.setString(1, email);
                ResultSet rs = stmt.executeQuery();
                User user = new User();
                if (rs.next()) {
                    String hashedPassword = rs.getString("password");
                    String username = rs.getString("username");
                    int userId = rs.getInt("id");
                    user.setUsername(username);
                    user.setEmail(email);
                    user.setId(userId);

                    // Verify password
                    if (BCrypt.checkpw(password, hashedPassword)) {
                        return user; // Password matches, authentication successful
                    } else {
                        return null; // Password does not match
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        // If no user found or password does not match
        System.out.println("Authentication failed for user: " + email);
        return null;
    }

}
