package org.chainoptim.desktop.core.notification.controller;

import org.chainoptim.desktop.core.notification.model.Notification;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Objects;

public class NotificationPopupController {

    @FXML
    private Label titleLabel;
    @FXML
    private Button closeButton;
    @FXML
    private Label messageLabel;

    public void initializePopup(Notification notification, Runnable closeAction) {
        titleLabel.setText(notification.getTitle());
        messageLabel.setText(notification.getMessage());

        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/xmark-solid.png")));
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(10);
        imageView.setFitWidth(10);
        closeButton.setGraphic(imageView);
        closeButton.setOnAction(e -> closeAction.run());
    }
}
