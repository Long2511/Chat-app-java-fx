package com.ouroboros.chatapp.chatapp;

import com.ouroboros.chatapp.chatapp.datatype.Message;
import com.ouroboros.chatapp.chatapp.serverside.DatabaseUtils;
import com.ouroboros.chatapp.chatapp.serverside.MessageHandler;

import java.time.LocalDateTime;          // import LocalDateTime

public class MiniMessageClient {
    public static void main(String[] args) {
        int testChatId = 1;
        int testSenderId = 83;

        Message msg = new Message();
        msg.setChatId(testChatId);
        msg.setSenderId(testSenderId);
        msg.setMessageType(Message.TYPE_FILE);
        msg.setContent("uploads/test-file.pdf");
        msg.setCreatedAt(LocalDateTime.now());
        msg.setUpdatedAt(msg.getCreatedAt());

        // Lưu thử vào DB
        try {
            MessageHandler.saveMessageToDatabase(msg);
            System.out.println("Inserted message into DB: " + msg.getContent());
        } catch (Exception e) {
            System.err.println("Failed to insert message: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtils.closeConnection();
        }
    }
}
