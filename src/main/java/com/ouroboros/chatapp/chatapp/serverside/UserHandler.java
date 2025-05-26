package com.ouroboros.chatapp.chatapp.serverside;

import com.ouroboros.chatapp.chatapp.datatype.User;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class UserHandler {
    public static void handleGetAllUsers(PrintWriter out) {
        List<User> users = DatabaseUtils.searchUsersByName(""); // Empty string returns all users
        requestCreation(out, users);
    }

    public static void handleSearchUsersByName(String query, PrintWriter out) {
        // Example: Use DatabaseUtils.searchUsersByName(query) to get users from DB
        List<User> users = DatabaseUtils.searchUsersByName(query); // Implement this in DatabaseUtils
        requestCreation(out, users);
    }

    private static void requestCreation(PrintWriter out, List<User> users) {
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
}

