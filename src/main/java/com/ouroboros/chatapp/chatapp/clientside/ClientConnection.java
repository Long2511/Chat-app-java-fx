package com.ouroboros.chatapp.chatapp.clientside;

import com.ouroboros.chatapp.chatapp.datatype.Message;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientConnection {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8080;
    public static List<Message> messages = new ArrayList<>();
    private static Socket sharedSocket;
    private static BufferedReader sharedIn;
    private static BufferedWriter sharedOut;

    /**
     * Gets or initializes a shared socket connection to the server
     *
     * @return the shared socket instance
     * @throws IOException if connection fails
     */
    public static synchronized Socket getSharedSocket() throws IOException {
        if (sharedSocket == null || sharedSocket.isClosed()) {
            sharedSocket = new Socket(SERVER_HOST, SERVER_PORT);
            sharedIn = new BufferedReader(new InputStreamReader(sharedSocket.getInputStream()));
            sharedOut = new BufferedWriter(new OutputStreamWriter(sharedSocket.getOutputStream()));
            System.out.println("Created new connection to " + SERVER_HOST + ":" + SERVER_PORT);
        }
        return sharedSocket;
    }

    /**
     * Gets the shared BufferedReader for the connection
     *
     * @return the shared BufferedReader
     * @throws IOException if connection is not established
     */
    public static synchronized BufferedReader getSharedReader() throws IOException {
        getSharedSocket(); // Ensure connection is established
        return sharedIn;
    }

    /**
     * Gets the shared BufferedWriter for the connection
     *
     * @return the shared BufferedWriter
     * @throws IOException if connection is not established
     */
    public static synchronized BufferedWriter getSharedWriter() throws IOException {
        getSharedSocket(); // Ensure connection is established
        return sharedOut;
    }

    /**
     * Closes the shared socket connection
     */
    public static synchronized void closeConnection() {
        try {
            if (sharedSocket != null && !sharedSocket.isClosed()) {
                sharedSocket.close();
                sharedSocket = null;
                sharedIn = null;
                sharedOut = null;
                System.out.println("Closed connection to server");
            }
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws IOException {
        try {
            // Use shared socket instead of creating a new one
            Socket socket = getSharedSocket();
            BufferedReader in = getSharedReader();
            BufferedWriter out = getSharedWriter();

            // create request all messages
            sendRequestMessages(1, out);

            // read response from server
            receiveMessages(in);

            // display messages
            displayMessages();

            // create request sent message
            sendRequestSentMessage(1, 2, "HIhihi ehehe!!", out);

            // read response from server
            receiveNewMessage(in);

            // display messages
            displayMessages();

        } catch (IOException e) {
            System.out.println("Error: " + e);
        } finally {
            closeConnection();
        }
    }

    private static void sendRequestMessages(int chatId, BufferedWriter out) throws IOException {
        out.write("start: GET_MESSAGES\r\n");
        out.write("chatId: " + chatId + "\r\n");
        out.write("end: GET_MESSAGES\r\n");
        out.flush();
    }

    private static void sendRequestSentMessage(int chatId, int senderId, String content, BufferedWriter out) throws IOException {
        out.write("start: SEND_MESSAGE\r\n");
        out.write("chatId: " + chatId + "\r\n");
        out.write("senderId: " + senderId + "\r\n");
        out.write("content: " + content + "\r\n");
        out.write("end: SEND_MESSAGE\r\n");
        out.flush();
    }

    private static void receiveMessages(BufferedReader in) throws IOException {
        String line;
        messages = new ArrayList<>();
        while (!(line = in.readLine()).equals("end: RESPONSE_MESSAGES")) {
            if (line.startsWith("length: ")) {
                int length = Integer.parseInt(line.substring("length: ".length()));
                for (int i = 0; i < length; i++) {
                    messages.add(Message.receiveObject(in));
                }
            }
        }
    }

    private static void receiveNewMessage(BufferedReader in) throws IOException {
        String line;
        while (!(line = in.readLine()).equals("end: ADD_NEW_MESSAGE")) {
            if (line.startsWith("length: ")) {
                int length = Integer.parseInt(line.substring("length: ".length()));
                for (int i = 0; i < length; i++) {
                    messages.add(Message.receiveObject(in));
                }
            }
        }
    }

    private static void displayMessages() {
        for (Message message : messages) {
            message.printMessage();
        }
    }

}

