package com.ouroboros.chatapp.chatapp.clientside;

import com.ouroboros.chatapp.chatapp.datatype.Message;
import com.ouroboros.chatapp.chatapp.serverside.DatabaseUtils;
import com.ouroboros.chatapp.chatapp.serverside.EncryptionUtil;

import java.io.*;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class that handles message-related communication with the chat server
 * using the shared socket connection from ClientConnection
 */
public class MessageService {
    private final List<Message> messages = new ArrayList<>();
    private BufferedReader in;
    private BufferedWriter out;

    /**
     * Creates a new MessageService using the existing socket from ClientConnection
     */
    public MessageService() {
        try {
            Socket socket = ClientConnection.getSharedSocket();
            if (socket == null || socket.isClosed()) {
                throw new IOException("No active socket connection available");
            }
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            System.err.println("Error initializing MessageService: " + e.getMessage());
        }
    }

    /**
     * Requests all messages for a specific chat
     *
     * @param chatId the chat ID to get messages for
     * @throws IOException if communication fails
     */
    public synchronized void requestMessages(int chatId) throws IOException {
        out.write("start: GET_MESSAGES\r\n");
        out.write("chatId: " + chatId + "\r\n");
        out.write("end: GET_MESSAGES\r\n");
        out.flush();

        receiveMessages(chatId);
    }

    /**
     * Sends a new message to the server
     *
     * @param chatId   the chat ID
     * @param senderId the sender ID
     * @param content  the message content
     * @throws IOException if communication fails
     */
    public synchronized void sendMessage(int chatId, int senderId, String content) throws IOException {
        try {
            // Encrypt the message using chat-specific key
            String encryptedContent = EncryptionUtil.encrypt(content, chatId);

            out.write("start: SEND_MESSAGE\r\n");
            out.write("chatId: " + chatId + "\r\n");
            out.write("senderId: " + senderId + "\r\n");
            out.write("content: " + encryptedContent + "\r\n");
            out.write("end: SEND_MESSAGE\r\n");
            out.flush();
        } catch (Exception e) {
            System.err.println("Error encrypting message: " + e.getMessage());
            throw new IOException("Failed to encrypt message", e);
        }

        receiveNewMessage(chatId);
    }

    /**
     * Receives messages from the server response
     *
     * @param chatId the chat ID to get messages for
     * @throws IOException if communication fails
     */
    private void receiveMessages(int chatId) throws IOException {
        String line;
        messages.clear();
        while (!(line = in.readLine()).equals("end: RESPONSE_MESSAGES")) {
            if (line.startsWith("length: ")) {
                int length = Integer.parseInt(line.substring("length: ".length()));
                for (int i = 0; i < length; i++) {
                    Message message = Message.receiveObject(in);
                    try {
                        // Decrypt the message using chat-specific key
                        String decryptedContent = EncryptionUtil.decrypt(message.getContent(), chatId);
                        message.setContent(decryptedContent);
                    } catch (Exception e) {
                        System.err.println("Error decrypting message: " + e.getMessage());
                    }
                    messages.add(message);
                }
            }
        }
    }

    /**
     * Receives a new message from the server response
     *
     * @param chatId the chat ID to get messages for
     * @throws IOException if communication fails
     */
    private void receiveNewMessage(int chatId) throws IOException {
        String line;
        while (!(line = in.readLine()).equals("end: ADD_NEW_MESSAGE")) {
            if (line.startsWith("length: ")) {
                int length = Integer.parseInt(line.substring("length: ".length()));
                for (int i = 0; i < length; i++) {
                    Message message = Message.receiveObject(in);
                    try {
                        // Decrypt the message using chat-specific key
                        String decryptedContent = EncryptionUtil.decrypt(message.getContent(), chatId);
                        message.setContent(decryptedContent);
                    } catch (Exception e) {
                        System.err.println("Error decrypting message: " + e.getMessage());
                    }
                    messages.add(message);
                }
            }
        }
    }

    /**
     * Returns the list of messages
     *
     * @return list of messages
     */
    public synchronized List<Message> getMessages() {
        return new ArrayList<>(messages);
    }
}
