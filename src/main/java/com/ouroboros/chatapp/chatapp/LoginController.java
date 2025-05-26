package com.ouroboros.chatapp.chatapp;

import com.ouroboros.chatapp.chatapp.Homepage.HomepageController;
import com.ouroboros.chatapp.chatapp.clientside.UserService;
import com.ouroboros.chatapp.chatapp.datatype.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

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
    private void onLoginButtonClick() throws IOException {
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
        loggedInUser = userService.login(email, password);
        if (loggedInUser != null) {
            welcomeLabel.setText("Login successful!");

            // Navigate to the homepage
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ouroboros/chatapp/chatapp/View/Homepage.fxml"));
            Scene scene = new Scene(loader.load());

            // Truyền user vào controller
            HomepageController controller = loader.getController();
            controller.setLoggedInUser(loggedInUser);

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);
        } else {
            welcomeLabel.setText("Invalid email or password.");
        }
    }


    @FXML
    protected void onRegisterButtonClick() throws IOException {
        SceneChanger.changeScene("View/RegisterView.fxml");
    }

       @FXML
    protected void onForgotPasswordButtonClick() throws IOException {
        SceneChanger.changeScene("View/ForgotPassword.fxml");
    }
}
