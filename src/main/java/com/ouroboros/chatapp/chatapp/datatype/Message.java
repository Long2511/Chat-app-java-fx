package com.ouroboros.chatapp.chatapp.datatype;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDateTime;


public class Message {
    private int id;
    private int senderId;
    private int chatId;
    private String content;
    private String messageType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static final String TYPE_FILE  = "FILE";

    // Getters
    public int getId() {
        return id;
    }

    public int getSenderId() {
        return senderId;
    }

    public int getChatId() {
        return chatId;
    }

    public String getContent() {
        return content;
    }

    public String getMessageType() {
        return messageType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public void setChatId(int chatId) {
        this.chatId = chatId;
    }

    public void setContent(String content) {
        this.content = content;
    }
    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void sendObject(BufferedWriter out) throws IOException {
        out.write("<start of object>\r\n");
        out.write("id: " + id + "\r\n");
        out.write("senderId: " + senderId + "\r\n");
        out.write("chatId: " + chatId + "\r\n");
        out.write("content: " + content + "\r\n");
        out.write("messageType: " + messageType + "\r\n");
        out.write("createdAt: " + createdAt + "\r\n");
        out.write("updatedAt: " + updatedAt + "\r\n");
        out.write("<end of object>\r\n");
    }

    public static Message receiveObject(BufferedReader in) throws IOException {
        String line;
        Message message = new Message();
        while (!(line = in.readLine()).equals("<end of object>")) {
            if (line.startsWith("id: ")) {
                message.id = Integer.parseInt(line.substring("id: ".length()));
            } else if (line.startsWith("senderId: ")) {
                message.senderId = Integer.parseInt(line.substring("senderId: ".length()));
            } else if (line.startsWith("chatId: ")) {
                message.chatId = Integer.parseInt(line.substring("chatId: ".length()));
            } else if (line.startsWith("content: ")) {
                message.content = line.substring("content: ".length());
            } else if (line.startsWith("messageType: ")) {
                message.messageType = line.substring("messageType: ".length());
            } else if (line.startsWith("createdAt: ")) {
                message.createdAt = LocalDateTime.parse(line.substring("createdAt: ".length()));
            } else if (line.startsWith("updatedAt: ")) {
                message.updatedAt = LocalDateTime.parse(line.substring("updatedAt: ".length()));
            }
        }
        return message;
    }

    public void printMessage() {
        System.out.println("Message ID: " + id);
        System.out.println("Sender ID: " + senderId);
        System.out.println("Chat ID: " + chatId);
        System.out.println("Content: " + content);
        System.out.println("Message Type: " + messageType);
        System.out.println("Created At: " + createdAt);
        System.out.println("Updated At: " + updatedAt);
    }

    public boolean isFile() {
        return TYPE_FILE.equalsIgnoreCase(this.messageType);
    }
}
