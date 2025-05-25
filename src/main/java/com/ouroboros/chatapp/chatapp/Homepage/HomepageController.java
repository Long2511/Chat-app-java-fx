package com.ouroboros.chatapp.chatapp.Homepage;

import com.ouroboros.chatapp.chatapp.clientside.ChatService;
import com.ouroboros.chatapp.chatapp.datatype.Chat;
import com.ouroboros.chatapp.chatapp.datatype.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import javafx.scene.control.ListView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.ArrayList;

import java.io.IOException;
import java.util.List;

public class HomepageController {

    @FXML
    private ListView<String> userList;

    @FXML
    private ListView<ChatPreview> chatListView;

    @FXML
    private Button createButton;

    @FXML
    private TabPane tabPane;

    @FXML
    private AnchorPane chatViewPane;

    private User loggedInUser;

    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
        System.out.println("Logged-in user: " + user.getUsername());
    }

    @FXML
    private void handleCreate() {
        var selectedUsers = userList.getSelectionModel().getSelectedItems();
        if (!selectedUsers.isEmpty()) {
            List<String> selectedUsernames = new ArrayList<>(selectedUsers);
            List<User> users = ChatService.searchUsers(selectedUsernames);
            Chat newChat = ChatService.createChat(users, "New Chat");
            System.out.println("Chat created: " + newChat);
        } else {
            System.out.println("No users selected.");
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