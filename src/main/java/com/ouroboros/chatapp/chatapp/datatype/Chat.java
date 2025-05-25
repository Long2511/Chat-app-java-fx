package com.ouroboros.chatapp.chatapp.datatype;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

public class Chat {
    private int id;
    private String name;
    private String type;
    private String createdAt;
    private String updatedAt;
    private List<User> users;

    // Constructor with parameters
    public Chat(int id, String name, List<User> users) {
        this.id = id;
        this.name = name;
        this.users = users;
    }


    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public List<User> getUsers() {
        return users;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    // Serialization method
    public void sendObject(BufferedWriter out) throws IOException {
        out.write("<start of object>\r\n");
        out.write("id: " + id + "\r\n");
        out.write("name: " + name + "\r\n");
        out.write("type: " + type + "\r\n");
        out.write("createdAt: " + createdAt + "\r\n");
        out.write("updatedAt: " + updatedAt + "\r\n");
        out.write("<end of object>\r\n");
    }

    // Deserialization method
    public static Chat receiveObject(BufferedReader in) throws IOException {
        String line;
        Chat chat = new Chat();
        while (!(line = in.readLine()).equals("<end of object>")) {
            if (line.startsWith("id: ")) {
                chat.id = Integer.parseInt(line.substring("id: ".length()));
            } else if (line.startsWith("name: ")) {
                chat.name = line.substring("name: ".length());
            } else if (line.startsWith("type: ")) {
                chat.type = line.substring("type: ".length());
            } else if (line.startsWith("createdAt: ")) {
                chat.createdAt = line.substring("createdAt: ".length());
            } else if (line.startsWith("updatedAt: ")) {
                chat.updatedAt = line.substring("updatedAt: ".length());
            }
        }
        return chat;
    }

    // Debugging method
    public void printChat() {
        System.out.println("Chat ID: " + id);
        System.out.println("Name: " + name);
        System.out.println("Type: " + type);
        System.out.println("Created At: " + createdAt);
        System.out.println("Updated At: " + updatedAt);
    }
}