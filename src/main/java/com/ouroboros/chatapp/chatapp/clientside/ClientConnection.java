package com.ouroboros.chatapp.chatapp.clientside;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientConnection implements AutoCloseable {
    private Socket socket;
    private PrintWriter out;
    private Scanner in;

    public ClientConnection(String host, int port) throws IOException {
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new Scanner(socket.getInputStream());
    }

    public String sendCommand(String command) {
        out.println(command);
        return in.nextLine(); // Read response from server
    }

    @Override
    public void close() throws IOException {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}