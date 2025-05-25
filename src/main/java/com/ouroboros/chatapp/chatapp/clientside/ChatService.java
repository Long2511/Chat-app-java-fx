package com.ouroboros.chatapp.chatapp.clientside;

import com.ouroboros.chatapp.chatapp.datatype.Chat;
import com.ouroboros.chatapp.chatapp.datatype.Message;
import com.ouroboros.chatapp.chatapp.datatype.User;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatService {
    private BufferedReader in;
    private BufferedWriter out;
    private final List<Chat> chats = new ArrayList<>();

    /**
     * Creates a new ChatService using the existing socket from ClientConnection
     */
    public ChatService() {
        try {
            Socket socket = ClientConnection.getSharedSocket();
            if (socket == null || socket.isClosed()) {
                throw new IOException("No active socket connection available");
            }
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            System.err.println("Error initializing ChatService: " + e.getMessage());
        }
    }

    /**
     * Sends a message to a specific chat
     * @param message the message to send
     * @return true if sent successfully, false otherwise
     */
    public synchronized boolean sendMessage(Message message) {
        try {
            out.write("start: SEND_MESSAGE\r\n");
            out.write("chatId: " + message.getChatId() + "\r\n");
            out.write("senderId: " + message.getSenderId() + "\r\n");
            out.write("content: " + message.getContent() + "\r\n");
            if (message.getMessageType() != null) {
                out.write("type: " + message.getMessageType() + "\r\n");
            }
            out.write("end: SEND_MESSAGE\r\n");
            out.flush();

            // Read response
            String line;
            while (!(line = in.readLine()).equals("end: MESSAGE_SEND_RESPONSE")) {
                if (line.startsWith("status: ")) {
                    String status = line.substring("status: ".length());
                    return status.equals("SUCCESS");
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves all messages for a specific chat
     * @param chatId the chat ID to get messages for
     * @return list of messages
     */
    public synchronized List<Message> getMessages(int chatId) {
        List<Message> messages = new ArrayList<>();
        try {
            out.write("start: GET_MESSAGES\r\n");
            out.write("chatId: " + chatId + "\r\n");
            out.write("end: GET_MESSAGES\r\n");
            out.flush();

            // Read response
            String line;
            while (!(line = in.readLine()).equals("end: RESPONSE_MESSAGES")) {
                if (line.startsWith("length: ")) {
                    int length = Integer.parseInt(line.substring("length: ".length()));
                    for (int i = 0; i < length; i++) {
                        messages.add(Message.receiveObject(in));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return messages;
    }

    /**
     * Retrieves all chats for a specific user
     * @param userId the user ID to get chats for
     * @return list of chats
     */
    public synchronized List<Chat> getAllChats(int userId) {
        chats.clear();
        try {
            out.write("start: GET_CHATS\r\n");
            out.write("userId: " + userId + "\r\n");
            out.write("end: GET_CHATS\r\n");
            out.flush();

            // Read response
            String line;
            while (!(line = in.readLine()).equals("end: RESPONSE_CHATS")) {
                if (line.startsWith("length: ")) {
                    int length = Integer.parseInt(line.substring("length: ".length()));
                    for (int i = 0; i < length; i++) {
                        chats.add(Chat.receiveObject(in));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>(chats);
    }

    /**
     * Creates a new chat with the given users and chat name
     * @param users list of users to include in the chat
     * @param chatName name of the chat
     * @return the created Chat object, or null if creation failed
     */
    public synchronized Chat createChat(List<User> users, String chatName) {
        try {
            // Extract user IDs
            StringBuilder userIds = new StringBuilder("[");
            for (int i = 0; i < users.size(); i++) {
                if (i > 0) userIds.append(",");
                userIds.append(users.get(i).getId());
            }
            userIds.append("]");

            // Send request
            out.write("start: CREATE_CHAT\r\n");
            out.write("name: " + chatName + "\r\n");
            out.write("userIds: " + userIds.toString() + "\r\n");
            out.write("end: CREATE_CHAT\r\n");
            out.flush();

            // Read response
            String line;
            while (!(line = in.readLine()).equals("end: CHAT_CREATE_RESPONSE")) {
                if (line.startsWith("status: SUCCESS")) {
                    return Chat.receiveObject(in);
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
