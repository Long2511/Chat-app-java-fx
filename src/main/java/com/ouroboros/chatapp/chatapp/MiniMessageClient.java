package com.ouroboros.chatapp.chatapp;

import com.ouroboros.chatapp.chatapp.datatype.Message;
import com.ouroboros.chatapp.chatapp.serverside.MessageHandler;

import java.time.LocalDateTime;          // import LocalDateTime

public class MiniMessageClient {
    public static void main(String[] args) {
        Message msg = new Message();
        msg.setChatId(1);
        msg.setSenderId(83);
        msg.setContent("Hello from mini-client!");
        msg.setMessageType("text");

        LocalDateTime now = LocalDateTime.now();   // chỉ khai báo 1 lần
        msg.setCreatedAt(now);
        msg.setUpdatedAt(now);

        MessageHandler.saveMessageToDatabase(msg);

        System.out.println("Inserted – kiểm tra bảng messages xem đã có bản ghi!");
        com.ouroboros.chatapp.chatapp.serverside.DatabaseUtils.closeConnection();
    }
}
