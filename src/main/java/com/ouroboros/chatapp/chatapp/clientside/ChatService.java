package com.ouroboros.chatapp.chatapp.clientside;

import com.ouroboros.chatapp.chatapp.datatype.Chat;
import com.ouroboros.chatapp.chatapp.datatype.Message;
import com.ouroboros.chatapp.chatapp.datatype.User;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import com.ouroboros.chatapp.chatapp.serverside.DatabaseUtils;


public class ChatService {

    private static final List<User> searchResults = new ArrayList<>();
    private static BufferedReader in;
    private static BufferedWriter out;

    static {
        try {
            Socket socket = ClientConnection.getSharedSocket();
            if (socket == null || socket.isClosed()) {
                throw new IOException("No active socket connection available");
            }
            in = ClientConnection.getSharedReader();
            out = ClientConnection.getSharedWriter();
        } catch (IOException e) {
            System.err.println("Error initializing ChatService: " + e.getMessage());
        }
    }

    public synchronized static List<User> searchUsers(List<String> usernames) {
        try {
            out.write("start: SEARCH_USERS_BY_NAME\r\n");
            out.write("length: " + usernames.size() + "\r\n");
            for (String username : usernames) {
                out.write("username: " + username + "\r\n");
            }
            out.write("end: SEARCH_USERS_BY_NAME\r\n");
            out.flush();

            return receiveUsers();
        } catch (IOException e) {
            System.err.println("Error searching users: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private synchronized static List<User> receiveUsers() throws IOException {
        String line;
        searchResults.clear();
        while (!(line = in.readLine()).equals("end: RESPONSE_USERS")) {
            if (line.startsWith("length: ")) {
                int length = Integer.parseInt(line.substring("length: ".length()));
                for (int i = 0; i < length; i++) {
                    searchResults.add(User.receiveObject(in));
                }
            }
        }
        return new ArrayList<>(searchResults);
    }

    public synchronized static Message getMessages(int chatId) {
        return null;
    }

    public synchronized static List<Chat> getAllChats(int userId) {
        List<Chat> chats = new ArrayList<>();
        try {
            out.write("start: GET_ALL_CHATS\r\n");
            out.write("userId: " + userId + "\r\n");
            out.write("end: GET_ALL_CHATS\r\n");
            out.flush();

            String line;
            System.out.println("Sending request to get all chats for userId = " + userId);//debuging
            
            while (!(line = in.readLine()).equals("end: RESPONSE_GET_ALL_CHATS")) {
                if (line.startsWith("length: ")) {
                    int length = Integer.parseInt(line.substring("length: ".length()));
                    for (int i = 0; i < length; i++) {
                        Chat chat = Chat.receiveObject(in);

                    // Đọc số lượng participants
                        line = in.readLine();
                        if (line.startsWith("participants: ")) {
                            int count = Integer.parseInt(line.substring("participants: ".length()));
                            List<User> participants = new ArrayList<>();
                            for (int j = 0; j < count; j++) {
                                participants.add(User.receiveObject(in));
                            }
                            chat.setParticipants(participants);
                        }

                        chats.add(chat);
                        System.out.println("Chat received: " + chat.getName());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error retrieving chats: " + e.getMessage());
        }
        return chats;
    }

    /*public synchronized static Chat createChat(List<User> users, String chatName) {
        try {
            out.write("start: CREATE_CHAT\r\n");
            out.write("name: " + chatName + "\r\n");
            out.write("users: " + users.size() + "\r\n");
            for (User user : users) {
                user.sendObject(out);
            }
            out.write("end: CREATE_CHAT\r\n");
            out.flush();

            // Receive response
            String line;
            while (!(line = in.readLine()).equals("end: RESPONSE_CREATE_CHAT")) {
                if (line.equals("start: RESPONSE_CREATE_CHAT")) {
                    if (line.startsWith("chatId: ")) {
                        int chatId = Integer.parseInt(line.substring("chatId: ".length()));
                        return new Chat(chatId, chatName, users);
                    }
                }
            }
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
*/
    public static Chat getChatDetails(int chatId) {
        try {
            out.write("start: GET_CHAT_DETAILS\r\n");
            out.write("chatId: " + chatId + "\r\n");
            out.write("end: GET_CHAT_DETAILS\r\n");
            out.flush();

            String line;
            while (!(line = in.readLine()).equals("end: RESPONSE_CHAT_DETAILS")) {
                if (line.startsWith("chatId: ")) {
                    return Chat.receiveObject(in);
                }
            }
        } catch (IOException e) {
            System.err.println("Error creating chat: " + e.getMessage());
        }
        return null;
    }
    

    public synchronized static Chat createChat(List<Integer> userIds, String chatNameStr) {
    try {
        String chatType = (userIds.size() > 2) ? "GROUP" : "PRIVATE";
        String chatName = chatType.equals("GROUP")
                ? (chatNameStr == null || chatNameStr.trim().isEmpty() ? "Group Chat" : chatNameStr)
                : ""; // PRIVATE chats may not need a name

        out.write("start: CREATE_CHAT\r\n");
        out.write("chatName: " + chatName + "\r\n");
        out.write("users: " + userIds.size() + "\r\n");
        for (Integer userId : userIds) {
            out.write("userId: " + userId + "\r\n");
        }
        out.write("end: CREATE_CHAT\r\n");
        out.flush();

        // Đọc phản hồi từ server (ví dụ chatId)
        int chatId = -1;
        String line;
        while (!(line = in.readLine()).equals("end: RESPONSE_CREATE_CHAT")) {
            if (line.startsWith("chatId: ")) {
                chatId = Integer.parseInt(line.substring("chatId: ".length()));
            }

    /**
     * Create a chat group and return the new chat's ID (or -1 on failure)
     */
    /*public synchronized static int createChatGroup(List<Integer> userIds, String chatName) {
        try {
            out.write("start: CREATE_CHAT\r\n");
            out.write("chatName: " + chatName + "\r\n");
            out.write("users: " + userIds.size() + "\r\n");
            for (Integer userId : userIds) {
                out.write("userId: " + userId + "\r\n");
            }
            out.write("end: CREATE_CHAT\r\n");
            out.flush();
            String line;
            int chatId = -1;
            while (!(line = in.readLine()).equals("end: RESPONSE_CREATE_CHAT")) {
                if (line.startsWith("chatId: ")) {
                    chatId = Integer.parseInt(line.substring("chatId: ".length()));
                }
            }
            return chatId;
        } catch (IOException e) {
            System.err.println("Error creating chat group: " + e.getMessage());
            return -1;
            */

        }

        return new Chat(chatId, chatName, null);
    } catch (IOException e) {
        System.err.println("Error creating chat: " + e.getMessage());
        return null;
    }
}
}