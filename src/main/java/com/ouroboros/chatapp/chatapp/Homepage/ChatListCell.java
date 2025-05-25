package com.ouroboros.chatapp.chatapp.Homepage;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListCell;
import javafx.scene.Parent;
import java.io.IOException;

public class ChatListCell extends ListCell<ChatPreview> {
    @Override
    protected void updateItem(ChatPreview item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
        } else {
            try {
                FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/ouroboros/chatapp/chatapp/View/ChatListCell.fxml")
                );
                Parent root = loader.load();

                ChatListCellController controller = loader.getController();
                controller.usernameLabel.setText(item.getUsername());
                controller.lastMessageLabel.setText(item.getLastMessage());
                controller.timeLabel.setText(item.getTime());

                setGraphic(root);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
