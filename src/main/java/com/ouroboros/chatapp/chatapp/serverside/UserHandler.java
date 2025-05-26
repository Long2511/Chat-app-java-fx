package com.ouroboros.chatapp.chatapp.serverside;

import com.ouroboros.chatapp.chatapp.datatype.User;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class UserHandler {
    // TODO: Replace with actual database logic to fetch users
    public static void handleGetAllUsers(PrintWriter out) {
        List<User> users = DatabaseUtils.searchUsersByName(""); // Empty string returns all users
        out.println("start: RESPONSE_USERS");
        out.println("length: " + users.size());
        for (User user : users) {
            try {
                BufferedWriter bw = new BufferedWriter(out);
                user.sendObject(bw);
                bw.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        out.println("end: RESPONSE_USERS");
        out.flush();
    }

    public static void handleSearchUsersByName(String query, PrintWriter out) {
        List<User> users = DatabaseUtils.searchUsersByName(query); // Implement this in DatabaseUtils
        out.println("start: RESPONSE_USERS");
        out.println("length: " + users.size());
        for (User user : users) {
            try {
                BufferedWriter bw = new BufferedWriter(out);
                user.sendObject(bw);
                bw.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        out.println("end: RESPONSE_USERS");
        out.flush();
    }

    public static void deleteAccount(int userId) {
        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public static void handleForgotPassword(String email, String newPassword, PrintWriter out) {
        var status = com.ouroboros.chatapp.chatapp.serverside.AuthHandler.RequestForgotPassword(email, newPassword);
        out.println("start: FORGOT_PASSWORD_RESPONSE");
        out.println("status: " + (status == com.ouroboros.chatapp.chatapp.datatype.STATUS.SUCCESS ? "SUCCESS" : "FAILURE"));
        out.println("end: FORGOT_PASSWORD_RESPONSE");
        out.flush();
    }
}

