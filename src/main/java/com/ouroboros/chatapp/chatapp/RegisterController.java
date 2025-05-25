package com.ouroboros.chatapp.chatapp;

import com.ouroboros.chatapp.chatapp.clientside.UserService;
import com.ouroboros.chatapp.chatapp.datatype.STATUS;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.application.Platform;

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
            navigateToLoginView();
        } else {
            statusLabel.setText("Registration failed. Email might already exist.");
        }
    }

    @FXML
    private void onLoginButtonClick() {
        navigateToLoginView();
    }

    private void navigateToLoginView() {
        try {
            Stage stage = (Stage) loginButton.getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/ouroboros/chatapp/chatapp/View/LoginView.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}