package com.ouroboros.chatapp.chatapp.datatype;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public class User {
    private long id;
    private String username;
    private String email;
    private String avatar;
    private String status;

    // Getters
    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getStatus() {
        return status;
    }

    // Setters
    public void setId(long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void sendObject(BufferedWriter out) throws IOException {
        out.write("<start of object>\r\n");
        out.write("id: " + id + "\r\n");
        out.write("username: " + username + "\r\n");
        out.write("email: " + email + "\r\n");
        out.write("avatar: " + (avatar != null ? avatar : "") + "\r\n");
        out.write("status: " + (status != null ? status : "offline") + "\r\n");
        out.write("<end of object>\r\n");
    }

    public static User receiveObject(BufferedReader in) throws IOException {
        String line;
        User user = new User();
        while (!(line = in.readLine()).equals("<end of object>")) {
            if (line.startsWith("id: ")) {
                user.id = Long.parseLong(line.substring("id: ".length()));
            } else if (line.startsWith("username: ")) {
                user.username = line.substring("username: ".length());
            } else if (line.startsWith("email: ")) {
                user.email = line.substring("email: ".length());
            } else if (line.startsWith("avatar: ")) {
                user.avatar = line.substring("avatar: ".length());
            } else if (line.startsWith("status: ")) {
                user.status = line.substring("status: ".length());
            }
        }
        return user;
    }
}
