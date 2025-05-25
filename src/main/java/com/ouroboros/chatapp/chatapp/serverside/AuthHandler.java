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
        // Create new account in the database
        // return STATUS.SUCCESS; // or STATUS.FAILURE based on the operation result

        return STATUS.SUCCESS; // Placeholder for successful registration
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
