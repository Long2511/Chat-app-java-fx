package com.ouroboros.chatapp.chatapp;

import com.ouroboros.chatapp.chatapp.clientside.UserService;
import com.ouroboros.chatapp.chatapp.datatype.STATUS;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class RegisterController implements Initializable {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button registerButton;

    @FXML
    private Button loginButton;

    @FXML
    private Label statusLabel;

    private UserService userService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            userService = new UserService();
        } catch (IOException e) {
            Platform.runLater(() -> statusLabel.setText("Error connecting to server"));
            e.printStackTrace();
        }
    }

    @FXML
    private void onRegisterButtonClick() {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please fill in all fields.");
            return;
        }

        // Check if userService was initialized properly
        if (userService == null) {
            statusLabel.setText("Cannot connect to server. Please try again later.");
            return;
        }

        // Call the UserService to handle registration
        if (userService.register(username, email, password) == STATUS.SUCCESS) {
            statusLabel.setText("Registration successful!");
            try {
                // Redirect to login view after successful registration
                SceneChanger.changeScene("/com/ouroboros/chatapp/chatapp/View/LoginView.fxml");
            } catch (IOException e) {
                statusLabel.setText("Error changing scene: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            statusLabel.setText("Registration failed. Email might already exist.");
        }
    }

    @FXML
    private void onLoginButtonClick() throws IOException {
        SceneChanger.changeScene("View/LoginView.fxml");
    }

}