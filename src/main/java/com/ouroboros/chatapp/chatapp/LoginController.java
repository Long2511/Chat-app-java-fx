package com.ouroboros.chatapp.chatapp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import java.io.IOException;

import com.ouroboros.chatapp.chatapp.clientside.Toast;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    @FXML
    private Label welcomeLabel;

    @FXML
    private void onLoginButtonClick() {
        String email = emailField.getText();
        String password = passwordField.getText();
        Stage stage = (Stage) loginButton.getScene().getWindow();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.show(stage, "Please fill in all fields.", 2000);
            return;
        }

        // Simulate login logic
        if (email.equals("user@example.com") && password.equals("password")) {
            Toast.show(stage, "Login successful!", 2000);
        } else {
            Toast.show(stage, "Invalid email or password.", 2000);
        }
    }

    @FXML
    private void onRegisterButtonClick() throws IOException {
        Stage stage = (Stage) registerButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("View/RegisterView.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
    }
}