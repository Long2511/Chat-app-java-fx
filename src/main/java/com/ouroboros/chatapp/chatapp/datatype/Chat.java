package com.ouroboros.chatapp.chatapp.datatype;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

public class Chat {
    private long id;
    private String name;
    private String type;
    private String createdAt;
    private String updatedAt;

    public Chat() {}

    public Chat(int chatId, String chatName, List<User> users) {
        this.id = chatId;
        this.name = chatName;
        this.type = users.size() > 2 ? "GROUP" : "PRIVATE"; // Determine type based on number of users
        this.createdAt = java.time.LocalDateTime.now().toString();
        this.updatedAt = createdAt; // Initially set to created time
    }

    // Getters
    public long getId() {
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

    // Setters
    public void setId(long id) {
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

    public void sendObject(BufferedWriter out) throws IOException {
        out.write("<start of object>\r\n");
        out.write("id: " + id + "\r\n");
        out.write("name: " + name + "\r\n");
        out.write("type: " + type + "\r\n");
        out.write("createdAt: " + createdAt + "\r\n");
        out.write("updatedAt: " + updatedAt + "\r\n");
        out.write("<end of object>\r\n");
    }

    public static Chat receiveObject(BufferedReader in) throws IOException {
        String line;
        Chat chat = new Chat();
        while (!(line = in.readLine()).equals("<end of object>")) {
            if (line.startsWith("id: ")) {
                chat.id = Long.parseLong(line.substring("id: ".length()));
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

    @Override
    public String toString() {
        return "Chat{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                '}';
    }
}
