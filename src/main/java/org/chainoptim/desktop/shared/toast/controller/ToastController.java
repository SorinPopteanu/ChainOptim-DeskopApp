package org.chainoptim.desktop.shared.toast.controller;

import org.chainoptim.desktop.shared.enums.OperationOutcome;
import org.chainoptim.desktop.shared.toast.model.ToastInfo;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.util.Objects;

public class ToastController {

    private ToastInfo toastInfo;
    private Runnable closeCallback;

    @FXML
    private Circle outcomeIconWrapper;
    @FXML
    private Label outcomeIcon;
    @FXML
    private Label titleLabel;
    @FXML
    private Button closeButton;

    // Icons
    private Image closeIcon;
    private Image successIcon;
    private Image errorIcon;
    private Image infoIcon;

    public void initialize(ToastInfo toastInfo, Runnable closeCallback) {
        this.toastInfo = toastInfo;
        this.closeCallback = closeCallback;

        initializeIcons();
        initializeToast();
    }

    private void initializeIcons() {
        closeIcon = new Image(Objects.requireNonNull(ToastController.class.getResourceAsStream("/img/xmark-solid.png")));
        successIcon = new Image(Objects.requireNonNull(ToastController.class.getResourceAsStream("/img/check-solid.png")));
        errorIcon = new Image(Objects.requireNonNull(ToastController.class.getResourceAsStream("/img/xmark-solid.png")));
        infoIcon = new Image(Objects.requireNonNull(ToastController.class.getResourceAsStream("/img/circle-info-solid.png")));
    }

    private void initializeToast() {
        initializeOutcomeIcon();

        titleLabel.setText(this.toastInfo.getTitle());

        ImageView closeIconView = new ImageView(closeIcon);
        closeIconView.setFitHeight(14);
        closeIconView.setFitWidth(14);
        closeButton.setGraphic(closeIconView);
        closeButton.getStyleClass().setAll("no-style-button");

        closeButton.setOnAction(e -> closeCallback.run());
    }

    private void initializeOutcomeIcon() {
        if (toastInfo == null) return;

        ImageView iconView = new ImageView();
        switch (toastInfo.getOperationOutcome()) {
            case OperationOutcome.SUCCESS -> {
                iconView.setImage(successIcon);
                outcomeIconWrapper.getStyleClass().setAll("success-outcome-icon-wrapper");
            }
            case OperationOutcome.ERROR -> {
                iconView.setImage(errorIcon);
                outcomeIconWrapper.getStyleClass().setAll("error-outcome-icon-wrapper");
            }
            case OperationOutcome.INFO -> {
                iconView.setImage(infoIcon);
                outcomeIconWrapper.getStyleClass().setAll("info-outcome-icon-wrapper");
            }
            default -> {}
        }
        iconView.setFitHeight(20);
        iconView.setFitWidth(20);

        outcomeIcon.setGraphic(iconView);
    }
}
