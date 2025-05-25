package com.ouroboros.chatapp.chatapp.clientside;

import com.ouroboros.chatapp.chatapp.datatype.STATUS;
import com.ouroboros.chatapp.chatapp.datatype.User;

import java.io.*;

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
    public synchronized STATUS login(String email, String password) {
        try {
            // Send login request to server
            out.write("start: LOGIN\r\n");
            out.write("email: " + email + "\r\n");
            out.write("password: " + password + "\r\n");
            out.write("end: LOGIN\r\n");
            out.flush();

            // Read response
            String line;
            while (!(line = in.readLine()).equals("end: AUTH_RESPONSE")) {
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
}
