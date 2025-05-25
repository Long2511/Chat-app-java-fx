package com.ouroboros.chatapp.chatapp.datatype;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public class User {
    private int id;
    private String username;
    private String email;

    // Getters
    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void sendObject(BufferedWriter out) throws IOException {
        out.write("<start of object>\r\n");
        out.write("id: " + id + "\r\n");
        out.write("username: " + username + "\r\n");
        out.write("email: " + email + "\r\n");
        out.write("<end of object>\r\n");
    }

    public static User receiveObject(BufferedReader in) throws IOException {
        String line;
        User user = new User();
        while (!(line = in.readLine()).equals("<end of object>")) {
            if (line.startsWith("id: ")) {
                user.id = Integer.parseInt(line.substring("id: ".length()));
            } else if (line.startsWith("username: ")) {
                user.username = line.substring("username: ".length());
            } else if (line.startsWith("email: ")) {
                user.email = line.substring("email: ".length());
            }
        }
        return user;
    }

    public void printUser() {
        System.out.println("User ID: " + id);
        System.out.println("Username: " + username);
        System.out.println("Email: " + email);
    }
}
