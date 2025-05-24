package com.ouroboros.chatapp.chatapp.serverside;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Map;

public class MessageUpdater implements Runnable {
    private final Map<String, Object> msg;
    private static final long RETRY_DELAY_MS = 3000; // Delay between retry attempts

    public MessageUpdater(Map<String, Object> msg) {
        this.msg = msg;
    }

    @Override
    public void run() {
        boolean updated = false;
        while (!updated) {
            try (Connection conn = DatabaseUtils.getConnection()) {
                PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO messages (id, chat_id, sender_id, content, message_type, media_url, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
                insertStmt.setInt(1, (Integer) msg.get("id"));
                insertStmt.setInt(2, (Integer) msg.get("chatId"));
                insertStmt.setInt(3, (Integer) msg.get("senderId"));
                insertStmt.setString(4, (String) msg.get("content"));
                insertStmt.setString(5, (String) msg.get("type"));
                if (msg.get("mediaUrl") == null) {
                    insertStmt.setNull(6, java.sql.Types.VARCHAR);
                } else {
                    insertStmt.setString(6, (String) msg.get("mediaUrl"));
                }
                insertStmt.setTimestamp(7, Timestamp.valueOf((String) msg.get("createdAt")));
                insertStmt.setTimestamp(8, Timestamp.valueOf((String) msg.get("updatedAt")));

                insertStmt.executeUpdate();
                System.out.println("Inserted message into database: " + msg.get("id"));
                updated = true;
            } catch (Exception e) {
                System.err.println("Error inserting message into database: " + e.getMessage());
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
}