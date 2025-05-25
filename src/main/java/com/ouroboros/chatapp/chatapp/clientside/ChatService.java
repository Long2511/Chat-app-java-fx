package com.ouroboros.chatapp.chatapp.clientside;

import com.ouroboros.chatapp.chatapp.datatype.Chat;
import com.ouroboros.chatapp.chatapp.datatype.User;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChatService {

    public static Chat createChat(List<User> users, String chatName) {
        try {
            BufferedWriter out = ClientConnection.getSharedWriter();
            BufferedReader in = ClientConnection.getSharedReader();

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
            e.printStackTrace();
        }
        return null;
    }

    public static List<User> searchUsers(List<String> usernames) {
        List<User> users = new ArrayList<>();
        try {
            BufferedWriter out = ClientConnection.getSharedWriter();
            BufferedReader in = ClientConnection.getSharedReader();

            out.write("start: SEARCH_USERS\r\n");
            for (String username : usernames) {
                out.write("username: " + username + "\r\n");
            }
            out.write("end: SEARCH_USERS\r\n");
            out.flush();

            String line;
            while (!(line = in.readLine()).equals("end: RESPONSE_SEARCH_USERS")) {
                if (line.startsWith("userId: ")) {
                    int userId = Integer.parseInt(line.substring("userId: ".length()));
                    String username = in.readLine().substring("username: ".length());
                    String email = in.readLine().substring("email: ".length());
                    users.add(new User(userId, username, email));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return users;
    }
}