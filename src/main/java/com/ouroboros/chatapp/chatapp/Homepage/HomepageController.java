package com.ouroboros.chatapp.chatapp.Homepage;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.scene.input.MouseEvent;

public class HomepageController {

    @FXML
    private TextField searchField;

    @FXML
    private ListView<String> userList;

    @FXML
    private ListView<ChatPreview> chatListView;

    @FXML
    private Button createButton, logoutButton;

    public void initialize() {
        //mẫu

        userList.getItems().addAll("user1", "user2", "user3", "user4");

        
        chatListView.setCellFactory(listView -> new ChatListCell());

        // List<ChatPreview> fromServer = api.getChatsByUserId(currentUserId);
        //chatListView.setItems(FXCollections.observableArrayList(fromServer));
        //mẫu
        chatListView.setItems(FXCollections.observableArrayList(
            new ChatPreview("Alice", "How are you?", "10:45 AM"),
            new ChatPreview("Team Chat", "New file uploaded", "09:10 AM")
        ));
    }

    @FXML
    private void handleCreate() {
        var selected = userList.getSelectionModel().getSelectedItems();
        System.out.println("Creating chat with: " + selected);
    }

    @FXML
    private void handleLogout() {
        System.out.println("Logging out...");
    }
    @FXML
private void handleChatSelection(MouseEvent event) {
    System.out.println("Chat item clicked!");
}
}
