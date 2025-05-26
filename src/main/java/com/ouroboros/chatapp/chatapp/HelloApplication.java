package com.ouroboros.chatapp.chatapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ouroboros/chatapp/chatapp/View/LoginView.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Chat Application");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/com/ouroboros/chatapp/chatapp/image/emoji_icon.png")));

        primaryStage.show();

        SceneChanger.setPrimaryStage(primaryStage);

        primaryStage.setResizable(false);
    }
}
