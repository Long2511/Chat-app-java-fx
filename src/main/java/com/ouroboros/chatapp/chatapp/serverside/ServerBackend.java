package com.ouroboros.chatapp.chatapp.serverside;

import com.ouroboros.chatapp.chatapp.datatype.Chat;
import com.ouroboros.chatapp.chatapp.datatype.STATUS;
import com.ouroboros.chatapp.chatapp.datatype.User;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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

                    System.out.println("Logout request");
                } else if (ChatHandler.isCreateChatRequest(line)) {
                    ChatHandler.handleCreateChatRequest(in, out);
                } else if (ChatHandler.isGetChatsRequest(line)) {
                    ChatHandler.handleGetChatsRequest(in, out);
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
}
