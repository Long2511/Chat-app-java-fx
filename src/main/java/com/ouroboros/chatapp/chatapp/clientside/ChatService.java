package com.ouroboros.chatapp.chatapp.clientside;

import com.ouroboros.chatapp.chatapp.datatype.Chat;
import com.ouroboros.chatapp.chatapp.datatype.Message;
import com.ouroboros.chatapp.chatapp.datatype.User;

import java.util.List;

public class ChatService {
    public static void sendMessage(Message message) {
        // TODO: Implement the logic to send a message to the server
    }

    public static Message getMessages(int chatId) {
        // TODO: Implement the logic to retrieve messages from the server for a specific chat
        return null;
    }

    public static List<Chat> getAllChats(int userId) {
        // TODO: Implement the logic to retrieve all chats for a specific user
        return null;
    }

    public static Chat createChat(List<User> users, String chatName) {
        // TODO: Implement the logic to create a new chat with the given users and chat name
        return null;
    }
}
