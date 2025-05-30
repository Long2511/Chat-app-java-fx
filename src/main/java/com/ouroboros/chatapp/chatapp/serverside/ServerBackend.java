package com.ouroboros.chatapp.chatapp.serverside;


import com.ouroboros.chatapp.chatapp.datatype.STATUS;
import com.ouroboros.chatapp.chatapp.datatype.User;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ServerBackend {
    private static final int PORT = 8080;

    public static Map<Long, List<BufferedWriter>> clientWriters = Collections.synchronizedMap(new HashMap<>());

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

        // Fetch initial data from the database
        MessageHandler.fetchDataFromDatabase();

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

            long clientUserId = -1; // Initialize client user ID
            // Look for the start marker and determine the object type
            String line;
            while (true) {
                line = in.readLine();
                if (line.equals("start: SEARCH_USERS_BY_NAME")) {
                    System.out.println("[SERVER DEBUG] Received SEARCH_USERS_BY_NAME request");
                    String query = "";
                    while (!(line = in.readLine()).equals("end: SEARCH_USERS_BY_NAME")) {
                        if (line.startsWith("query: ")) {
                            query = line.substring("query: ".length());
                        }
                    }
                    System.out.println("[SERVER DEBUG] SEARCH_USERS_BY_NAME query: " + query);
                    PrintWriter pw = new PrintWriter(out, true);
                    UserHandler.handleSearchUsersByName(query, pw);
                } else if (line.equals("start: GET_ALL_USERS")) {
                    System.out.println("[SERVER DEBUG] Received GET_ALL_USERS request");
                    while (!(line = in.readLine()).equals("end: GET_ALL_USERS")) {
                        // No parameters needed
                    }
                    PrintWriter pw = new PrintWriter(out, true);
                    UserHandler.handleGetAllUsers(pw);
                } else if (line.equals("start: CREATE_CHAT")) {
                    ChatHandler.handleCreateChatRequest(in, out);
                } else if (line.equals("start: GET_MESSAGES")) {
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
                    handleSendMessage(in, out);
                } else if (line.equals("start: LOGIN")) {
                    String email = null;
                    String password = null;

                    while (!(line = in.readLine()).equals("end: LOGIN")) {
                        if (line.startsWith("email: ")) {
                            email = line.substring("email: ".length());
                        } else if (line.startsWith("password: ")) {
                            password = line.substring("password: ".length());
                        }
                    }

                    System.out.println("Login attempt for: " + email);

                    User user = null;
                    if (email != null && password != null) {
                        user = AuthHandler.RequestLogin(email, password);
                    }
                    if (user != null) {
                        clientUserId = user.getId(); // Set the client user ID
                        // Add the writer to the map with user ID as key
                        clientWriters.computeIfAbsent(clientUserId, k -> new ArrayList<>()).add(out);

                        System.out.println("Login successful for: " + email);
                        out.write("start: AUTH_RESPONSE\r\n");
                        out.write("status: SUCCESS\r\n");
                        out.write("username: " + user.getUsername() + "\r\n");
                        out.write("email: " + user.getEmail() + "\r\n");
                        out.write("userId: " + user.getId() + "\r\n");
                        out.write("end: AUTH_RESPONSE\r\n");
                        out.flush();
                    } else {
                        System.out.println("Login failed for: " + email);
                        out.write("start: AUTH_RESPONSE\r\n");
                        out.write("status: FAILURE\r\n");
                        out.write("end: AUTH_RESPONSE\r\n");
                        out.flush();
                    }
                } else if (line.equals("start: REGISTER")) {
                    String username = null;
                    String email = null;
                    String password = null;

                    while (!(line = in.readLine()).equals("end: REGISTER")) {
                        if (line.startsWith("username: ")) {
                            username = line.substring("username: ".length());
                        } else if (line.startsWith("email: ")) {
                            email = line.substring("email: ".length());
                        } else if (line.startsWith("password: ")) {
                            password = line.substring("password: ".length());
                        }
                    }

                    System.out.println("Registration attempt for: " + email);

                    STATUS registerStatus = STATUS.FAILURE;

                    if (username != null && email != null && password != null) {
                        registerStatus = AuthHandler.RequestRegister(username, email, password);
                    }
                    if (registerStatus.equals(STATUS.SUCCESS)) {
                        out.write("start: REGISTER_RESPONSE\r\n");
                        out.write("status: SUCCESS\r\n");
                        out.write("username: " + username + "\r\n");
                        out.write("end: REGISTER_RESPONSE\r\n");
                        out.flush();
                    } else {
                        out.write("start: REGISTER_RESPONSE\r\n");
                        out.write("status: FAILURE\r\n");
                        out.write("end: REGISTER_RESPONSE\r\n");
                        out.flush();
                    }
                } else if (line.equals("start: LOGOUT")) {
                    while (!(line = in.readLine()).equals("end: LOGOUT")) {
                        // No parameters needed for logout
                    }

                    removeClientWriter(clientUserId, out);
                    clientUserId = -1;

                    System.out.println("Logout request");
                } else if (line.equals("start: FORGOT_PASSWORD")) {
                    String email = null;
                    String newPassword = null;
                    while (!(line = in.readLine()).equals("end: FORGOT_PASSWORD")) {
                        if (line.startsWith("email: ")) {
                            email = line.substring("email: ".length());
                        } else if (line.startsWith("newPassword: ")) {
                            newPassword = line.substring("newPassword: ".length());
                        }
                    }
                    PrintWriter pw = new PrintWriter(out, true);
                    com.ouroboros.chatapp.chatapp.serverside.UserHandler.handleForgotPassword(email, newPassword, pw);
                } else if (ChatHandler.isCreateChatRequest(line)) {
                    ChatHandler.handleCreateChatRequest(in, out);
                } else if (ChatHandler.isGetChatsRequest(line)) {
                    ChatHandler.handleGetChatsRequest(in, out);
                } else if (line.equals("start: DELETE_ACCOUNT")) {
                    while (!(line = in.readLine()).equals("end: DELETE_ACCOUNT")) {
                        if (line.startsWith("userId: ")) {
                            int userId = Integer.parseInt(line.substring("userId: ".length()));

                            removeClientWriter(userId, out);
                            clientUserId = -1;

                            UserHandler.deleteAccount(userId);
                        }
                    }
                    System.out.println("Delete account request");
                }
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
            String type = "TEXT"; // Default message type
            String fileUrl = null;
            String mediaUrl = null;
            String line;
            while (!(line = in.readLine()).equals("end: SEND_MESSAGE")) {
                if (line.startsWith("chatId: ")) {
                    chatId = Integer.parseInt(line.substring("chatId: ".length()));
                } else if (line.startsWith("senderId: ")) {
                    senderId = Integer.parseInt(line.substring("senderId: ".length()));
                } else if (line.startsWith("messageType: ")) {
                    type = line.substring("messageType: ".length()).toUpperCase();
                } else if (line.startsWith("content: ")) {
                    content = line.substring("content: ".length());
                } else if (line.startsWith("fileUrl: ")) {
                    fileUrl = line.substring("fileUrl: ".length());
                } else if (line.startsWith("mediaUrl: ")) {
                    mediaUrl = line.substring("mediaUrl: ".length());
                }
            }
            System.out.println("senderId: " + senderId);
            System.out.println("chatId: " + chatId);
            System.out.println("content: " + content);
            System.out.println("fileUrl: " + fileUrl);
            System.out.println("mediaUrl: " + mediaUrl);

            if (chatId != -1 && senderId != -1 && content != null) {
                // Handle sending the message (pass fileUrl/mediaUrl)
                MessageHandler.handleSendMessage(chatId, senderId, content, type, fileUrl, mediaUrl, out);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error handling SEND_MESSAGE", e);
        }
    }

    private static void removeClientWriter(long userId, BufferedWriter writer) {
        List<BufferedWriter> writers = clientWriters.get(userId);
        if (writers != null) {
            writers.remove(writer);
            if (writers.isEmpty()) {
                clientWriters.remove(userId);
            }
        }
    }
}


