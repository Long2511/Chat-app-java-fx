package com.ouroboros.chatapp.chatapp.serverside;


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
            new Thread(() -> handleClient(clientSocket)).start();
        }
    }

    private static void handleClient(Socket clientSocket) {
        Logger logger = Logger.getLogger(ServerBackend.class.getName());
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))) {
            String line = in.readLine();
            if (line == null) return;
            StringTokenizer tokenizer = new StringTokenizer(line);
            String method = tokenizer.nextToken();
            String path = tokenizer.nextToken();

            // Handle CORS preflight requests
            if (method.equals("OPTIONS")) {
                out.write("HTTP/1.1 200 OK\r\n");
                out.write("Access-Control-Allow-Origin: http://localhost:3000\r\n");
                out.write("Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS\r\n");
                out.write("Access-Control-Allow-Headers: Content-Type, Authorization\r\n");
                out.write("\r\n");
                out.flush();
                return;
            }

            // Remove /api prefix for easier matching
            if (path.startsWith("/api/")) path = path.substring(4);

            // User API: GET /users
            if (method.equals("GET") && path.equals("/users")) {
                String response = UserApiHandler.handleGetUsers();
                out.write(response + "\n");
                out.flush();
                return;
            }
            // Auth API: POST /auth/register
            if (method.equals("POST") && path.equals("/auth/register")) {
                String body = ApiUtils.readBody(in);
                String response = AuthApiHandler.handleRegister(body);
                out.write(response + "\n");
                out.flush();
                return;
            }
            // Auth API: POST /auth/login
            if (method.equals("POST") && path.equals("/auth/login")) {
                String body = ApiUtils.readBody(in);
                String response = AuthApiHandler.handleLogin(body);
                out.write(response + "\n");
                out.flush();
                return;
            }

            // Auth API: POST /auth/logout
            if (method.equals("POST") && path.equals("/auth/logout")) {
//                AuthApiHandler.handleLogout(out);
                return;
            }

            // Chat API
            if (method.equals("GET") && path.equals("/chats")) {
//                ChatAPIHandler.handleGetChats(out);
                return;
            } else if (method.equals("POST") && path.startsWith("/chats/private/")) {
//                ChatAPIHandler.handlePostPrivateChat(path, out);
                return;
            } else if (method.equals("POST") && path.startsWith("/chats/group")) {
//                ChatAPIHandler.handlePostGroupChat(path, in, out);
                return;
            }

            // Messages API: GET /messages/chat/{chatId}
//            if (MessagesApiHandler.handleGetMessages(method, path, in, out)) {
//                return;
//            }
            // Messages API: POST /messages/chat/{chatId}
//            if (MessagesApiHandler.handlePostMessage(method, path, in, out)) {
//                return;
//            }

            // Simple string-based API protocol
            if (line.startsWith("GET_USERS")) {
                String response = UserApiHandler.handleGetUsers();
                out.write(response + "\n");
                out.flush();
                return;
            } else if (line.startsWith("REGISTER:")) {
                String json = line.substring("REGISTER:".length());
                String response = AuthApiHandler.handleRegister(json);
                out.write(response + "\n");
                out.flush();
                return;
            } else if (line.startsWith("LOGIN:")) {
                String json = line.substring("LOGIN:".length());
                String response = AuthApiHandler.handleLogin(json);
                out.write(response + "\n");
                out.flush();
                return;
            } else if (line.startsWith("LOGOUT")) {
                String response = AuthApiHandler.handleLogout();
                out.write(response + "\n");
                out.flush();
                return;
            }

            // Default case: Not found
            out.write("HTTP/1.1 404 Not Found\r\nContent-Type: text/plain\r\n\r\nNot found");
            out.flush();
            return;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Cannot connect to Database", e);
        } finally {
            try { clientSocket.close(); } catch (IOException ignored) {}
        }
    }
}
