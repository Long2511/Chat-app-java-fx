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
import java.util.ArrayList;
import java.util.List;

public class HomepageController {

    private final javafx.collections.ObservableList<User> userSearchResults = javafx.collections.FXCollections.observableArrayList();
    @FXML
    private TextField searchField;
    @FXML
    private ListView<ChatPreview> chatListView;
    @FXML
    private Button createButton, logoutButton;
    @FXML
    private TabPane tabPane;
    @FXML
    private AnchorPane chatViewPane;
    @FXML
    private ListView<User> userSearchList;
    private User loggedInUser;

    private final javafx.animation.PauseTransition searchDelay = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(0.5));
    private String lastSearchText = "";

    public void initialize() {
        chatListView.setCellFactory(listView -> new ChatListCell());
        chatListView.setItems(FXCollections.observableArrayList(
                new ChatPreview("Alice", "How are you?", "10:45 AM"),
                new ChatPreview("Team Chat", "New file uploaded", "09:10 AM")
        ));
        userSearchList.setItems(userSearchResults);
        userSearchList.setCellFactory(listView -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                } else {
                    setText(user.getUsername() + (user.getStatus() != null ? " [" + user.getStatus() + "]" : ""));
                }
            }
        });
        loadAllUsersToSearchList();
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            lastSearchText = newValue;
            searchDelay.stop();
            searchDelay.setOnFinished(event -> filterUserSearchList(lastSearchText));
            searchDelay.playFromStart();
        });
    }

    private void loadAllUsersToSearchList() {
        new Thread(() -> {
            try {
                java.util.List<User> allUsers = com.ouroboros.chatapp.chatapp.clientside.UserService.getAllUsers();
                javafx.application.Platform.runLater(() -> {
                    userSearchResults.clear();
                    userSearchResults.addAll(allUsers);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void filterUserSearchList(String query) {
        if (query == null || query.isEmpty()) {
            loadAllUsersToSearchList();
            return;
        }
        new Thread(() -> {
            try {
                List<User> filtered = com.ouroboros.chatapp.chatapp.clientside.UserService.searchUsers(query);
                javafx.application.Platform.runLater(() -> {
                    userSearchResults.clear();
                    userSearchResults.addAll(filtered);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
        System.out.println("Logged-in user: " + user.getUsername());
    }

    @FXML
    private void handleCreate() {
        var selectedUsers = userSearchList.getSelectionModel().getSelectedItems();
        if (!selectedUsers.isEmpty()) {
            List<String> usernames = new ArrayList<>();
            for (User user : selectedUsers) {
                usernames.add(user.getUsername());
            }
            List<User> users = ChatService.searchUsers(usernames);
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