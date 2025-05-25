package com.ouroboros.chatapp.chatapp;

import com.ouroboros.chatapp.chatapp.clientside.ClientConnection;
import com.ouroboros.chatapp.chatapp.clientside.MessageService;
import com.ouroboros.chatapp.chatapp.datatype.Message;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MessagesViewController implements Initializable {

    @FXML
    private ListView<Message> messagesListView;

    @FXML
    private TextField messageField;

    @FXML
    private Button sendBtn;

    @FXML
    private Button refreshBtn;

    @FXML
    private TextField chatIdField;

    @FXML
    private TextField senderIdField;

    private final ObservableList<Message> messageList = FXCollections.observableArrayList();
    private MessageService messageService;

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

        // Initialize message service
        try {
            messageService = new MessageService();
            handleRefresh(); // Load initial messages
        } catch (Exception e) {
            System.err.println("Failed to initialize message service: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        if (messageService == null) return;

        try {
            int chatId = Integer.parseInt(chatIdField.getText());

            // Run in background thread to avoid UI freeze
            new Thread(() -> {
                try {
                    messageService.requestMessages(chatId);

                    // Update UI on JavaFX thread
                    Platform.runLater(() -> {
                        messageList.clear();
                        messageList.addAll(messageService.getMessages());
                        messagesListView.scrollTo(messageList.size() - 1);
                    });
                } catch (IOException e) {
                    System.err.println("Error refreshing messages: " + e.getMessage());
                }
            }).start();
        } catch (NumberFormatException e) {
            System.err.println("Invalid chat ID");
        }
    }

    @FXML
    private void handleSend() {
        if (messageService == null || messageField.getText().isEmpty()) return;

        try {
            int chatId = Integer.parseInt(chatIdField.getText());
            int senderId = Integer.parseInt(senderIdField.getText());
            String content = messageField.getText();

            // Run in background thread to avoid UI freeze
            new Thread(() -> {
                try {
                    messageService.sendMessage(chatId, senderId, content);

                    // Update UI on JavaFX thread
                    Platform.runLater(() -> {
                        messageList.clear();
                        messageList.addAll(messageService.getMessages());
                        messagesListView.scrollTo(messageList.size() - 1);
                        messageField.clear();
                    });
                } catch (IOException e) {
                    System.err.println("Error sending message: " + e.getMessage());
                }
            }).start();
        } catch (NumberFormatException e) {
            System.err.println("Invalid chat ID or sender ID");
        }
    }
}
