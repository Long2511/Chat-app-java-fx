package com.ouroboros.chatapp.chatapp;

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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

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
    public void initialize() {
        // Initialize any necessary setup
        messageScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        messageScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        // Set up message input handling
        messageInput.setOnAction(event -> handleSendMessage());
    }

    @FXML
    private void handleSendMessage() {
        String message = messageInput.getText().trim();
        if (!message.isEmpty()) {
            addMessage(message, true); // true indicates it's from the current user
            messageInput.clear();
            // TODO: Implement actual message sending logic
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

    public void setAvatarColor(String color) {
        avatarRect.setFill(Color.web(color));
    }
} 