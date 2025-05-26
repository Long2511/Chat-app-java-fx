package com.ouroboros.chatapp.chatapp;

import com.ouroboros.chatapp.chatapp.clientside.MessageService;
import com.ouroboros.chatapp.chatapp.datatype.Message;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class MessagesViewController implements Initializable {

    private final ObservableList<Message> messageList = FXCollections.observableArrayList();

    @FXML
    private ListView<Message> messagesListView;

    @FXML
    private TextField messageField;

    @FXML
    private Button sendBtn;

    @FXML
    private TextField chatIdField;

    @FXML
    private TextField senderIdField;

    private MessageService messageService;

    // Thread for message updates
    private Thread updateThread;
    private final AtomicBoolean isUpdateRunning = new AtomicBoolean(false);

    // Latch to ensure initial message load completes before starting the update thread
    private CountDownLatch initialLoadLatch;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set up the ListView with a custom cell factory
        messagesListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Message message, boolean empty) {
                super.updateItem(message, empty);

                if (empty || message == null) {
                    setText(null);
                } else {
                    setText(message.getSenderId() + ": " + message.getContent());
                }
            }
        });

        // Bind the list to the ListView
        messagesListView.setItems(messageList);

        // Set up scene change listener for cleanup
        messagesListView.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene == null && oldScene != null) {
                // Scene changed - stop update thread
                stopUpdateThread();
            }
        });

        // Also handle window close event
        Platform.runLater(() -> {
            messagesListView.getScene().getWindow().addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST,
                    event -> stopUpdateThread());
        });

        // Initialize message service and load initial messages
        try {
            messageService = new MessageService();

            // Load initial messages then start update thread
            initialLoadLatch = new CountDownLatch(1);
            loadInitialMessages();
        } catch (Exception e) {
            System.err.println("Error initializing MessageService: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Loads the initial messages for the current chat
     */
    private void loadInitialMessages() {
        try {
            // Get the chat ID
            int chatId = Integer.parseInt(chatIdField.getText());

            // Load messages in a background thread
            CompletableFuture.runAsync(() -> {
                try {
                    // Request messages from the server
                    messageService.requestMessages(chatId);

                    // Update UI on JavaFX thread
                    Platform.runLater(() -> {
                        messageList.clear();
                        messageList.addAll(messageService.getMessages());

                        // Scroll to the bottom
                        messagesListView.scrollTo(messageList.size() - 1);
                        System.out.println("Initial messages loaded: " + messageList.size());
                    });
                } catch (IOException e) {
                    System.err.println("Error loading initial messages: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    // Signal that initial load is complete
                    initialLoadLatch.countDown();

                    // Start the update thread now that initial load is complete
                    startUpdateThread();
                }
            });
        } catch (NumberFormatException e) {
            System.err.println("Invalid chat ID");

            // Still count down the latch to avoid deadlocks
            initialLoadLatch.countDown();
        }
    }

    /**
     * Starts a thread that continuously checks for new messages
     */
    private void startUpdateThread() {
        // Don't start if already running
        if (isUpdateRunning.get()) return;

        isUpdateRunning.set(true);

        updateThread = new Thread(() -> {
            try {
                // Wait for initial load to complete before starting
                initialLoadLatch.await();

                System.out.println("Message update thread started");

                while (isUpdateRunning.get()) {
                    try {
                        // Check for new messages without blocking
                        String marker = messageService.checkForNewMessageMarker();

                        if (marker != null && marker.equals("start: ADD_NEW_MESSAGE")) {
                            // Process the new message
                            messageService.receiveNewMessage();

                            // Get current chat ID
                            int chatId = Integer.parseInt(chatIdField.getText());

                            // Update UI on JavaFX thread
                            Platform.runLater(() -> {
                                // Update message list
                                messageList.clear();
                                messageList.addAll(messageService.getMessages());

                                // Scroll to latest message
                                messagesListView.scrollTo(messageList.size() - 1);
                                System.out.println("Message list updated: " + messageList.size());
                            });
                        }

                        // Sleep briefly to avoid excessive CPU usage
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        // Thread was interrupted, exit the loop
                        break;
                    } catch (Exception e) {
                        System.err.println("Error in update thread: " + e.getMessage());
                        e.printStackTrace();

                        // Sleep longer after an error
                        Thread.sleep(1000);
                    }
                }
            } catch (InterruptedException e) {
                // Thread was interrupted while waiting for the latch
                System.out.println("Update thread interrupted while waiting for initial load");
            } finally {
                System.out.println("Message update thread stopped");
            }
        });

        // Set as daemon thread so it doesn't prevent app exit
        updateThread.setDaemon(true);
        updateThread.start();
    }

    /**
     * Stops the update thread safely
     */
    private void stopUpdateThread() {
        isUpdateRunning.set(false);

        if (updateThread != null && updateThread.isAlive()) {
            updateThread.interrupt();

            try {
                // Wait briefly for thread to terminate
                updateThread.join(500);
            } catch (InterruptedException e) {
                System.err.println("Interrupted while waiting for update thread to stop");
            }

            System.out.println("Message update thread stopped");
        }
    }

    /**
     * Sends a message to the current chat
     */
    @FXML
    private void handleSend() {
        if (messageService == null || messageField.getText().isEmpty()) {
            return;
        }

        try {
            int chatId = Integer.parseInt(chatIdField.getText());
            int senderId = Integer.parseInt(senderIdField.getText());
            String content = messageField.getText();

            // Clear the message field immediately for better user experience
            String messageToBeSent = content;
            messageField.clear();

            // Send message in a background thread
            CompletableFuture.runAsync(() -> {
                try {
                    messageService.sendMessage(chatId, senderId, messageToBeSent);

                    // Update UI on JavaFX thread (the update thread will also catch this)
                    Platform.runLater(() -> {
                        messageList.clear();
                        messageList.addAll(messageService.getMessages());
                        messagesListView.scrollTo(messageList.size() - 1);
                    });
                } catch (IOException e) {
                    System.err.println("Error sending message: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        } catch (NumberFormatException e) {
            System.err.println("Invalid chat ID or sender ID");
        }
    }

    /**
     * Public cleanup method to be called when view is closed
     */
    public void cleanup() {
        stopUpdateThread();
    }

    public void setChatAndSender(int chatId, int senderId) {
        chatIdField.setText(String.valueOf(chatId));
        senderIdField.setText(String.valueOf(senderId));
//        handleRefresh(); // tự động tải tin nhắn sau khi set
    }
}
