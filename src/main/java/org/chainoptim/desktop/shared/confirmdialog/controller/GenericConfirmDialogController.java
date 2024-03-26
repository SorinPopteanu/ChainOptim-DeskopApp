package org.chainoptim.desktop.shared.confirmdialog.controller;

import org.chainoptim.desktop.core.abstraction.ControllerFactory;
import org.chainoptim.desktop.shared.confirmdialog.controller.GenericConfirmDialogActionListener;
import org.chainoptim.desktop.shared.confirmdialog.model.ConfirmDialogDataReceiver;
import org.chainoptim.desktop.shared.confirmdialog.model.ConfirmDialogInput;
import org.chainoptim.desktop.shared.util.DataReceiver;
import org.chainoptim.desktop.shared.util.resourceloader.FXMLLoaderService;
import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import lombok.Setter;

import java.io.IOException;

public class GenericConfirmDialogController<T> implements ConfirmDialogDataReceiver<T> {

//    private final UserService userService;

//    private Integer customRoleId;
    private final FXMLLoaderService fxmlLoaderService;
    private final ControllerFactory controllerFactory;

    private T data;
    private ConfirmDialogInput confirmDialogInput;

    @Setter
    private GenericConfirmDialogActionListener<T> actionListener;

    @FXML
    private Label titleLabel;
    @FXML
    private TextFlow messageTextFlowContainer;
    @FXML
    private StackPane extraStackPane;
    @FXML
    private Button confirmButton;
    @FXML
    private Button cancelButton;

    @Inject
    public GenericConfirmDialogController(FXMLLoaderService fxmlLoaderService, ControllerFactory controllerFactory) {
        this.fxmlLoaderService = fxmlLoaderService;
        this.controllerFactory = controllerFactory;
    }

//    @Inject
//    public GenericConfirmDialogController(UserService userService) {
//        this.userService = userService;
//    }

    @Override
    public void setData(T data, ConfirmDialogInput confirmDialogInput) {
        this.data = data;
        this.confirmDialogInput = confirmDialogInput;

        initializeUI();
//        userService.getUsersByCustomRoleId(customRoleId)
//                .thenApply(this::handleUsersResponse)
//                .exceptionally(this::handleUsersException);
    }

    private void initializeUI() {
        titleLabel.setText(confirmDialogInput.getDialogTitle());
        messageTextFlowContainer.getChildren().add(new Text(confirmDialogInput.getDialogMessage()));

        String extraViewFilePath = confirmDialogInput.getDialogExtraChildViewPath();
        if (extraViewFilePath != null) {
            loadExtraContainer(extraViewFilePath);
        } else {
            extraStackPane.setVisible(false);
            extraStackPane.setManaged(false);
        }
    }

    private void loadExtraContainer(String extraViewFilePath) {
        FXMLLoader loader = fxmlLoaderService.setUpLoader(extraViewFilePath, controllerFactory::createController);
        try {
            Node extraView = loader.load();
            Object extraController = loader.getController();
            setDataToExtraController(extraController);
            extraStackPane.getChildren().add(extraView);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void setDataToExtraController(Object extraController) {
        if (extraController instanceof DataReceiver dataReceiver) {
            try {
                dataReceiver.setData(data);
            } catch (ClassCastException ex) {
                ex.printStackTrace();
            }
        }
    }


    @FXML
    private void onConfirmButtonClicked() {
        actionListener.onConfirmAction(data);
    }

    @FXML
    private void onCancelButtonClicked() {
        actionListener.onCancelAction();
    }
}
