package com.ouroboros.chatapp.chatapp.clientside;

import com.ouroboros.chatapp.chatapp.datatype.STATUS;
import com.ouroboros.chatapp.chatapp.datatype.User;

import java.io.*;
import java.util.List;

public class UserService {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8080;

    private BufferedReader in;
    private BufferedWriter out;

    /**
     * Creates a new UserService using the existing socket from ClientConnection
     * @throws IOException if connection fails
     */
    public UserService() throws IOException {
        try {
            this.in = ClientConnection.getSharedReader();
            this.out = ClientConnection.getSharedWriter();
        } catch (IOException e) {
            System.err.println("Error initializing UserService: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Authenticates a user with the server
     * @param email user's email
     * @param password user's password
     * @return STATUS indicating login success or failure
     */
    public synchronized User login(String email, String password) {
        try {
            // Send login request to server
            out.write("start: LOGIN\r\n");
            out.write("email: " + email + "\r\n");
            out.write("password: " + password + "\r\n");
            out.write("end: LOGIN\r\n");
            out.flush();

            String status = "FAILURE"; // Default status
            User user = new User();
            user.setEmail(email);
            // Read response
            String line;
            while (!(line = in.readLine()).equals("end: AUTH_RESPONSE")) {
                if (line.startsWith("status: ")) {
                    status = line.substring("status: ".length());
                } else if (line.startsWith("username: ")) {
                    user.setUsername(line.substring("username: ".length()));
                } else if (line.startsWith("userId: ")) {
                    user.setId(Integer.parseInt(line.substring("userId: ".length())));
                }
            }
            if (status.equals("SUCCESS")) {
                return user; // Return user object on successful login
            } else {
                return null; // Return null if login failed
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Registers a new user with the server
     * @param username user's name
     * @param email user's email
     * @param password user's password
     * @return STATUS indicating registration success or failure
     */
    public synchronized STATUS register(String username, String email, String password) {
        try {
            // Send registration request to server
            out.write("start: REGISTER\r\n");
            out.write("username: " + username + "\r\n");
            out.write("email: " + email + "\r\n");
            out.write("password: " + password + "\r\n");
            out.write("end: REGISTER\r\n");
            out.flush();

            // Read response
            String line;
            while (!(line = in.readLine()).equals("end: REGISTER_RESPONSE")) {
                if (line.startsWith("status: ")) {
                    String status = line.substring("status: ".length());
                    if (status.equals("SUCCESS")) {
                        return STATUS.SUCCESS;
                    } else {
                        return STATUS.FAILURE;
                    }
                }
            }
            return STATUS.FAILURE; // Default to failure if no status was received
        } catch (Exception e) {
            e.printStackTrace();
            return STATUS.FAILURE;
        }
    }

    /**
     * Logs out the current user
     * @return STATUS indicating logout success or failure
     */
    public synchronized STATUS logout() {
        try {
            // Send logout request to server
            out.write("start: LOGOUT\r\n");
            out.write("end: LOGOUT\r\n");
            out.flush();

            // Read response
            String line;
            while (!(line = in.readLine()).equals("end: LOGOUT_RESPONSE")) {
                if (line.startsWith("status: ")) {
                    String status = line.substring("status: ".length());
                    if (status.equals("SUCCESS")) {
                        return STATUS.SUCCESS;
                    } else {
                        return STATUS.FAILURE;
                    }
                }
            }
            return STATUS.SUCCESS; // Default to success for logout even if no response
        } catch (Exception e) {
            e.printStackTrace();
            return STATUS.FAILURE;
        }
    }

    public static List<User> getAllUsers() {
        try {
            // Send request to get all users
            BufferedWriter out = ClientConnection.getSharedWriter();
            BufferedReader in = ClientConnection.getSharedReader();
            out.write("start: GET_ALL_USERS\r\n");
            out.write("end: GET_ALL_USERS\r\n");
            out.flush();

            // Read response
            String line;
            List<User> users = new java.util.ArrayList<>();
            while (!(line = in.readLine()).equals("end: GET_ALL_USERS_RESPONSE")) {
                if (line.startsWith("user: ")) {
                    String userData = line.substring("user: ".length());
                    String[] parts = userData.split(",");
                    User user = new User();
                    user.setId(Integer.parseInt(parts[0]));
                    user.setUsername(parts[1]);
                    user.setEmail(parts[2]);
                    users.add(user);
                }
            }
            return users;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
