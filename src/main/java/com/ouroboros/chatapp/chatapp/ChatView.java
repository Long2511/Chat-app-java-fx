package com.ouroboros.chatapp.chatapp;

import com.ouroboros.chatapp.chatapp.datatype.Chat;
import com.ouroboros.chatapp.chatapp.datatype.User;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

import com.ouroboros.chatapp.chatapp.clientside.ChatService;
import com.ouroboros.chatapp.chatapp.clientside.Toast;

public class ChatView extends Application {

    // For demo purposes
    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ouroboros/chatapp/chatapp/chat-view.fxml"));
            Parent root = loader.load();

            ChatViewController controller = loader.getController();

            primaryStage.setTitle("Chat Demo");
            primaryStage.setScene(new Scene(root, 640, 640));
            primaryStage.show();

            // Handle application close
            primaryStage.setOnCloseRequest(event -> {
                Platform.exit();
                System.exit(0);
            });
        } catch (IOException e) {
            System.err.println("Error loading FXML file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void openChatView(javafx.scene.Node anyNode, String chatNameStr, User user, int chatId) {
        try {
            FXMLLoader loader = new FXMLLoader(ChatView.class.getResource("/com/ouroboros/chatapp/chatapp/chat-view.fxml"));
            Parent chatView = loader.load();
            ChatViewController controller = loader.getController();

            Chat fullChat = ChatService.getChatById(chatId, (int) user.getId());

            controller.setCurrentUser(user); 
            controller.setParticipants(fullChat.getParticipants());
            controller.setChatTitle(chatNameStr);
            controller.setChatAndUser(chatId, (int) user.getId());
            
            // Switch the whole scene
            Stage stage = (Stage) anyNode.getScene().getWindow();
            stage.setScene(new Scene(chatView));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
