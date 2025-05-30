package com.ouroboros.chatapp.chatapp.serverside;

import com.ouroboros.chatapp.chatapp.datatype.Message;

import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.ouroboros.chatapp.chatapp.serverside.ChatHandler.chatUsersMap;
import static com.ouroboros.chatapp.chatapp.serverside.ServerBackend.clientWriters;

public class MessageHandler {
    private static final Logger logger = Logger.getLogger(MessageHandler.class.getName());
    public static final List<Message> messages = Collections.synchronizedList(new ArrayList<>());
    public static final AtomicInteger messageIdCounter = new AtomicInteger(1);


    public static void fetchDataFromDatabase() {
        // Whenever the server starts, fetch all messages from the database and continue from there
        try (Connection conn = DatabaseUtils.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, chat_id, sender_id, content, message_type, media_url, created_at, updated_at FROM messages ORDER BY id ASC")) {

            int maxId = 0;
            while (rs.next()) {
                // TODO: add more attributes
                Message msg = new Message();
                int id = rs.getInt("id");
                msg.setId(rs.getInt("id"));
                msg.setChatId(rs.getInt("chat_id"));
                msg.setSenderId(rs.getInt("sender_id"));
                msg.setContent(rs.getString("content"));
                msg.setMessageType(rs.getString("message_type"));

                messages.add(msg);
                if (id > maxId) {
                    maxId = id;
                }
            }
            messageIdCounter.set(maxId + 1);
            System.out.println("Fetched " + messages.size() + " messages from database.");
        } catch (Exception e) {
            System.err.println("Error fetching messages from database: " + e.getMessage());
        }
    }

    public static void handleRequestMessages(int chatId, BufferedWriter out) {
        synchronized (messages) {
            try {
                long length = messages.stream().filter(m -> m.getChatId() == chatId).count();
                System.out.println("Length of messages for chat ID " + chatId + ": " + length);

                out.write("start: RESPONSE_MESSAGES\r\n");
                out.write("length: " + length + "\r\n");
                for (Message msg : messages) {
                    if (msg.getChatId() == chatId) {
                        msg.sendObject(out);
                    }
                    System.out.println("message content: " + msg.getContent());
                }
                out.write("end: RESPONSE_MESSAGES\r\n");
                out.flush();
                logger.info("Sent " + length + " messages for chat ID: " + chatId);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error sending messages for chat ID: " + chatId, e);
            }
        }
    }


    public static void handleSendMessage(int chatId, int senderId, String content, String type, String fileUrl, String mediaUrl, BufferedWriter out) {
        synchronized (messages) {
            try {
                // Create a new message object
                Message newMsg = new Message();
                newMsg.setId(messageIdCounter.getAndIncrement());
                newMsg.setChatId(chatId);
                newMsg.setSenderId(senderId);
                newMsg.setContent(content);
                newMsg.setMessageType(type);
                newMsg.setFileUrl(fileUrl);
                newMsg.setMediaUrl(mediaUrl);
                LocalDateTime now = LocalDateTime.now();
                newMsg.setCreatedAt(now);
                newMsg.setUpdatedAt(now);

                // Add the new message to the list
                messages.add(newMsg);

                // Save the message to the database in a new thread
                new Thread(() -> saveMessageToDatabase(newMsg)).start();

                System.out.println("Hello, sent back to client: " + newMsg.getContent());
                System.out.println("fileUrl: " + newMsg.getFileUrl());
                System.out.println("mediaUrl: " + newMsg.getMediaUrl());

                System.out.println("BufferedWriter out: " + out);
                // send notification to the client
                out.write("start: ADD_NEW_MESSAGE\r\n");
                out.write("length: 1\r\n");
                newMsg.sendObject(out);
                out.write("end: ADD_NEW_MESSAGE\r\n");
                out.flush();

                /// Handle realtime update message
                // sent notify to other clients in the chat
                List<Integer> userIdsInChat = DatabaseUtils.getUserIdsInChat(chatId);
                for (int userIdInChat : userIdsInChat) {
                    System.out.println("User ID in chat: " + userIdInChat);
                    if (userIdInChat != senderId && clientWriters.get((long) userIdInChat) != null) { // Don't notify the sender
                        for (BufferedWriter userOut : clientWriters.get((long) userIdInChat)) {
                            userOut.write("start: ADD_NEW_MESSAGE\r\n");
                            userOut.write("length: 1\r\n");
                            newMsg.sendObject(userOut);
                            userOut.write("end: ADD_NEW_MESSAGE\r\n");
                            userOut.flush();

                            System.out.println("Sent new message to user ID: " + userIdInChat);
                            System.out.println("BufferedWriter userOut: " + userOut);
                        }
                    }
                }

                for (Long userIdInClientWriters : clientWriters.keySet()) {
                    System.out.println("User ID in clientWriters: " + userIdInClientWriters);
                }

                logger.info("Sent new message with ID: " + newMsg.getId() + " for chat ID: " + chatId);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error sending new message for chat ID: " + chatId, e);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void saveMessageToDatabase(Message message) {
        final String SQL =
                "INSERT INTO messages " +
                        "(chat_id, sender_id, content, message_type, file_url, media_url, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        System.out.println("chatId: " + message.getChatId());
        System.out.println("senderId: " + message.getSenderId());
        System.out.println("content: " + message.getContent());
        System.out.println("fileUrl: " + message.getFileUrl());
        System.out.println("mediaUrl: " + message.getMediaUrl());

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {
            // Validate required fields
            if (message.getChatId() <= 0 || message.getSenderId() <= 0) {
                throw new SQLException("Invalid message data: chat_id and sender_id are required");
            }
            stmt.setInt(1, message.getChatId());
            stmt.setInt(2, message.getSenderId());
            stmt.setString(3, message.getContent());
            stmt.setString(4, (message.getMessageType() == null ? "TEXT" : message.getMessageType().toUpperCase()));
            stmt.setString(5, message.getFileUrl());
            stmt.setString(6, message.getMediaUrl());
            stmt.setTimestamp(7, Timestamp.valueOf(message.getCreatedAt()));
            stmt.setTimestamp(8, Timestamp.valueOf(message.getUpdatedAt()));
            int rowsAffected = stmt.executeUpdate();
            logger.info(rowsAffected > 0
                    ? "Message stored in DB (chatId=" + message.getChatId() + ")"
                    : "DB insert returned 0 rows");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error saving message to database: " + e.getMessage(), e);
        }
    }
}

