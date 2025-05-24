package com.ouroboros.chatapp.chatapp.serverside;

import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.sql.*;
import java.util.Date;

public class MessagesApiHandler {
    public static final List<Map<String, Object>> messages = Collections.synchronizedList(new ArrayList<>());
    public static int messageIdCounter = 1;

    public static void fetchDataFromDatabase() {
        // Whenever the server starts, fetch all messages from the database and continue from there
        try (Connection conn = DatabaseUtils.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, chat_id, sender_id, content, message_type, media_url, created_at, updated_at FROM messages ORDER BY id ASC")) {

            int maxId = 0;
            while (rs.next()) {
                Map<String, Object> msg = new HashMap<>();
                int id = rs.getInt("id");
                msg.put("id", id);
                msg.put("chatId", rs.getInt("chat_id"));
                msg.put("senderId", rs.getInt("sender_id"));
                msg.put("content", rs.getString("content"));
                msg.put("type", rs.getString("message_type"));
                msg.put("mediaUrl", rs.getString("media_url"));
                // timestamp does not exist in the database & doesnot have any purpose right now
                msg.put("timestamp", rs.getString("created_at"));
                msg.put("createdAt", rs.getString("created_at"));
                msg.put("updatedAt", rs.getString("updated_at"));
                messages.add(msg);
                if (id > maxId) {
                    maxId = id;
                }
            }
            messageIdCounter = maxId + 1;
            System.out.println("Fetched " + messages.size() + " messages from database.");
        } catch (Exception e) {
            System.err.println("Error fetching messages from database: " + e.getMessage());
        }
    }

    // Simple string-based get messages: expects command = "GET_MESSAGES:<chatId>"
    public static String handleGetMessages(int chatId) {
        StringBuilder sb = new StringBuilder("[");
        boolean isFirstMsg = true;
        for (Map<String, Object> msg : messages) {
            if (((Integer) msg.get("chatId")).equals(chatId)) {
                if (!isFirstMsg) sb.append(",");
                sb.append(ApiUtils.toJson(msg));
                isFirstMsg = false;
            }
        }
        sb.append("]");
        return sb.toString();
    }

    // Simple string-based post message: expects command = "SEND_MSG:{json}"
    public static String handlePostMessage(String json) {
        int chatId, senderId;
        try {
            chatId = Integer.parseInt(ApiUtils.extractJsonField(json, "chatId"));
            senderId = Integer.parseInt(ApiUtils.extractJsonField(json, "senderId"));
        } catch (Exception e) {
            return "{\"error\":\"Invalid chatId or senderId\"}";
        }
        String content = ApiUtils.extractJsonField(json, "content");
        String type = ApiUtils.extractJsonField(json, "type");
        String mediaUrl = ApiUtils.extractJsonField(json, "mediaUrl");
        int msgId = messageIdCounter++;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date createdTime = new Date();
        Map<String, Object> msg = new HashMap<>();
        msg.put("id", msgId);
        msg.put("chatId", chatId);
        msg.put("senderId", senderId);
        msg.put("content", content);
        msg.put("type", type != null ? type : "TEXT");
        msg.put("mediaUrl", mediaUrl);
        msg.put("timestamp", dateFormat.format(createdTime));
        msg.put("createdAt", dateFormat.format(createdTime));
        msg.put("updatedAt", dateFormat.format(createdTime));
        messages.add(msg);
        // Optionally, store to DB in background
        new Thread(new MessageUpdater(msg)).start();
        return ApiUtils.toJson(msg);
    }
}

