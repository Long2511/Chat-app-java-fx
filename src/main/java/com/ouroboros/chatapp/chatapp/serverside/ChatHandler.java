package com.ouroboros.chatapp.chatapp.serverside;

import com.ouroboros.chatapp.chatapp.datatype.Chat;

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
    public static final Map<Integer, List<Integer>> chatUsersMap = Collections.synchronizedMap(new java.util.HashMap<>());
    public static AtomicInteger chatIdCounter = new AtomicInteger(1);

    public static boolean isCreateChatRequest(String method) {
        return method.equals("start: CREATE_CHAT");
    }

    public static boolean isGetChatsRequest(String method) {
        return method.equals("start: GET_ALL_CHATS");
    }

    public static boolean handleCreateChatRequest(BufferedReader in, BufferedWriter out) throws IOException {
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
            return false;
        }

        // Create a new chat
        int chatId = chatIdCounter.getAndIncrement();
        Chat newChat = new Chat();
        newChat.setId(chatId);
        newChat.setName(chatName);
        newChat.setType(userIds.size() > 2 ? "GROUP" : "PRIVATE");

        if (!chatUsersMap.containsKey(chatId)) {
            chatUsersMap.put(chatId, new ArrayList<>());
        }
        for (int userId : userIds) {
            chatUsersMap.get(chatId).add(userId);
        }

        // Add the chat to the list of chats
        chats.add(newChat);

        // TODO: Save to database if needed
        // DatabaseUtils.saveChat(newChat, userIds);

        // Send response
        out.write("start: RESPONSE_CREATE_CHAT\r\n");
        out.write("status: SUCCESS\r\n");
        out.write("chatId: " + chatId + "\r\n");
        out.write("end: RESPONSE_CREATE_CHAT\r\n");
        out.flush();

        return true;
    }

    public static boolean handleGetChatsRequest(BufferedReader in, BufferedWriter out) throws IOException {
        int userId = -1;
        String line;
        while (!(line = in.readLine()).equals("end: GET_ALL_CHATS")) {
            if (line.startsWith("userId: ")) {
                userId = Integer.parseInt(line.substring("userId: ".length()));
            }
        }

        List<Chat> resultChats = new ArrayList<>();

        for (Chat chat : chats) {
            if (chatUsersMap.containsKey(chat.getId()) && chatUsersMap.get(chat.getId()).contains(userId)) {
                // If the user is part of the chat, we can send it
                resultChats.add(chat);
            }
        }

        out.write("start: RESPONSE_GET_ALL_CHATS\r\n");
        out.write("length: " + resultChats.size() + "\r\n");
        for (Chat chat : resultChats) {
            chat.sendObject(out);
        }
        out.write("end: RESPONSE_GET_ALL_CHATS\r\n");
        out.flush();
        return true;
    }
}
