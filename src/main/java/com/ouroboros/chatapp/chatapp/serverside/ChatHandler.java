package com.ouroboros.chatapp.chatapp.serverside;

import com.ouroboros.chatapp.chatapp.datatype.Chat;
import com.ouroboros.chatapp.chatapp.datatype.User;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ChatHandler {
    public static final List<Chat> chats = Collections.synchronizedList(new ArrayList<>());
    // TODO: chatUsersMap should be replaced with a database solution for scalability
    public static final Map<Integer, List<Integer>> chatUsersMap = Collections.synchronizedMap(new java.util.HashMap<>());
    public static AtomicInteger chatIdCounter = new AtomicInteger(1);

    public static boolean isCreateChatRequest(String method) {
        return method.equals("start: CREATE_CHAT");
    }

    public static boolean isGetChatsRequest(String method) {
        return method.equals("start: GET_ALL_CHATS");
    }

    public static void handleCreateChatRequest(BufferedReader in, BufferedWriter out) throws IOException {
        String chatName = null;
        List<Integer> userIds = new ArrayList<>();
        int userCount = 0;

        String line;
        while (!(line = in.readLine()).equals("end: CREATE_CHAT")) {
            if (line.startsWith("chatName: ")) {
                chatName = line.substring("chatName: ".length());
            } else if (line.startsWith("users: ")) {
                userCount = Integer.parseInt(line.substring("users: ".length()));
            } else if (line.startsWith("userId: ")) {
                userIds.add(Integer.parseInt(line.substring("userId: ".length())));
            }
        }

        // Validate request data
        if (chatName == null || userIds.isEmpty() || userIds.size() != userCount) {
            out.write("start: RESPONSE_CREATE_CHAT\r\n");
            out.write("status: FAILURE\r\n");
            out.write("message: Invalid chat creation request\r\n");
            out.write("end: RESPONSE_CREATE_CHAT\r\n");
            out.flush();
            return;
        }

        // Create a new chat object (no id yet)
        Chat newChat = new Chat();
        newChat.setName(chatName);
        newChat.setType(userIds.size() > 2 ? "GROUP" : "PRIVATE");

        // Save to database and get the generated chatId
        long chatId = com.ouroboros.chatapp.chatapp.serverside.DatabaseUtils.saveChatAndReturnId(newChat, userIds); // <-- returns chatId
        newChat.setId(chatId);

        // Send response with chatId from DB
        out.write("start: RESPONSE_CREATE_CHAT\r\n");
        out.write("status: SUCCESS\r\n");
        out.write("chatId: " + chatId + "\r\n");
        out.write("end: RESPONSE_CREATE_CHAT\r\n");
        out.flush();
    }

    public static boolean handleGetChatsRequest(BufferedReader in, BufferedWriter out) throws IOException {
        int userId = -1;
        String line;
        while (!(line = in.readLine()).equals("end: GET_ALL_CHATS")) {
            System.out.println("DEBUG: received line = " + line);//debug
            if (line.startsWith("userId: ")) {
                userId = Integer.parseInt(line.substring("userId: ".length()));
                System.out.println("DEBUG: Received get chats request for userId: " + userId);
            }
        }


    
    // Load chats from database instead of memory
    List<Chat> resultChats = DatabaseUtils.loadChatsForUser(userId);
    System.out.println("DEBUG: Loaded " + resultChats.size() + " chats from database for user " + userId); //debug

    out.write("start: RESPONSE_GET_ALL_CHATS\r\n");
    out.write("length: " + resultChats.size() + "\r\n");

    System.out.println("DEBUG: Sending RESPONSE_GET_ALL_CHATS with " + resultChats.size() + " chats");//debug
    
    for (Chat chat : resultChats) {
        chat.sendObject(out);

        //participants
        List<User> participants = chat.getParticipants() != null ? chat.getParticipants() : new ArrayList<>();
        out.write("participants: " + participants.size() + "\r\n");
        for (User user : participants) {
            user.sendObject(out);
        }

        System.out.println("DEBUG: Sent chat: " + chat.getId() + " - " + chat.getName());//debug
    }
    out.write("end: RESPONSE_GET_ALL_CHATS\r\n");
    out.flush();
    return true;
    
}
}
