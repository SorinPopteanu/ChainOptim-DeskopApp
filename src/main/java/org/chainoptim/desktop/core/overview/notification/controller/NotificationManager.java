package org.chainoptim.desktop.core.overview.notification.controller;

import org.chainoptim.desktop.core.overview.notification.model.Notification;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

/**
 * Manages the display of notifications received from WebSocketClient in AppController.
 */
public class NotificationManager {

    private NotificationPopupController popupController;

    private static final int NOTIFICATION_WIDTH = 300;
    private static final int NOTIFICATION_HEIGHT = 100;
    private static final int SPACE_BETWEEN = 20;

    private final Queue<Stage> activeNotifications = new LinkedList<>();

    public void showNotification(Notification notification) {
        System.out.println("Showing notification: " + notification);
        Platform.runLater(() -> {
            Stage popupStage = createNotificationStage(notification);
            positionAndShow(popupStage);
        });
    }

    private Stage createNotificationStage(Notification notification) {
        Stage popupStage = initializeNotificationView(notification);

        popupStage.setOnShown(e -> autoClose(popupStage, 10000));
        popupStage.setOnHidden(e -> {
            activeNotifications.remove(popupStage);
            adjustNotificationPositions();
        });

        return popupStage;
    }

    private Stage initializeNotificationView(Notification notification) {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.NONE);
        popupStage.initStyle(StageStyle.TRANSPARENT);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/chainoptim/desktop/core/notification/NotificationPopupView.fxml"));
        try {
            Parent root = loader.load();
            root.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/notifications.css")).toExternalForm());
            popupController = loader.getController();
            popupController.initializePopup(notification, popupStage::close);

            Scene scene = new Scene(root, NOTIFICATION_WIDTH, NOTIFICATION_HEIGHT);
            scene.setFill(Color.TRANSPARENT);
            popupStage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return popupStage;
    }

    private void positionAndShow(Stage popupStage) {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double baseX = screenBounds.getMaxX() - NOTIFICATION_WIDTH - SPACE_BETWEEN - 40;
        double baseY = screenBounds.getMaxY() - ((NOTIFICATION_HEIGHT + SPACE_BETWEEN) * (activeNotifications.size() + 1));

        popupStage.setX(baseX);
        popupStage.setY(baseY);
        popupStage.show();

        activeNotifications.add(popupStage);
    }

    private void adjustNotificationPositions() {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        int counter = 1;

        for (Stage stage : activeNotifications) {
            double newY = screenBounds.getMaxY() - ((NOTIFICATION_HEIGHT + SPACE_BETWEEN) * counter);
            stage.setY(newY);
            counter++;
        }
    }

    private void autoClose(Stage popupStage, int delayMillis) {
        new Thread(() -> {
            try {
                Thread.sleep(delayMillis);
                Platform.runLater(popupStage::close);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
