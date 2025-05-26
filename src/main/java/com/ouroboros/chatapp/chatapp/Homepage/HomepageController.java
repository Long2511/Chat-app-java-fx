package com.ouroboros.chatapp.chatapp.Homepage;

import com.ouroboros.chatapp.chatapp.MessagesViewController;
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


        chatListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(ChatPreview item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getTitle());
            }
        });

        
        // load chat previews when the "Message" tab is selected
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if ("Your Chats".equals(newTab.getText()) && loggedInUser != null) {
        loadChatPreviews();
        }
    });

    }

    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
        System.out.println("Logged-in user: " + user.getUsername());
        loadChatPreviews();
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
    private void loadChatPreviews() {
    List<Chat> chats = ChatService.getAllChats(loggedInUser.getId());

    List<ChatPreview> previews = chats.stream()
        .map(chat -> {
            String title = "GROUP".equals(chat.getType())
                    ? chat.getName()
                    : "Chat #" + chat.getId(); // sau này thay bằng tên người đối phương
            return new ChatPreview(chat.getId(), title);
        })
        .toList();
    System.out.println("Loaded chat previews: " + previews);
    chatListView.setItems(FXCollections.observableArrayList(previews));
}


    @FXML
    private void handleChatSelection(MouseEvent event) {
        ChatPreview selectedChat = chatListView.getSelectionModel().getSelectedItem();
        if (selectedChat != null) {
            System.out.println("Chat selected: " + selectedChat.getTitle());
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ouroboros/chatapp/chatapp/View/MessagesView.fxml"));
                AnchorPane chatView = loader.load();
                //set the chat ID and sender ID in the controller
                MessagesViewController controller = loader.getController();
                controller.setChatAndSender(selectedChat.getChatId(), loggedInUser.getId());

                chatViewPane.getChildren().setAll(chatView);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

   /*  @FXML
    private void handleTabChange() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if ("Message".equals(selectedTab.getText())) {
            try {
                AnchorPane chatView = FXMLLoader.load(getClass().getResource("/com/ouroboros/chatapp/chatapp/View/MessagesView.fxml"));
                chatViewPane.getChildren().setAll(chatView);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }*/
}