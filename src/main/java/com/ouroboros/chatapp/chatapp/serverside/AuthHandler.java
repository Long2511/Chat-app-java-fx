package com.ouroboros.chatapp.chatapp.serverside;

import com.ouroboros.chatapp.chatapp.datatype.STATUS;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;;
import org.mindrot.jbcrypt.BCrypt;

public class AuthHandler {

    public static STATUS RequestLogin(String email, String password) {
        // Compare with data from the database
        // return STATUS.SUCCESS; // or STATUS.FAILURE based on the comparison

        return STATUS.SUCCESS; // Placeholder for successful login
    }

    public static STATUS RequestRegister(String username, String email, String password) {
        // Create new account in the database
        // return STATUS.SUCCESS; // or STATUS.FAILURE based on the operation result

        return STATUS.SUCCESS; // Placeholder for successful registration
    }
    public boolean authenticateUser(String email, String password) throws SQLException {
        try (Connection conn = DatabaseUtils.getConnection()) {
            // Check if email exists and retrieve user details
            try (PreparedStatement stmt = conn.prepareStatement("SELECT id, username, password, email FROM users WHERE email = ?")) {
                stmt.setString(1, email);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    String hashedPassword = rs.getString("password");
                    String username = rs.getString("username");
                    int userId = rs.getInt("id");

                    // Verify password
                    if (BCrypt.checkpw(password, hashedPassword)) {
                        String storedPassword = Utils.createJwtToken(userId, username, email);
                        return storedPassword.equals(password);
                    } else {
                        return false; // Password does not match
                    }


                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        }
        // If no user found or password does not match
        System.out.println("Authentication failed for user: " + email);
        return false;
    }
}
