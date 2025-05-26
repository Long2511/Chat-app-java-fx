package com.ouroboros.chatapp.chatapp;

import com.ouroboros.chatapp.chatapp.datatype.Message;
import com.ouroboros.chatapp.chatapp.datatype.User;
import com.ouroboros.chatapp.chatapp.clientside.MessageService;
import com.ouroboros.chatapp.chatapp.clientside.ClientConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.TextArea;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
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

    // Thread for message updates
    private Thread messageListenerThread;
    private final AtomicBoolean isUpdateRunning = new AtomicBoolean(false);

    // Latch to ensure initial message load completes before starting the update thread
    private CountDownLatch initialLoadLatch;

    @FXML
    public void initialize() {
        // Initialize any necessary setup
        messageScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        messageScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        // Set up message input handling
        messageInput.setOnAction(event -> handleSendMessage());
        sendButton.setOnAction(event -> handleSendMessage());
        backButton.setOnAction(event -> handleBackButton());

        // Initialize message service
        messageService = new MessageService();

        // Set up scene change listener for cleanup
        messageContainer.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene == null && oldScene != null) {
                // Scene changed - stop update thread
                stopMessageListener();
            }
        });

        // Set up listener for window close events to stop the thread
        Platform.runLater(() -> {
            if (messageContainer.getScene() != null) {
                Stage stage = (Stage) messageContainer.getScene().getWindow();
                stage.setOnCloseRequest(event -> stopMessageListener());
            }
        });
    }

    public void loadChatMessages(int chatId) {
        this.chatId = chatId;

        // Initialize the latch for synchronization
        initialLoadLatch = new CountDownLatch(1);

        // Load messages in a background thread
        CompletableFuture.runAsync(() -> {
            try {
                // Request messages from the server
                messageService.requestMessages(chatId);
                List<Message> messages = messageService.getMessages();

                // Update UI on JavaFX thread
                Platform.runLater(() -> {
                    messageContainer.getChildren().clear();
                    for (Message message : messages) {
                        boolean isFromCurrentUser = message.getSenderId() == currentUser.getId();
                        addMessage(message.getContent(), isFromCurrentUser);
                    }

                    // Scroll to bottom
                    messageScroll.setVvalue(1.0);
                    System.out.println("Initial messages loaded for chat ID: " + chatId);
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Failed to load messages");
                    alert.setContentText("Could not load messages from the server: " + e.getMessage());
                    alert.showAndWait();
                });
                System.err.println("Error loading initial messages: " + e.getMessage());
                e.printStackTrace();
            } finally {
                // Signal that initial load is complete
                initialLoadLatch.countDown();

                // Start the update thread now that initial load is complete
                startMessageListener();
            }
        });
    }

    private void startMessageListener() {
        // Don't start if already running
        if (isUpdateRunning.get()) return;

        isUpdateRunning.set(true);

        messageListenerThread = new Thread(() -> {
            try {
                // Wait for initial load to complete before starting
                initialLoadLatch.await();

                System.out.println("Message listener thread started for chat ID: " + chatId);
                Socket socket = ClientConnection.getSharedSocket();
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                while (isUpdateRunning.get() && !Thread.currentThread().isInterrupted()) {
                    try {
                        // Check for new messages
                        String line = reader.readLine();

                        if (line != null && line.equals("start: ADD_NEW_MESSAGE")) {
                            Message newMessage = null;

                            // Process the message marker
                            while (!(line = reader.readLine()).equals("end: ADD_NEW_MESSAGE")) {
                                if (line.startsWith("length: ")) {
                                    int length = Integer.parseInt(line.substring("length: ".length()));
                                    for (int i = 0; i < length && i == 0; i++) {
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
                                    // Scroll to bottom
                                    messageScroll.setVvalue(1.0);
                                });
                            }
                        }

                        // Sleep briefly to avoid excessive CPU usage
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        // Thread was interrupted, exit the loop
                        break;
                    } catch (Exception e) {
                        if (isUpdateRunning.get()) {
                            System.err.println("Error in message listener: " + e.getMessage());
                            e.printStackTrace();
                            // Sleep longer after an error
                            Thread.sleep(1000);
                        } else {
                            break;
                        }
                    }
                }
            } catch (InterruptedException e) {
                // Thread was interrupted while waiting for the latch
                System.out.println("Message listener thread interrupted while waiting for initial load");
            } catch (IOException e) {
                System.err.println("Socket error in message listener: " + e.getMessage());
                e.printStackTrace();
            } finally {
                System.out.println("Message listener thread stopped for chat ID: " + chatId);
            }
        });

        messageListenerThread.setDaemon(true);
        messageListenerThread.start();
    }

    private void stopMessageListener() {
        isUpdateRunning.set(false);

        if (messageListenerThread != null && messageListenerThread.isAlive()) {
            messageListenerThread.interrupt();

            try {
                // Wait briefly for thread to terminate
                messageListenerThread.join(500);
            } catch (InterruptedException e) {
                System.err.println("Interrupted while waiting for message listener thread to stop");
            }

            System.out.println("Message listener thread stopped for chat ID: " + chatId);
            messageListenerThread = null;
        }
    }

    @FXML
    private void handleSendMessage() {
        String message = messageInput.getText().trim();
        if (!message.isEmpty()) {
            // Clear the message field immediately for better user experience
            String messageToBeSent = message;
            messageInput.clear();

            // Send message in a background thread
            CompletableFuture.runAsync(() -> {
                try {
                    messageService.sendMessage(chatId, (int) currentUser.getId(), messageToBeSent);
                } catch (IOException e) {
                    Platform.runLater(() -> {
                        Stage stage = (Stage) messageContainer.getScene().getWindow();
                        Toast.show(stage, "Failed to send message: " + e.getMessage(), 4000);
                    });
                    System.err.println("Error sending message: " + e.getMessage());
                    e.printStackTrace();
                }
            });
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

    /**
     * Public cleanup method to be called when view is closed
     */
    public void cleanup() {
        stopMessageListener();
    }
}
