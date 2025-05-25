package com.ouroboros.chatapp.chatapp.Homepage;

import com.ouroboros.chatapp.chatapp.clientside.ChatService;
import com.ouroboros.chatapp.chatapp.datatype.Chat;
import com.ouroboros.chatapp.chatapp.datatype.User;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class HomepageController {

    @FXML
    private TextField searchField;

    @FXML
    private ListView<String> userList;

    @FXML
    private ListView<ChatPreview> chatListView;

    @FXML
    private Button createButton, logoutButton;

    @FXML
    private TabPane tabPane;

    @FXML
    private AnchorPane chatViewPane;

    private User loggedInUser;

    public void initialize() {
        // Populate user list and chat previews
        userList.getItems().addAll("user1", "user2", "user3", "user4");
        chatListView.setCellFactory(listView -> new ChatListCell());
        chatListView.setItems(FXCollections.observableArrayList(
                new ChatPreview("Alice", "How are you?", "10:45 AM"),
                new ChatPreview("Team Chat", "New file uploaded", "09:10 AM")
        ));
    }

    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
        System.out.println("Logged-in user: " + user.getUsername());
    }

    @FXML
    private void handleCreate() {
        var selectedUsers = userList.getSelectionModel().getSelectedItems();
        if (!selectedUsers.isEmpty()) {
            List<User> users = ChatService.searchUsers(selectedUsers);
            Chat newChat = ChatService.createChat(users, "New Chat");
            System.out.println("Chat created: " + newChat);
        } else {
            System.out.println("No users selected.");
        }
    }

    @FXML
    private void handleLogout() {
        System.out.println("Logging out...");
        try {
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/ouroboros/chatapp/chatapp/View/LoginView.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleChatSelection(MouseEvent event) {
        ChatPreview selectedChat = chatListView.getSelectionModel().getSelectedItem();
        if (selectedChat != null) {
            System.out.println("Chat selected: " + selectedChat.getUsername());
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ouroboros/chatapp/chatapp/View/chat-view.fxml"));
                AnchorPane chatView = loader.load();
                chatViewPane.getChildren().setAll(chatView);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleTabChange() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if ("Message".equals(selectedTab.getText())) {
            try {
                AnchorPane chatView = FXMLLoader.load(getClass().getResource("/com/ouroboros/chatapp/chatapp/View/chat-view.fxml"));
                chatViewPane.getChildren().setAll(chatView);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}