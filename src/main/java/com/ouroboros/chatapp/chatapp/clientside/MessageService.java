package com.ouroboros.chatapp.chatapp.clientside;

import com.ouroboros.chatapp.chatapp.datatype.Message;
import com.ouroboros.chatapp.chatapp.serverside.EncryptionUtil;

import java.io.*;
import java.net.Socket;
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
    public synchronized void requestMessages(int chatId) throws Exception {
        out.write("start: GET_MESSAGES\r\n");
        out.write("chatId: " + chatId + "\r\n");
        out.write("end: GET_MESSAGES\r\n");
        out.flush();

        receiveMessages();
    }

    /**
     * Sends a new message to the server
     *
     * @param chatId   the chat ID
     * @param senderId the sender ID
     * @param content  the message content
     * @throws IOException if communication fails
     */
    public synchronized void sendMessage(int chatId, int senderId, String content) throws Exception {
        // Encrypt the content before sending
        String encryptedContent = EncryptionUtil.encrypt(content, chatId);
        out.write("start: SEND_MESSAGE\r\n");
        out.write("chatId: " + chatId + "\r\n");
        out.write("senderId: " + senderId + "\r\n");
        out.write("content: " + encryptedContent + "\r\n");
        out.write("end: SEND_MESSAGE\r\n");
        out.flush();

//        receiveNewMessage();
    }

    // Send icon and file (and optionally text, but only encrypt text)
    public synchronized void sendMessage(Message m) throws Exception {
        String contentToSend = m.getContent();
        // Only encrypt if not a file message
        if (!m.isFile()) {
            contentToSend = EncryptionUtil.encrypt(contentToSend, m.getChatId());
        }
        out.write("start: SEND_MESSAGE\r\n");
        out.write("chatId: "      + m.getChatId()      + "\r\n");
        out.write("senderId: "    + m.getSenderId()    + "\r\n");
        out.write("messageType: " + m.getMessageType() + "\r\n");
        out.write("content: "     + contentToSend     + "\r\n");
        out.write("end: SEND_MESSAGE\r\n");
        out.flush();

        receiveNewMessage();
    }

    /**
     * Dedicated method for sending file messages. Do not use sendMessage for files!
     * This method assumes the file is already uploaded to the server (or local uploads/).
     * The message content should be the file name, and fileUrl should be the path.
     * Adds debug output for troubleshooting.
     */
    public synchronized void sendFile(Message m, File file) throws Exception {
        System.out.println("[DEBUG] sendFile called");
        System.out.println("[DEBUG] Message: chatId=" + m.getChatId() + ", senderId=" + m.getSenderId() + ", fileUrl=" + m.getFileUrl() + ", content=" + m.getContent() + ", type=" + m.getMessageType());
        System.out.println("[DEBUG] File: " + (file != null ? file.getAbsolutePath() : "null"));
        if (!m.isFile()) {
            throw new IllegalArgumentException("sendFile can only be used for file messages (type FILE)");
        }
        // Optionally: upload file to server here if needed (currently assumed done in controller)
        out.write("start: SEND_MESSAGE\r\n");
        out.write("chatId: "      + m.getChatId()      + "\r\n");
        out.write("senderId: "    + m.getSenderId()    + "\r\n");
        out.write("messageType: " + m.getMessageType() + "\r\n");
        out.write("content: "     + m.getContent()     + "\r\n"); // file name or description
        out.write("fileUrl: "     + (m.getFileUrl() != null ? m.getFileUrl() : "") + "\r\n");
        out.write("mediaUrl: "    + (m.getMediaUrl() != null ? m.getMediaUrl() : "") + "\r\n");
        out.write("end: SEND_MESSAGE\r\n");
        out.flush();
        System.out.println("[DEBUG] sendFile message sent to server");
        receiveNewMessage();
        System.out.println("[DEBUG] sendFile receiveNewMessage completed");
    }

    /**
     * Receives messages from the server response
     *
     * @throws IOException if communication fails
     */
    private void receiveMessages() throws Exception {
        String line;
        messages.clear();
        while (!(line = in.readLine()).equals("end: RESPONSE_MESSAGES")) {
            if (line.startsWith("length: ")) {
                int length = Integer.parseInt(line.substring("length: ".length()));
                for (int i = 0; i < length; i++) {
                    Message encryptedMessage = Message.receiveObject(in);
                    // Only decrypt if not a file message
                    if (!encryptedMessage.isFile()) {
                        String decryptedContent = EncryptionUtil.decrypt(encryptedMessage.getContent(), encryptedMessage.getChatId());
                        encryptedMessage.setContent(decryptedContent);
                    }
                    messages.add(encryptedMessage);
                }
            }
        }
    }

    /**
     * Receives a new message from the server response
     *
     * @throws IOException if communication fails
     */
    public void receiveNewMessage() throws Exception {
        String line;
        while (!(line = in.readLine()).equals("end: ADD_NEW_MESSAGE")) {
            if (line.startsWith("length: ")) {
                int length = Integer.parseInt(line.substring("length: ".length()));
                for (int i = 0; i < length; i++) {
                    Message encryptedMessage = Message.receiveObject(in);
                    // Only decrypt if not a file message
                    if (!encryptedMessage.isFile()) {
                        String decryptedContent = EncryptionUtil.decrypt(encryptedMessage.getContent(), encryptedMessage.getChatId());
                        encryptedMessage.setContent(decryptedContent);
                    }
                    messages.add(encryptedMessage);
                }
            }
        }
    }

    /**
     * Checks if there's a new message marker in the input stream without blocking
     * @return The line containing the marker, or null if no data is available
     */
    public synchronized String checkForNewMessageMarker() {
        try {
            // Check if there's data available to read without blocking
            if (ClientConnection.getSharedSocket().getInputStream().available() > 0) {
                // Mark the current position in case we need to reset
                in.mark(1024);

                // Try to read a line
                String line = in.readLine();

                if (line != null && line.equals("start: ADD_NEW_MESSAGE")) {
                    // Found the marker
                    return line;
                } else {
                    // Not what we're looking for, reset the stream position
                    in.reset();
                }
            }
        } catch (IOException e) {
            System.err.println("Error checking for new message marker: " + e.getMessage());
        }

        // No marker found
        return null;
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
