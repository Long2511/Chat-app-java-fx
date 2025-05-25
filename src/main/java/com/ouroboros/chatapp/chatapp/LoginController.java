package com.ouroboros.chatapp.chatapp;

import com.ouroboros.chatapp.chatapp.Homepage.HomepageController;
import com.ouroboros.chatapp.chatapp.clientside.UserService;
import com.ouroboros.chatapp.chatapp.datatype.STATUS;
import com.ouroboros.chatapp.chatapp.datatype.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.application.Platform;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

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

    private UserService userService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize UserService when the controller is created
        try {
            userService = new UserService();
        } catch (IOException e) {
            Platform.runLater(() -> welcomeLabel.setText("Error connecting to server"));
            e.printStackTrace();
        }
    }

    @FXML
    private void onLoginButtonClick() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            welcomeLabel.setText("Please fill in all fields.");
            return;
        }

        // Check if userService was initialized properly
        if (userService == null) {
            welcomeLabel.setText("Cannot connect to server. Please try again later.");
            return;
        }

        // Call the UserService to handle login
        if (userService.login(email, password) == STATUS.SUCCESS) {
//        loggedInUser = UserService.loginAndGetUser(email, password);
//        if (loggedInUser != null) {
            welcomeLabel.setText("Login successful!");
            navigateToHomePage();
        } else {
            welcomeLabel.setText("Invalid email or password.");
        }
    }

    public void navigateToHomePage() {
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
