package com.ouroboros.chatapp.chatapp;

import com.ouroboros.chatapp.chatapp.clientside.UserService;
import com.ouroboros.chatapp.chatapp.datatype.STATUS;
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

        // Call the UserService to handle login
        if (UserService.login(email, password) == STATUS.SUCCESS) {
            Toast.show(stage, "Login successful!", 2000);
            navigateToHomePage();
        } else {
            Toast.show(stage, "Invalid email or password.", 2000);
        }
    }

    private void navigateToHomePage() {
        try {
            Stage stage = (Stage) loginButton.getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/ouroboros/chatapp/chatapp/View/HomeView.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void onRegisterButtonClick() throws IOException {
        SceneChanger.changeScene("View/RegisterView.fxml");
    }
}