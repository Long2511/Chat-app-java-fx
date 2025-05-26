package com.ouroboros.chatapp.chatapp;

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
import javafx.scene.control.Alert;

import java.io.IOException;

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
        
        // Style the message based on sender
        if (isFromCurrentUser) {
            messageArea.setStyle("-fx-background-color: #007AFF; -fx-text-fill: white; -fx-background-radius: 10;");
        } else {
            messageArea.setStyle("-fx-background-color: #E5E5EA; -fx-text-fill: black; -fx-background-radius: 10;");
        }

        HBox messageBox = new HBox(messageArea);
        messageBox.setAlignment(isFromCurrentUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        messageBox.setMaxWidth(messageScroll.getWidth());
        
        messageContainer.getChildren().add(messageBox);
        
        // Scroll to bottom
        messageScroll.setVvalue(1.0);
    }

    public void setChatTitle(String username) {
        chatTitle.setText(username);
    }

    public void setAvatarColor(String color) {
        avatarRect.setFill(Color.web(color));
    }

    @FXML
    private void handleBackButton() {
            try {
                SceneChanger.changeScene("/com/ouroboros/chatapp/chatapp/View/Homepage.fxml");
            } catch (IOException e) {
                Stage stage = (Stage) messageContainer.getScene().getWindow();

                Toast.show(stage, "Cannot go back", 4000);
            }
    }
}