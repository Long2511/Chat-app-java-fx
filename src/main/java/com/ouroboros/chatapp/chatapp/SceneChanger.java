package com.ouroboros.chatapp.chatapp;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneChanger {
    public static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static void changeScene(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(SceneChanger.class.getResource(fxml));
        Scene scene = new Scene(fxmlLoader.load());
        primaryStage.setScene(scene);
    }
}
