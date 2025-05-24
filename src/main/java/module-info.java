module com.ouroboros.chatapp.chatapp {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.ouroboros.chatapp.chatapp to javafx.fxml;
    exports com.ouroboros.chatapp.chatapp;
}