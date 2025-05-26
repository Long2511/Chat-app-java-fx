package com.ouroboros.chatapp.chatapp.serverside;

import com.ouroboros.chatapp.chatapp.datatype.Message;
import com.ouroboros.chatapp.chatapp.serverside.DatabaseUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageHandler {
    private static final Logger logger = Logger.getLogger(MessageHandler.class.getName());
    public static final List<Message> messages = Collections.synchronizedList(new ArrayList<>());
    public static final AtomicInteger messageIdCounter = new AtomicInteger(1);

    public static void handleRequestMessages(int chatId, BufferedWriter out) {
        synchronized (messages) {
            try {
                long length = messages.stream().filter(m -> m.getChatId() == chatId).count();

                out.write("start: RESPONSE_MESSAGES\r\n");
                out.write("length: " + length + "\r\n");
                for (Message msg : messages) {
                    if (msg.getChatId() == chatId) msg.sendObject(out);
                }

                out.write("start: RESPONSE_MESSAGES\r\n");
                out.write("length: " + length + "\r\n");
                for (Message msg : messages) {
                    if (msg.getChatId() == chatId) {
                        msg.sendObject(out);
                    }
                }
                out.write("end: RESPONSE_MESSAGES\r\n");
                out.flush();
                logger.info("Sent " + length + " messages for chat ID: " + chatId);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error sending messages for chat ID: " + chatId, e);
            }
        }
    }

    public static void handleSendMessage(int chatId, int senderId, String content, BufferedWriter out) {
        synchronized (messages) {
            try {
                // Create a new message object
                Message newMsg = new Message();
                newMsg.setId(messageIdCounter.getAndIncrement());
                newMsg.setChatId(chatId);
                newMsg.setSenderId(senderId);
                newMsg.setContent(content);
                newMsg.setMessageType("TEXT");

                LocalDateTime now = LocalDateTime.now();
                newMsg.setCreatedAt(now);
                newMsg.setUpdatedAt(now);

//                saveMessageToDatabase(newMsg);
                // Add the new message to the list
                messages.add(newMsg);

                System.out.println("Hello, sent back to client: " + newMsg.getContent());

                // Store the message in the database
                out.write("start: ADD_NEW_MESSAGE\r\n");
                out.write("length: 1\r\n");
                newMsg.sendObject(out);
                out.write("end: ADD_NEW_MESSAGE\r\n");
                out.flush();

                logger.info("Sent new message with ID: " + newMsg.getId() + " for chat ID: " + chatId);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error sending new message for chat ID: " + chatId, e);
            }
        }
    }

    public static void saveMessageToDatabase(Message message) {
        final String SQL =
                "INSERT INTO messages " +
                        "(chat_id, sender_id, content, message_type, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {
            // Validate required fields
            if (message.getChatId() <= 0 || message.getSenderId() <= 0 || message.getContent() == null || message.getContent().trim().isEmpty()) {
                throw new SQLException("Invalid message data: chat_id, sender_id, and content are required");
            }

            stmt.setInt(1, message.getChatId());
            stmt.setInt(2, message.getSenderId());
            stmt.setString(3, message.getContent());
            stmt.setString(4,
                    (message.getMessageType() == null ? "TEXT" : message.getMessageType().toUpperCase()));
            stmt.setTimestamp(5, Timestamp.valueOf(message.getCreatedAt()));
            stmt.setTimestamp(6, Timestamp.valueOf(message.getUpdatedAt()));
            
            int rowsAffected = stmt.executeUpdate();
            logger.info(rowsAffected > 0
                    ? "Message stored in DB (chatId=" + message.getChatId() + ")"
                    : "DB insert returned 0 rows");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error saving message to database: " + e.getMessage(), e);
        }
    }
}