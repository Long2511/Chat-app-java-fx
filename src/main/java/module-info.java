module com.ouroboros.chatapp.chatapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;
    requires java.sql;
    requires org.postgresql.jdbc;
    requires jbcrypt;
    requires jjwt.api;


    opens com.ouroboros.chatapp.chatapp to javafx.fxml;
    exports com.ouroboros.chatapp.chatapp;
     opens com.ouroboros.chatapp.chatapp.Homepage to javafx.fxml;
    exports com.ouroboros.chatapp.chatapp.Homepage to javafx.fxml;
}