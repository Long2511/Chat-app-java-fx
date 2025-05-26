package com.ouroboros.chatapp.chatapp.Homepage;

public class ChatPreview {
    private final int chatId;
    private final String title;

    public ChatPreview(int chatId, String title) {
        this.chatId = chatId;
        this.title = title;
    }

    public int getChatId() {
        return chatId;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return title;
    }
}


