package com.ouroboros.chatapp.chatapp.serverside;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatAPIHandler {

    // Auto-incrementing chat ID for in-memory chats
    private static int chatIdSeq = 1;

    // Thread-safe in-memory chat list (for mock/demo use)
    public static final List<Map<String, Object>> chats = Collections.synchronizedList(new ArrayList<>());

    // Date-time formatter for ISO strings
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    // Simple string-based get chats: expects command = "GET_CHATS"
    public static String handleGetChats() {
        return ApiUtils.toJsonArray(chats);
    }

    // Simple string-based create private chat: expects command = "CREATE_PRIVATE_CHAT:<otherUserId>"
    public static String handlePostPrivateChat(int otherId) {
        int currentUserId = 1; // Hardcoded current user ID (for testing/demo)
        for (Map<String, Object> chat : chats) {
            if ("private".equals(chat.get("type"))) {
                List<Integer> participants = (List<Integer>) chat.get("participants");
                if (participants.contains(currentUserId) && participants.contains(otherId)) {
                    return ApiUtils.toJson(chat);
                }
            }
        }
        int chatId = chatIdSeq++;
        String now = LocalDateTime.now().format(formatter);
        Map<String, Object> chat = new HashMap<>();
        chat.put("id", chatId);
        chat.put("type", "private");
        chat.put("participants", Arrays.asList(currentUserId, otherId));
        chat.put("name", "Private Chat");
        chat.put("createdAt", now);
        chat.put("updatedAt", now);
        chats.add(chat);
        try (Connection conn = DatabaseUtils.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO chats (id, type, name, created_at, updated_at) VALUES (?, ?, ?, now(), now())");
            stmt.setInt(1, chatId);
            stmt.setString(2, "private");
            stmt.setString(3, "Private Chat");
            stmt.executeUpdate();
            PreparedStatement partStmt = conn.prepareStatement(
                "INSERT INTO chat_participants (chat_id, user_id) VALUES (?, ?), (?, ?)");
            partStmt.setInt(1, chatId);
            partStmt.setInt(2, currentUserId);
            partStmt.setInt(3, chatId);
            partStmt.setInt(4, otherId);
            partStmt.executeUpdate();
        } catch (SQLException e) {
            return "{\"error\":\"" + e.getMessage().replace("\"", "'") + "\"}";
        }
        return ApiUtils.toJson(chat);
    }

    // Simple string-based create group chat: expects command = "CREATE_GROUP_CHAT:<name>:<jsonArrayOfUserIds>"
    public static String handlePostGroupChat(String name, List<Integer> ids) {
        int chatId = chatIdSeq++;
        String now = LocalDateTime.now().format(formatter);
        Map<String, Object> chat = new HashMap<>();
        chat.put("id", chatId);
        chat.put("type", "group");
        chat.put("participants", ids);
        chat.put("name", name);
        chat.put("createdAt", now);
        chat.put("updatedAt", now);
        chats.add(chat);
        try (Connection conn = DatabaseUtils.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO chats (id, type, name, created_at, updated_at) VALUES (?, ?, ?, now(), now())");
            stmt.setInt(1, chatId);
            stmt.setString(2, "group");
            stmt.setString(3, name);
            stmt.executeUpdate();
            PreparedStatement partStmt = conn.prepareStatement(
                "INSERT INTO chat_participants (chat_id, user_id) VALUES (?, ?)");
            for (Integer id : ids) {
                partStmt.setInt(1, chatId);
                partStmt.setInt(2, id);
                partStmt.addBatch();
            }
            partStmt.executeBatch();
        } catch (SQLException e) {
            return "{\"error\":\"" + e.getMessage().replace("\"", "'") + "\"}";
        }
        return ApiUtils.toJson(chat);
    }
}
