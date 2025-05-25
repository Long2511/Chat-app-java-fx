package com.ouroboros.chatapp.chatapp.clientside;

import com.ouroboros.chatapp.chatapp.datatype.Chat;
import com.ouroboros.chatapp.chatapp.datatype.Message;
import com.ouroboros.chatapp.chatapp.datatype.User;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatService {

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

    public synchronized static List<Chat> getAllChats(int userId) {
        List<Chat> chats = new ArrayList<>();
        try {
            out.write("start: GET_ALL_CHATS\r\n");
            out.write("userId: " + userId + "\r\n");
            out.write("end: GET_ALL_CHATS\r\n");
            out.flush();

            String line;
            while (!(line = in.readLine()).equals("end: RESPONSE_CHATS")) {
                if (line.startsWith("length: ")) {
                    int length = Integer.parseInt(line.substring("length: ".length()));
                    for (int i = 0; i < length; i++) {
                        chats.add(Chat.receiveObject(in));
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error retrieving chats: " + e.getMessage());
        }
        return chats;
    }

    public synchronized static Chat createChat(List<User> users, String chatName) {
        try {
            out.write("start: CREATE_CHAT\r\n");
            out.write("chatName: " + chatName + "\r\n");
            out.write("users: " + users.size() + "\r\n");
            for (User user : users) {
                out.write("userId: " + user.getId() + "\r\n");
            }
            out.write("end: CREATE_CHAT\r\n");
            out.flush();

            String line;
            while (!(line = in.readLine()).equals("end: RESPONSE_CREATE_CHAT")) {
                if (line.startsWith("chatId: ")) {
                    int chatId = Integer.parseInt(line.substring("chatId: ".length()));
                    return new Chat(chatId, chatName, users);
                }
            }
        } catch (IOException e) {
            System.err.println("Error creating chat: " + e.getMessage());
        }
        return null;
    }

    public synchronized static Chat getChatDetails(int chatId) {
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
            System.err.println("Error fetching chat details: " + e.getMessage());
        }
        return null;
    }

    public synchronized static List<User> searchUsers(ObservableList<String> selectedUsers) {
        return null;
    }

}