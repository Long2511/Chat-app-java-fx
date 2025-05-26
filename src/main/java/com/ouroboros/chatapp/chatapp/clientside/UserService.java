package com.ouroboros.chatapp.chatapp.clientside;

import com.ouroboros.chatapp.chatapp.datatype.STATUS;
import com.ouroboros.chatapp.chatapp.datatype.User;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    private final BufferedReader in;
    private final BufferedWriter out;

    /**
     * Creates a new UserService using the existing socket from ClientConnection
     *
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
     * Retrieves a list of all users from the server
     *
     * @return List of User objects
     * @throws IOException if an I/O error occurs
     */
    public static List<User> getAllUsers() throws IOException {
        BufferedWriter out = ClientConnection.getSharedWriter();
        BufferedReader in = ClientConnection.getSharedReader();
        out.write("start: GET_ALL_USERS\r\n");
        out.write("end: GET_ALL_USERS\r\n");
        out.flush();
        List<User> users = new ArrayList<>();
        String line;
        while (!(line = in.readLine()).equals("end: RESPONSE_USERS")) {
            if (line.startsWith("length: ")) {
                int length = Integer.parseInt(line.substring("length: ".length()));
                for (int i = 0; i < length; i++) {
                    users.add(User.receiveObject(in));
                }
            }
        }
        return users;
    }

    /**
     * Searches for users by name
     *
     * @param query the name or part of the name to search for
     * @return List of User objects matching the search criteria
     * @throws IOException if an I/O error occurs
     */
    public static List<User> searchUsers(String query) throws IOException {
        BufferedWriter out = ClientConnection.getSharedWriter();
        BufferedReader in = ClientConnection.getSharedReader();
        out.write("start: SEARCH_USERS_BY_NAME\r\n");
        out.write("query: " + query + "\r\n");
        out.write("end: SEARCH_USERS_BY_NAME\r\n");
        out.flush();
        List<User> users = new ArrayList<>();
        String line;
        while (!(line = in.readLine()).equals("end: RESPONSE_USERS")) {
            if (line.startsWith("length: ")) {
                int length = Integer.parseInt(line.substring("length: ".length()));
                for (int i = 0; i < length; i++) {
                    users.add(User.receiveObject(in));
                }
            }
        }
        return users;
    }

    public static void deleteAccount(long id) throws IOException {
        BufferedWriter out = ClientConnection.getSharedWriter();
        BufferedReader in = ClientConnection.getSharedReader();

        out.write("start: DELETE_ACCOUNT\r\n");
        out.write("userId: " + id + "\r\n");
        out.write("end: DELETE_ACCOUNT\r\n");
        out.flush();
    }

    /**
     * Authenticates a user with the server
     *
     * @param email    user's email
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
     *
     * @param username user's name
     * @param email    user's email
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


    public static boolean forgotPassword(String email, String newPassword) {
        try {
            BufferedWriter out = ClientConnection.getSharedWriter();
            BufferedReader in = ClientConnection.getSharedReader();
            out.write("start: FORGOT_PASSWORD\r\n");
            out.write("email: " + email + "\r\n");
            out.write("newPassword: " + newPassword + "\r\n");
            out.write("end: FORGOT_PASSWORD\r\n");
            out.flush();
            String line;
            while (!(line = in.readLine()).equals("end: FORGOT_PASSWORD_RESPONSE")) {
                if (line.startsWith("status: ")) {
                    String status = line.substring("status: ".length());
                    return status.equals("SUCCESS");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
