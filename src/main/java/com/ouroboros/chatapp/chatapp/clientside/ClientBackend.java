package com.ouroboros.chatapp.chatapp.clientside;

import java.io.*;
import java.net.Socket;

public class ClientBackend {
    private final String host;
    private final int port;

    public ClientBackend(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String sendCommand(String command) throws IOException {
        try (Socket socket = new Socket(host, port);
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out.write(command + "\n");
            out.flush();
            return in.readLine();
        }
    }

    public void close() throws IOException {
        // No longer needed as each command uses its own connection
    }

    // Example usage
    public static void main(String[] args) {
        try {
            ClientBackend client = new ClientBackend("localhost", 8080);
            // Test get users
            String users = client.sendCommand("GET_USERS");
            System.out.println("Users: " + users);
            // Test register
            String regJson = "{\"username\":\"testuser\",\"email\":\"test@example.com\",\"password\":\"testpass\"}";
            String regResp = client.sendCommand("REGISTER:" + regJson);
            System.out.println("Register: " + regResp);
            // Test login
            String loginJson = "{\"email\":\"test@example.com\",\"password\":\"testpass\"}";
            String loginResp = client.sendCommand("LOGIN:" + loginJson);
            System.out.println("Login: " + loginResp);
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

