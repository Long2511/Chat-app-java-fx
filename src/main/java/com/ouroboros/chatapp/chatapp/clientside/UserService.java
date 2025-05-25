package com.ouroboros.chatapp.chatapp.clientside;

import com.ouroboros.chatapp.chatapp.datatype.STATUS;

public class UserService {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8080;

    public static STATUS login(String email, String password) {
        try (ClientConnection connection = new ClientConnection(SERVER_HOST, SERVER_PORT)) {
            String response = connection.sendCommand("LOGIN " + email + " " + password);
            return response.equals("SUCCESS") ? STATUS.SUCCESS : STATUS.FAILURE;
        } catch (Exception e) {
            e.printStackTrace();
            return STATUS.FAILURE;
        }
    }

    public static STATUS register(String username, String email, String password) {
        try (ClientConnection connection = new ClientConnection(SERVER_HOST, SERVER_PORT)) {
            String response = connection.sendCommand("REGISTER " + username + " " + email + " " + password);
            return response.equals("SUCCESS") ? STATUS.SUCCESS : STATUS.FAILURE;
        } catch (Exception e) {
            e.printStackTrace();
            return STATUS.FAILURE;
        }
    }
}