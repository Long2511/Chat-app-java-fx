package com.ouroboros.chatapp.chatapp.Homepage;

public class ChatPreview {

    private String username;
    private String lastMessage;
    private String time;

    public ChatPreview(String username, String lastMessage, String time) {
        this.username = username;
        this.lastMessage = lastMessage;
        this.time = time;
    }

    public String getUsername() {
        return username;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public String getTime() {
        return time;
    }

    // Optional: for debug/log
    @Override
    public String toString() {
        return username + ": " + lastMessage + " (" + time + ")";
    }
}
