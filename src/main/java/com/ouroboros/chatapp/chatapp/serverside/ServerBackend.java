package com.ouroboros.chatapp.chatapp.serverside;

import com.ouroboros.chatapp.chatapp.datatype.Message;
import com.ouroboros.chatapp.chatapp.serverside.MessageHandler;
import com.ouroboros.chatapp.chatapp.serverside.DatabaseUtils;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ServerBackend {
    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        Logger logger = Logger.getLogger(ServerBackend.class.getName());
        System.out.println("Chat backend server started on port " + PORT);

        try {
            DatabaseUtils.getConnection();
            System.out.println("Successfully connected to database");
        } catch (Exception e) {
            System.err.println("Failed to connect to database: " + e.getMessage());
            logger.log(Level.SEVERE, "Database connection failed", e);
            System.exit(1);
        }

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
            while ((line = in.readLine()) != null) {
                if (line.equals("start: SEND_MESSAGE")) {
                    System.out.println("Received SEND_MESSAGE request");
                    handleSendMessage(in, out);
                }
                // Add other handlers here...
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error handling client connection", e);
        } finally {
            try {
                clientSocket.close();
                System.out.println("Client disconnected");
            } catch (IOException ignored) {
            }
        }
    }

    private static void handleSendMessage(BufferedReader in, BufferedWriter out) {
        Logger logger = Logger.getLogger(ServerBackend.class.getName());
        try {
            int chatId = -1;
            int senderId = -1;
            String content = null;

            String line;
            while (!(line = in.readLine()).equals("end: SEND_MESSAGE")) {
                if (line.startsWith("chatId: ")) {
                    chatId = Integer.parseInt(line.substring("chatId: ".length()));
                } else if (line.startsWith("senderId: ")) {
                    senderId = Integer.parseInt(line.substring("senderId: ".length()));
                } else if (line.startsWith("content: ")) {
                    content = line.substring("content: ".length());
                }
            }

            if (chatId != -1 && senderId != -1 && content != null) {
                Message message = new Message();
                message.setChatId(chatId);
                message.setSenderId(senderId);
                message.setContent(content);
                message.setMessageType("text");
                java.time.LocalDateTime now = java.time.LocalDateTime.now();
                message.setCreatedAt(now);
                message.setUpdatedAt(now);
                System.out.println("handleSendMessage received: " + message);

                // Save message to the database
                MessageHandler.saveMessageToDatabase(message);
                System.out.println("handleSendMessage saved to database: " + message);


                // Handle sending the message
                MessageHandler.handleSendMessage(chatId, senderId, content, out);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error handling SEND_MESSAGE", e);
        }
    }
}