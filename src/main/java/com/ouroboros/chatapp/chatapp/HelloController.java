package com.ouroboros.chatapp.chatapp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloController {

    @FXML
    private Button registerButton;

    @FXML
    private void onHelloButtonClick() throws IOException {
        // Get the current stage from the button
        Stage stage = (Stage) registerButton.getScene().getWindow();

        // Load the LoginView.fxml
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("View/LoginView.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        // Set the new scene
        stage.setScene(scene);
    }
}