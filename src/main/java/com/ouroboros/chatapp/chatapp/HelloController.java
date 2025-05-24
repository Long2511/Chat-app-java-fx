package com.ouroboros.chatapp.chatapp;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class HelloController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void handleSendMessage() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}