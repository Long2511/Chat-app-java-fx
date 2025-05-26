// ✅ REVISED FILE: HomepageController.java
// Tính năng: Tạo chat mới (PRIVATE hoặc GROUP), lưu vào bảng `chats` và `chat_participants`, và tự động mở giao diện chat sau khi tạo thành công

package com.ouroboros.chatapp.chatapp.Homepage;

import com.ouroboros.chatapp.chatapp.ChatView;
import com.ouroboros.chatapp.chatapp.ChatViewController;
import com.ouroboros.chatapp.chatapp.clientside.ChatService;
import com.ouroboros.chatapp.chatapp.clientside.Toast;
import com.ouroboros.chatapp.chatapp.datatype.Chat;
import com.ouroboros.chatapp.chatapp.datatype.User;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
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

    private final javafx.collections.ObservableList<User> userSearchResults = FXCollections.observableArrayList();
    @FXML private TextField searchField;
    @FXML private ListView<ChatPreview> chatListView;
    @FXML private Button createButton, logoutButton;
    @FXML private TabPane tabPane;
    @FXML private AnchorPane chatViewPane;
    @FXML private ListView<User> userSearchList;
    @FXML private TextField chatName;
    private User loggedInUser;

    private final javafx.animation.PauseTransition searchDelay = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(0.5));
    private String lastSearchText = "";

    public void initialize() {
        chatListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(ChatPreview item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getTitle());
            }
        });

        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if ("Your Chats".equals(newTab.getText()) && loggedInUser != null) {
                loadChatPreviews();
            }
        });

        userSearchList.setItems(userSearchResults);
        userSearchList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        userSearchList.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                setText((empty || user == null) ? null : user.getUsername() +
                        (user.getStatus() != null ? " [" + user.getStatus() + "]" : ""));
            }
        });

        loadAllUsersToSearchList();

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            lastSearchText = newValue;
            searchDelay.stop();
            searchDelay.setOnFinished(event -> filterUserSearchList(lastSearchText));
            searchDelay.playFromStart();
        });

        userSearchList.getSelectionModel().getSelectedItems().addListener((javafx.collections.ListChangeListener<User>) c -> {
            chatName.setVisible(userSearchList.getSelectionModel().getSelectedItems().size() > 1);
        });
        chatName.setVisible(false);
    }

    private void loadAllUsersToSearchList() {
        new Thread(() -> {
            try {
                List<User> allUsers = com.ouroboros.chatapp.chatapp.clientside.UserService.getAllUsers();
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

        if (selectedUsers.isEmpty()) {
            Stage stage = (Stage) chatListView.getScene().getWindow();
            Toast.show(stage, "Please select at least a person to open chat", 4000);
            return;
        }

        List<Integer> userIds = new ArrayList<>();
        for (User user : selectedUsers) {
            userIds.add((int) user.getId());
        }

        if (!userIds.contains((int) loggedInUser.getId())) {
            userIds.add(0, (int) loggedInUser.getId());
        }

        final String chatNameStr = (userIds.size() > 2)
                ? (chatName.getText() == null || chatName.getText().isBlank() ? "Group Chat" : chatName.getText())
                : selectedUsers.get(0).getUsername();

        new Thread(() -> {
            int chatId = ChatService.createChatGroup(userIds, chatNameStr);
            javafx.application.Platform.runLater(() -> {
                if (chatId > 0) {
                    ChatView.openChatView(createButton, chatNameStr, loggedInUser, chatId);
                } else {
                    Stage stage = (Stage) chatListView.getScene().getWindow();
                    Toast.show(stage, "Failed to create chat", 4000);
                }
            });
        }).start();
    }

    @FXML
    private void handleLogout() {
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
        List<Chat> chats = ChatService.getAllChats((int) loggedInUser.getId());
        List<ChatPreview> previews = chats.stream().map(chat -> {
            String title;
            if ("GROUP".equals(chat.getType())) {
                title = chat.getName() != null && !chat.getName().isBlank()
                        ? chat.getName()
                        : "[Unnamed Group #" + chat.getId() + "]";
            } else {
                title = chat.getParticipants() != null && !chat.getParticipants().isEmpty()
                        ? chat.getParticipants().get(0).getUsername()
                        : "Chat with " + chat.getId();
            }
            return new ChatPreview((int) chat.getId(), title);
        }).toList();
        chatListView.setItems(FXCollections.observableArrayList(previews));
    }

    @FXML
    private void handleChatSelection(MouseEvent event) {
        if (event.getClickCount() == 2) {
            ChatPreview selectedChat = chatListView.getSelectionModel().getSelectedItem();
            if (selectedChat != null) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ouroboros/chatapp/chatapp/View/MessagesView.fxml"));
                    AnchorPane chatView = loader.load();
                    ChatViewController controller = loader.getController();
                    controller.setChatAndUser(selectedChat.getChatId(), (int) loggedInUser.getId());
                    chatViewPane.getChildren().setAll(chatView);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @FXML
    public void handleDeleteAccount(ActionEvent actionEvent) {
        try {
            com.ouroboros.chatapp.chatapp.clientside.UserService.deleteAccount(loggedInUser.getId());
            handleLogout();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
