package com.ouroboros.chatapp.chatapp;

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
import javafx.stage.Modality;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.geometry.Insets;



import java.awt.*;
import java.io.File;
import java.io.IOException;
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