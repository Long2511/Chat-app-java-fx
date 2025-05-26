package com.ouroboros.chatapp.chatapp;

import com.ouroboros.chatapp.chatapp.datatype.Message;
import com.ouroboros.chatapp.chatapp.datatype.User;
import com.ouroboros.chatapp.chatapp.clientside.MessageService;
import com.ouroboros.chatapp.chatapp.clientside.ClientConnection;
import com.ouroboros.chatapp.chatapp.clientside.MessageService;
import com.ouroboros.chatapp.chatapp.datatype.Message;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.application.Platform;
import javafx.stage.Modality;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.geometry.Insets;


import java.io.BufferedReader;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;

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

    // Flag to indicate if we're currently sending a message
    private final AtomicBoolean isSendingMessage = new AtomicBoolean(false);

    @FXML
    private Button fileButton;

    private int currentChatId;
    private int currentUserId;


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
                cleanup();
            }
        });

        // Set up listener for window close events to stop the thread
        Platform.runLater(() -> {
            if (messageContainer.getScene() != null) {
                Stage stage = (Stage) messageContainer.getScene().getWindow();
                stage.setOnCloseRequest(event -> cleanup());
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

                while (isUpdateRunning.get() && !Thread.currentThread().isInterrupted()) {
                    try {
                        // Skip checking for messages if we're currently sending a message to avoid socket conflicts
                        if (isSendingMessage.get()) {
                            Thread.sleep(100);
                            continue;
                        }

                        // Check if there are any bytes available to read
                        Socket socket = ClientConnection.getSharedSocket();
                        if (socket.getInputStream().available() > 0) {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                            // Try to read a line without blocking indefinitely
                            String line = null;
                            if (reader.ready()) {
                                line = reader.readLine();
                            }

                            if (line != null && line.equals("start: ADD_NEW_MESSAGE")) {
                                Message newMessage = null;

                                System.out.println("Received new message marker for chat ID: " + chatId);

                                // Process the message marker
                                while (!(line = reader.readLine()).equals("end: ADD_NEW_MESSAGE")) {
                                    if (line.startsWith("length: ")) {
                                        int length = Integer.parseInt(line.substring("length: ".length()));
                                        for (int i = 0; i < length && i == 0; i++) {
                                            newMessage = Message.receiveObject(reader);
                                        }
                                    }
                                }

                                System.out.println("message content: " + (newMessage != null ? newMessage.getContent() : "null"));

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
            } finally {
                System.out.println("Message listener thread stopped for chat ID: " + chatId);
            }
        });

        messageListenerThread.setDaemon(true);
        messageListenerThread.start();
    }

    private void stopMessageListener() {
        // Set the flag to stop the thread
        isUpdateRunning.set(false);

        if (messageListenerThread != null && messageListenerThread.isAlive()) {
            messageListenerThread.interrupt();

            try {
                // Wait briefly for thread to terminate
                messageListenerThread.join(500);

                // If thread is still alive after timeout, log a warning
                if (messageListenerThread.isAlive()) {
                    System.err.println("Warning: Message listener thread did not terminate within timeout");
                }
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
        if (!message.isEmpty() && !isSendingMessage.get()) {
            // Clear the message field immediately for better user experience
            String messageToBeSent = message;
            messageInput.clear();

            // Mark that we're sending a message to avoid socket conflicts
            isSendingMessage.set(true);

            // Send message in a background thread with CompletableFuture instead of raw Thread
            CompletableFuture.runAsync(() -> {
                try {
                    System.out.println("Sending message: " + messageToBeSent);
                    messageService.sendMessage(chatId, (int) currentUser.getId(), messageToBeSent);

                    // Update UI with the sent message - we don't need to wait for server confirmation
                    Platform.runLater(() -> {
                        addMessage(messageToBeSent, true);
                        messageScroll.setVvalue(1.0);
                    });

                    System.out.println("Message sent successfully");
                } catch (IOException e) {
                    Platform.runLater(() -> {
                        Stage stage = (Stage) messageContainer.getScene().getWindow();
                        Toast.show(stage, "Failed to send message: " + e.getMessage(), 4000);
                    });
                    System.err.println("Error sending message: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    // Reset the sending flag regardless of success/failure
                    isSendingMessage.set(false);
                }
            });
        }
    }

    @FXML
    private void handleFileUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to Send");

        // Only file pdf
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );

        File selectedFile = fileChooser.showOpenDialog(fileButton.getScene().getWindow());
        if (selectedFile != null) {
            try {
                // 1. Store the file in the "uploads" directory
                Path uploadsDir = Paths.get("uploads");
                if (!Files.exists(uploadsDir)) {
                    Files.createDirectories(uploadsDir);
                }

                Path targetPath = uploadsDir.resolve(selectedFile.getName());
                Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

                // 2. Send the file information as a message
                Message msg = new Message();
                msg.setChatId(currentChatId);
                msg.setSenderId(currentUserId);
                msg.setMessageType(Message.TYPE_FILE);
                msg.setContent("uploads/" + selectedFile.getName());
                msg.setCreatedAt(LocalDateTime.now());
                msg.setUpdatedAt(msg.getCreatedAt());

                MessageService messageService = new MessageService();
                messageService.sendMessage(msg);

                // 3. Render the message in the chat view
                renderMessage(msg);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void setChatAndUser(int chatId, int userId) {
        this.currentChatId = chatId;
        this.currentUserId = userId;
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
        // Do proper cleanup before navigating back
        cleanup();

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
        System.out.println("Performing complete cleanup for ChatViewController");

        // Stop the message listener thread first
        stopMessageListener();

        // Wait until any ongoing message sending completes
        long startTime = System.currentTimeMillis();
        while (isSendingMessage.get() && System.currentTimeMillis() - startTime < 2000) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                break;
            }
        }

        // Reset state variables
        messageListenerThread = null;
        isUpdateRunning.set(false);
        isSendingMessage.set(false);

        System.out.println("ChatViewController cleanup complete");
    }

    @FXML
    private void handleIconClick() {
        // Create a popup window with emoji icons
        Stage iconStage = new Stage();
        GridPane iconGrid = new GridPane();
        iconGrid.setHgap(10);
        iconGrid.setVgap(10);
        iconGrid.setPadding(new Insets(10));
        iconGrid.setStyle("-fx-background-color: white;");

        // Add some emoji icons
        String[] emojis = {"😊", "😂", "❤️", "👍", "🎉", "🔥", "👋", "🎈", "⭐", "🌈",
                "😍", "😎", "🤔", "😢", "😡", "🎂", "🎁", "🎮", "📱", "💻"};

        int col = 0;
        int row = 0;
        for (String emoji : emojis) {
            Button emojiBtn = new Button(emoji);
            emojiBtn.setStyle("-fx-font-size: 24px; -fx-background-color: transparent; -fx-border-color: transparent;");
            emojiBtn.setOnAction(e -> {
                try {
                    // Add the emoji to the message input
                    String currentText = messageInput.getText();
                    messageInput.setText(currentText + emoji);
                    iconStage.close();
                } catch (Exception ex) {
                    System.err.println("Error adding emoji: " + ex.getMessage());
                }
            });
            iconGrid.add(emojiBtn, col, row);
            col++;
            if (col > 4) {
                col = 0;
                row++;
            }
        }
        Scene scene = new Scene(iconGrid);
        iconStage.setScene(scene);
        iconStage.setTitle("Select Emoji");
        iconStage.initModality(Modality.APPLICATION_MODAL); // Make it modal
        iconStage.show();
    }

    private void renderMessage(Message msg) {
        Label label;
        if (msg.isFile()) {
            Hyperlink fileLink = new Hyperlink("📄 " + msg.getContent());
            fileLink.setOnAction(e -> {
                try {
                    File file = new File(msg.getContent());
                    Desktop.getDesktop().open(file);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
            messageContainer.getChildren().add(fileLink);
        } else {
            label = new Label(msg.getContent());
            messageContainer.getChildren().add(label);
        }
    }

}
