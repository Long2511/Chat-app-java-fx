package com.ouroboros.chatapp.chatapp;

import com.ouroboros.chatapp.chatapp.datatype.Message;
import com.ouroboros.chatapp.chatapp.datatype.User;
import com.ouroboros.chatapp.chatapp.clientside.MessageService;
import com.ouroboros.chatapp.chatapp.clientside.ClientConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.TextArea;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.ouroboros.chatapp.chatapp.clientside.Toast;

public class ChatViewController {
    @FXML
    private HBox topBar;
    
    @FXML
    private Rectangle avatarRect;
    
    @FXML
    private Label chatTitle;
    
    @FXML
    private ScrollPane messageScroll;
    
    @FXML
    private VBox messageContainer;
    
    @FXML
    private HBox inputBar;
    
    @FXML
    private TextField messageInput;
    
    @FXML
    private Button sendButton;

    @FXML
    private Button backButton;

    private User currentUser;
    private int chatId;
    private MessageService messageService;
    private Thread messageListenerThread;
    private final AtomicBoolean running = new AtomicBoolean(false);

    @FXML
    public void initialize() {
        // Initialize any necessary setup
        messageScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        messageScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        // Set up message input handling
        messageInput.setOnAction(event -> handleSendMessage());
        sendButton.setOnAction(event -> handleSendMessage());

        // Initialize message service
        messageService = new MessageService();

        // Set up listener for window close events to stop the thread
        Platform.runLater(() -> {
            Stage stage = (Stage) messageContainer.getScene().getWindow();
            stage.setOnCloseRequest(event -> stopMessageListener());
        });
    }

    public void loadChatMessages(int chatId) {
        this.chatId = chatId;

        try {
            // Load past messages from server
            messageService.requestMessages(chatId);
            List<Message> messages = messageService.getMessages();

            // Display past messages
            Platform.runLater(() -> {
                messageContainer.getChildren().clear();
                for (Message message : messages) {
                    boolean isFromCurrentUser = message.getSenderId() == currentUser.getId();
                    addMessage(message.getContent(), isFromCurrentUser);
                }

                // After loading past messages, start listening for new ones
                startMessageListener();
            });
        } catch (IOException e) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Failed to load messages");
                alert.setContentText("Could not load messages from the server: " + e.getMessage());
                alert.showAndWait();
            });
        }
    }

    private void startMessageListener() {
        // Ensure any existing thread is stopped
        stopMessageListener();

        // Start new listener thread
        running.set(true);
        messageListenerThread = new Thread(() -> {
            try {
                Socket socket = ClientConnection.getSharedSocket();
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String line;
                while (running.get() && !Thread.currentThread().isInterrupted()) {
                    line = reader.readLine();
                    if (line != null && line.equals("start: ADD_NEW_MESSAGE")) {
                        // New message notification received, check if it's for our chat
                        Message newMessage = null;
                        while (!(line = reader.readLine()).equals("end: ADD_NEW_MESSAGE")) {
                            if (line.startsWith("length: ")) {
                                int length = Integer.parseInt(line.substring("length: ".length()));
                                for (int i = 0; i < length && i == 0; i++) { // Just get the first message
                                    newMessage = Message.receiveObject(reader);
                                }
                            }
                        }

                        // If the message is for our chat, display it
                        if (newMessage != null && newMessage.getChatId() == chatId) {
                            final Message displayMessage = newMessage;
                            Platform.runLater(() -> {
                                boolean isFromCurrentUser = displayMessage.getSenderId() == currentUser.getId();
                                addMessage(displayMessage.getContent(), isFromCurrentUser);
                            });
                        }
                    }
                }
            } catch (IOException e) {
                if (running.get()) {
                    System.err.println("Error in message listener: " + e.getMessage());
                }
            }
        });

        messageListenerThread.setDaemon(true);
        messageListenerThread.start();
    }

    private void stopMessageListener() {
        running.set(false);
        if (messageListenerThread != null) {
            messageListenerThread.interrupt();
            messageListenerThread = null;
        }
    }

    @FXML
    private void handleSendMessage() {
        String message = messageInput.getText().trim();
        if (!message.isEmpty()) {
            try {
                // Send message to server
                messageService.sendMessage(chatId, (int) currentUser.getId(), message);

                System.out.println("Message sent: " + message);

                // Clear input field
                messageInput.clear();
            } catch (IOException e) {
                Stage stage = (Stage) messageContainer.getScene().getWindow();
                Toast.show(stage, "Failed to send message: " + e.getMessage(), 4000);
            }
        }
    }

    public void addMessage(String message, boolean isFromCurrentUser) {
        TextArea messageArea = new TextArea(message);
        messageArea.setWrapText(true);
        messageArea.setEditable(false);
        messageArea.setPrefRowCount(1);
        messageArea.setMaxWidth(400);
        
        // Add CSS classes
        messageArea.getStyleClass().add("message-area");
        messageArea.getStyleClass().add(isFromCurrentUser ? "current-user-message" : "other-user-message");

        HBox messageBox = new HBox(messageArea);
        messageBox.getStyleClass().add("message-box");
        messageBox.setAlignment(isFromCurrentUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        messageBox.setMaxWidth(messageScroll.getWidth());
        
        messageContainer.getChildren().add(messageBox);
        messageContainer.getStyleClass().add("message-container");
        
        // Scroll to bottom
        messageScroll.setVvalue(1.0);
    }

    public void setChatTitle(String username) {
        chatTitle.setText(username);
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
        // TODO: pass chatId to load messages when setting current user
        // using chatId = 1 for testing
        setChatId(1);
    }

    public void setChatId(int chatId) {
        this.chatId = chatId;
        // Load messages when chat ID is set
        loadChatMessages(chatId);
    }

    public void setAvatarColor(String color) {
        avatarRect.setFill(Color.web(color));
    }

    @FXML
    private void handleBackButton() {
        // Stop message listener thread before going back
        stopMessageListener();

        try {
            SceneChanger.changeScene("/com/ouroboros/chatapp/chatapp/View/Homepage.fxml");
        } catch (IOException e) {
            Stage stage = (Stage) messageContainer.getScene().getWindow();
            Toast.show(stage, "Cannot go back", 4000);
        }
    }
}

