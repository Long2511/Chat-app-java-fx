package com.ouroboros.chatapp.chatapp.serverside;

import com.ouroboros.chatapp.chatapp.datatype.Message;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.text.SimpleDateFormat;


public class MessageHandler {
    public static AtomicInteger messageIdCounter = new AtomicInteger(1);
    public static final List<Message> messages = Collections.synchronizedList(new ArrayList<>());


    public static void handleRequestMessages(int chatId, BufferedWriter out) throws IOException {
        int length = 0;
        for (Message msg : messages) {
            if (msg.getChatId() == chatId) {
                length++;
            }
        }

        out.write("start: RESPONSE_MESSAGES\r\n");
        out.write("length: " + length + "\r\n");
        for (Message msg : messages) if (msg.getChatId() == chatId) {
            msg.sendObject(out);
        }
        out.write("end: RESPONSE_MESSAGES\r\n");
        out.flush();
        System.out.println("Sent " + length + " messages for chat ID: " + chatId);
    }

    public static void handleSendMessage(int chatId, int senderId, String content, BufferedWriter out) throws IOException {
        // add the new message to the list
        Message newMsg = new Message();
        newMsg.setId(messageIdCounter.getAndIncrement());
        newMsg.setChatId(chatId);
        newMsg.setSenderId(senderId);
        newMsg.setContent(content);
        newMsg.setCreatedAt(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date()));
        newMsg.setUpdatedAt(newMsg.getCreatedAt());
        messages.add(newMsg);

        // send the new message back to the clients
        out.write("start: ADD_NEW_MESSAGE\r\n");
        out.write("length: 1\r\n");
        newMsg.sendObject(out);
        out.write("end: ADD_NEW_MESSAGE\r\n");
        out.flush();

        System.out.println("Sent new message with ID: " + newMsg.getId() + " for chat ID: " + chatId);
    }
}