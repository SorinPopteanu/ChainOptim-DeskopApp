package org.chainoptim.desktop.shared.toast.controller;

import org.chainoptim.desktop.shared.enums.OperationOutcome;
import org.chainoptim.desktop.shared.toast.model.ToastInfo;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;

/**
 * Manager of Toast Display throughout the application.
 * Usage: Inject the interface with Guice and call addToast(ToastInfo) to display a toast.
 */
public class ToastManagerImpl implements ToastManager {

    private final Stage mainStage;

    private static final int TOAST_WIDTH = 360;
    private static final int TOAST_MIN_HEIGHT = 100;
    private static final int TOAST_MAX_HEIGHT = 300;
    private static final int SPACE_BETWEEN = 20;
    private static final Map<OperationOutcome, Integer> timeToCloseSeconds = Map.of(
            OperationOutcome.SUCCESS, 5,
            OperationOutcome.ERROR, 120,
            OperationOutcome.WARNING, 10,
            OperationOutcome.INFO, 10
    );

    private final Queue<Stage> activeToasts = new LinkedList<>();

    @Inject
    public ToastManagerImpl(Stage mainStage) {
        this.mainStage = mainStage;
    }

    public void addToast(ToastInfo toastInfo) {
        System.out.println("Showing toast: " + toastInfo);
        Platform.runLater(() -> {
            Stage popupStage = createToastStage(toastInfo);
            positionAndShow(popupStage);
        });
    }

    private Stage createToastStage(ToastInfo toastInfo) {
        Stage popupStage = initializeToastView(toastInfo);

        popupStage.setOnShown(e -> autoClose(popupStage,
                timeToCloseSeconds.get(toastInfo.getOperationOutcome()) * 1000));
        popupStage.setOnHidden(e -> {
            activeToasts.remove(popupStage);
            adjustToastsPositions();
        });

        return popupStage;
    }

    private Stage initializeToastView(ToastInfo toastInfo) {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.NONE);
        popupStage.initStyle(StageStyle.TRANSPARENT);
        popupStage.setAlwaysOnTop(true);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/chainoptim/desktop/shared/toast/ToastView.fxml"));
        try {
            Parent root = loader.load();
            root.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/toast.css")).toExternalForm());
            ToastController popupController = loader.getController();
            popupController.initialize(toastInfo, () -> closeToast(popupStage));

            Scene scene = new Scene(root, TOAST_WIDTH, TOAST_MIN_HEIGHT);
            scene.setFill(Color.TRANSPARENT);
            popupStage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return popupStage;
    }

    private void positionAndShow(Stage popupStage) {
        double baseX = mainStage.getX() + mainStage.getWidth() - TOAST_WIDTH - SPACE_BETWEEN - 30;
        double baseY = mainStage.getY() + mainStage.getHeight() - ((TOAST_MIN_HEIGHT + SPACE_BETWEEN) * (activeToasts.size()) + 1) - 150;

        popupStage.setX(baseX);
        popupStage.setY(baseY);
        popupStage.show();

        activeToasts.add(popupStage);
    }

    private void adjustToastsPositions() {
        double baseY = mainStage.getY() + mainStage.getHeight();
        int counter = 1;

        for (Stage stage : activeToasts) {
            double newY = baseY - (TOAST_MIN_HEIGHT + SPACE_BETWEEN) * counter;
            stage.setY(newY);
            counter++;
        }
    }

    private void autoClose(Stage popupStage, int delayMillis) {
        new Thread(() -> {
            try {
                Thread.sleep(delayMillis);
                closeToast(popupStage);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void closeToast(Stage popupStage) {
        activeToasts.remove(popupStage);
        Platform.runLater(popupStage::close);
    }

}
