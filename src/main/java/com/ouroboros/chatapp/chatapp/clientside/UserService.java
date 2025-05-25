package com.ouroboros.chatapp.chatapp.clientside;

import com.ouroboros.chatapp.chatapp.datatype.STATUS;
import com.ouroboros.chatapp.chatapp.datatype.User;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public class UserService {

    public static User loginAndGetUser(String email, String password) {
        try {
            BufferedWriter out = ClientConnection.getSharedWriter();
            BufferedReader in = ClientConnection.getSharedReader();

            out.write("start: LOGIN\r\n");
            out.write("email: " + email + "\r\n");
            out.write("password: " + password + "\r\n");
            out.write("end: LOGIN\r\n");
            out.flush();

            String response = in.readLine();
            if ("SUCCESS".equals(response)) {
                String username = in.readLine().split(": ")[1];
                int userId = Integer.parseInt(in.readLine().split(": ")[1]);
                return new User(userId, username, email);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static STATUS register(String username, String email, String password) {
        try {
            BufferedWriter out = ClientConnection.getSharedWriter();
            out.write("start: REGISTER\r\n");
            out.write("username: " + username + "\r\n");
            out.write("email: " + email + "\r\n");
            out.write("password: " + password + "\r\n");
            out.write("end: REGISTER\r\n");
            out.flush();

            String response = ClientConnection.getSharedReader().readLine();
            return "SUCCESS".equals(response) ? STATUS.SUCCESS : STATUS.FAILURE;
        } catch (IOException e) {
            e.printStackTrace();
            return STATUS.FAILURE;
        }
    }
}