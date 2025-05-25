/**
 * Toast.java
 * <p>
 * This class is used to create and display toast messages.
 * Toast messages are small notifications that appear temporarily on the screen.
 *
 * @author Hai Long Mac
 */

package com.ouroboros.chatapp.chatapp.clientside;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class Toast {

    // List to track currently active toasts to avoid overlapping
    private static final List<Popup> activeToasts = new ArrayList<>();

    /**
     * Displays a toast message on the screen.
     *
     * @param ownerStage      The stage on which the toast should be displayed.
     * @param message         The message to be shown.
     * @param durationInMillis Duration for which the toast should be visible (in milliseconds).
     */
    public static void show(Stage ownerStage, String message, int durationInMillis) {
        Platform.runLater(() -> {
            Popup popup = new Popup();
            popup.setAutoFix(true);
            popup.setAutoHide(true);
            popup.setHideOnEscape(true);

            Label label = new Label(message);
            label.setStyle("-fx-background-color: black; -fx-text-fill: white; " +
                    "-fx-border-radius: 10px; -fx-background-radius: 10px; " +
                    "-fx-label-padding: 10px");
            label.setOpacity(0);

            StackPane pane = new StackPane(label);
            pane.setStyle("-fx-background-color: transparent;");
            popup.getContent().add(pane);

            // Calculate position based on existing toasts
            popup.setOnShown(event -> {
                Scene scene = ownerStage.getScene();
                double baseX = scene.getWindow().getX() + (scene.getWidth() - pane.getWidth()) / 2;
                double baseY = scene.getWindow().getY() + (scene.getHeight() - pane.getHeight()) / 2;

                for (Popup activeToast : activeToasts) {
                    baseY += 50; // Stack toasts below existing ones
                }

                popup.setX(baseX);
                popup.setY(baseY);
                activeToasts.add(popup);
            });

            // Remove toast from active list when hidden
            popup.setOnHidden(event -> activeToasts.remove(popup));
            popup.show(ownerStage);

            // Fade in animation
            FadeTransition fadeIn = new FadeTransition(Duration.millis(500), label);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1.0);

            // Fade out animation
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), label);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0);
            fadeOut.setDelay(Duration.millis(durationInMillis));

            fadeIn.setOnFinished(e -> fadeOut.play());
            fadeOut.setOnFinished(e -> popup.hide());

            fadeIn.play();
        });
    }
}