package com.ouroboros.chatapp.chatapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ouroboros/chatapp/chatapp/View/LoginView.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Chat Application");

//        // Set the icon for the primary stage
//        Image icon = new Image(getClass().getResourceAsStream("/GameHandler/lottikarotti_main/images/icon_game.jpg"));
//        primaryStage.getIcons().add(icon);

        primaryStage.show();

        SceneChanger.setPrimaryStage(primaryStage);

        primaryStage.setResizable(false);
    }

    public static void main(String[] args) {
        launch();
    }
}
