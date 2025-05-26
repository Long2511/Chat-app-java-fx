package com.ouroboros.chatapp.chatapp;

import com.ouroboros.chatapp.chatapp.datatype.Message;
import com.ouroboros.chatapp.chatapp.serverside.DatabaseUtils;
import com.ouroboros.chatapp.chatapp.serverside.EncryptionUtil;
import com.ouroboros.chatapp.chatapp.serverside.MessageHandler;

import java.time.LocalDateTime;          // import LocalDateTime

public class MiniMessageClient {
    public static void main(String[] args) throws Exception {
        int testChatId = 1;
        int testSenderId = 83;

        Message msg = new Message();

        String originalMessage = "Hello, this is a secret message!";
        String encryptedMessage = EncryptionUtil.encrypt(originalMessage,testChatId); // Giả lập mã hóa
        msg.setChatId(testChatId);
        msg.setSenderId(testSenderId);
        msg.setMessageType("TEXT");
        msg.setContent(encryptedMessage);
        msg.setCreatedAt(LocalDateTime.now());
        msg.setUpdatedAt(msg.getCreatedAt());

        System.out.println("Original Message: " + originalMessage);
        System.out.println("Encrypted Message: " + encryptedMessage);
        // Lưu thử vào DB
        try {
            MessageHandler.saveMessageToDatabase(msg);
            System.out.println("Inserted message into DB: " + msg.getContent());

            String decryptedMessage = EncryptionUtil.decrypt(msg.getContent(), testChatId);
            System.out.println("Decrypted Message: " + decryptedMessage);
        } catch (Exception e) {
            System.err.println("Failed to insert message: " + e.getMessage());
            e.printStackTrace();


        } finally {
            DatabaseUtils.closeConnection();
        }




    }
}
