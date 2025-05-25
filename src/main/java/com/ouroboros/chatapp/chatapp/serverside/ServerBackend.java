package com.ouroboros.chatapp.chatapp.serverside;

import com.ouroboros.chatapp.chatapp.datatype.Chat;
import com.ouroboros.chatapp.chatapp.datatype.Message;
import com.ouroboros.chatapp.chatapp.datatype.User;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

public class ServerBackend {
    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        Logger logger = Logger.getLogger(ServerBackend.class.getName());
        System.out.println("Chat backend server started on port " + PORT);

        // Initialize database connection
        try {
            DatabaseUtils.getConnection();
            System.out.println("Successfully connected to database");
        } catch (Exception e) {
            System.err.println("Failed to connect to database: " + e.getMessage());
            logger.log(Level.SEVERE, "Database connection failed", e);
            System.exit(1);
        }

        // Fetch message data from database
//        MessagesApiHandler.fetchDataFromDatabase();

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected: " + clientSocket.getInetAddress());
            new Thread(() -> handleClient(clientSocket)).start();
        }
    }

    private static void handleClient(Socket clientSocket) {
        Logger logger = Logger.getLogger(ServerBackend.class.getName());
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))) {

            String line;

            // Look for the start marker and determine the object type
            while (true) {
                line = in.readLine();

                if (line.equals("start: GET_MESSAGES")) {
                    int chatId = -1;
                    while (!(line = in.readLine()).equals("end: GET_MESSAGES")) {
                        if (line.startsWith("chatId: ")) {
                            chatId = Integer.parseInt(line.substring("chatId: ".length()));
                        }
                    }
                    System.out.println("Chat ID: " + chatId);
                    if (chatId != -1) {
                        MessageHandler.handleRequestMessages(chatId, out);
                    }
                } else if (line.equals("start: SEND_MESSAGE")) {
                    int chatId = -1;
                    int senderId = -1;
                    String content = null;

                    while (!(line = in.readLine()).equals("end: SEND_MESSAGE")) {
                        if (line.startsWith("chatId: ")) {
                            chatId = Integer.parseInt(line.substring("chatId: ".length()));
                        } else if (line.startsWith("senderId: ")) {
                            senderId = Integer.parseInt(line.substring("senderId: ".length()));
                        } else if (line.startsWith("content: ")) {
                            content = line.substring("content: ".length());
                        }
                    }

                    System.out.println("Chat ID: " + chatId);
                    System.out.println("Sender ID: " + senderId);
                    System.out.println("Content: " + content);

                    if (chatId != -1 && senderId != -1 && content != null) {
                        MessageHandler.handleSendMessage(chatId, senderId, content, out);
                    }
                }

            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error handling client connection", e);
        } finally {
            try {
                clientSocket.close();
                System.out.println("Client disconnected");
            } catch (IOException ignored) {}
        }
    }
}
