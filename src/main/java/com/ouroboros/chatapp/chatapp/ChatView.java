package com.ouroboros.chatapp.chatapp;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ChatView extends Application {

    // For demo purposes
    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ouroboros/chatapp/chatapp/View/MessagesView.fxml"));
            Parent root = loader.load();

            MessagesViewController controller = loader.getController();

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

    public static void main(String[] args) {
        launch(args);
    }
}
