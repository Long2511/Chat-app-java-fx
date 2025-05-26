package com.ouroboros.chatapp.chatapp;

import com.ouroboros.chatapp.chatapp.clientside.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;

import java.io.IOException;

public class ForgotPasswordController {
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private Button resetButton;
    @FXML
    private Label statusLabel;
    @FXML
    private Button backToLoginButton;

    @FXML
    private void onResetButtonClick() {
        String email = emailField.getText();
        String newPassword = newPasswordField.getText();
        if (email.isEmpty() || newPassword.isEmpty()) {
            statusLabel.setText("Please enter your email and new password.");
            return;
        }
        boolean result = UserService.forgotPassword(email, newPassword);
        if (result) {
            statusLabel.setText("Password reset successful.");
        } else {
            statusLabel.setText("Failed to reset password. Check your email.");
        }
    }

    @FXML
    private void onBackToLoginClick() throws IOException {
        Stage stage = (Stage) backToLoginButton.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ouroboros/chatapp/chatapp/View/LoginView.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
    }
}
