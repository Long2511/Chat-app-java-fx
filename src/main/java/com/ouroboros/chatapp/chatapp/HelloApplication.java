package com.ouroboros.chatapp.chatapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
            HelloApplication.class.getResource("/com/ouroboros/chatapp/chatapp/View/Homepage.fxml")
        );
        Scene scene = new Scene(fxmlLoader.load(), 640, 640); // chỉnh size phù hợp
        stage.setTitle("Chat Application");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
