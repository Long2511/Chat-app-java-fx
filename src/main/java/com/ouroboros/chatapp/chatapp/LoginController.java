package com.ouroboros.chatapp.chatapp;

import com.ouroboros.chatapp.chatapp.Homepage.HomepageController;
import com.ouroboros.chatapp.chatapp.clientside.UserService;
import com.ouroboros.chatapp.chatapp.datatype.STATUS;
import com.ouroboros.chatapp.chatapp.datatype.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;

import java.io.IOException;

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

    private User loggedInUser;

    @FXML
    private void onLoginButtonClick() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            welcomeLabel.setText("Please fill in all fields.");
            return;
        }

        // Call the UserService to handle login
        loggedInUser = UserService.loginAndGetUser(email, password);
        if (loggedInUser != null) {
            welcomeLabel.setText("Login successful!");
            navigateToHomePage();
        } else {
            welcomeLabel.setText("Invalid email or password.");
        }
    }

    private void navigateToHomePage() {
        try {
            Stage stage = (Stage) loginButton.getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/ouroboros/chatapp/chatapp/View/Homepage.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            // Pass the logged-in user to the HomepageController
            HomepageController controller = fxmlLoader.getController();
            controller.setLoggedInUser(loggedInUser);

            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onRegisterButtonClick() throws IOException {
        Stage stage = (Stage) registerButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/ouroboros/chatapp/chatapp/View/RegisterView.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
    }
}