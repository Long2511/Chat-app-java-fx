package com.ouroboros.chatapp.chatapp.datatype;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Chat {
    private long id;
    private String name;
    private String type;
    private String createdAt;
    private String updatedAt;
    private List<User> participants;

    public Chat() {}

    public Chat(int chatId, String chatName, List<User> users) {
        this.id = chatId;
        this.name = chatName;
        this.type = users.size() > 2 ? "GROUP" : "PRIVATE";
        this.createdAt = java.time.LocalDateTime.now().toString();
        this.updatedAt = createdAt;
        this.participants = users;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public List<User> getParticipants() { return participants; }

    public void setId(long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public void setParticipants(List<User> participants) { this.participants = participants; }

    public void sendObject(BufferedWriter out) throws IOException {
        out.write("<start of object>\r\n");
        out.write("id: " + id + "\r\n");
        out.write("name: " + name + "\r\n");
        out.write("type: " + type + "\r\n");
        out.write("createdAt: " + createdAt + "\r\n");
        out.write("updatedAt: " + updatedAt + "\r\n");

        if (participants != null) {
            out.write("participants: " + participants.size() + "\r\n");
            for (User user : participants) {
                user.sendObject(out);
            }
        } else {
            out.write("participants: 0\r\n");
        }

        out.write("<end of object>\r\n");
    }

    public static Chat receiveObject(BufferedReader in) throws IOException {
        String line;
        Chat chat = new Chat();
        while (!(line = in.readLine()).equals("<end of object>")) {
            if (line.startsWith("id: ")) {
                chat.setId(Long.parseLong(line.substring("id: ".length())));
            } else if (line.startsWith("name: ")) {
                chat.setName(line.substring("name: ".length()));
            } else if (line.startsWith("type: ")) {
                chat.setType(line.substring("type: ".length()));
            } else if (line.startsWith("createdAt: ")) {
                chat.setCreatedAt(line.substring("createdAt: ".length()));
            } else if (line.startsWith("updatedAt: ")) {
                chat.setUpdatedAt(line.substring("updatedAt: ".length()));
            } else if (line.startsWith("participants: ")) {
                int count = Integer.parseInt(line.substring("participants: ".length()));
                List<User> participants = new ArrayList<>();
                for (int i = 0; i < count; i++) {
                    participants.add(User.receiveObject(in));
                }
                chat.setParticipants(participants);
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
                ", participants=" + (participants != null ? participants.size() : "null") +
                '}';
    }
}
