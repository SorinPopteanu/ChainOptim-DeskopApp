package org.chainoptim.desktop.core.organization.controller;

import org.chainoptim.desktop.core.abstraction.ControllerFactory;
import org.chainoptim.desktop.core.organization.model.CustomRole;
import org.chainoptim.desktop.core.organization.model.Organization;
import org.chainoptim.desktop.shared.confirmdialog.controller.GenericConfirmDialogActionListener;
import org.chainoptim.desktop.shared.confirmdialog.controller.GenericConfirmDialogController;
import org.chainoptim.desktop.shared.confirmdialog.controller.RunnableConfirmDialogActionListener;
import org.chainoptim.desktop.shared.confirmdialog.model.ConfirmDialogInput;
import org.chainoptim.desktop.shared.util.DataReceiver;
import org.chainoptim.desktop.shared.util.resourceloader.FXMLLoaderService;
import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.function.Consumer;

public class OrganizationSubscriptionPlanController implements DataReceiver<Organization> {

    private final FXMLLoaderService fxmlLoaderService;
    private final ControllerFactory controllerFactory;

    private Organization organization;

    // Listeners
    private RunnableConfirmDialogActionListener<CustomRole> confirmDialogUpdateListener;
    private RunnableConfirmDialogActionListener<CustomRole> confirmDialogDeleteListener;

    // FXML
    @FXML
    private StackPane updateConfirmDialogPane;
    @FXML
    private StackPane deleteConfirmDialogPane;


    @Inject
    public OrganizationSubscriptionPlanController(FXMLLoaderService fxmlLoaderService, ControllerFactory controllerFactory) {
        this.fxmlLoaderService = fxmlLoaderService;
        this.controllerFactory = controllerFactory;
    }

    @Override
    public void setData(Organization data) {
        this.organization = data;

        setupListeners();
        loadUpdateConfirmDialog();
        loadDeleteConfirmDialog();
    }

    private void setupListeners() {
        Consumer<CustomRole> onConfirmDelete = this::deleteObject;
        Runnable onCancelDelete = this::cancelDeleteObject;

        confirmDialogDeleteListener = new RunnableConfirmDialogActionListener<>(onConfirmDelete, onCancelDelete);

        Consumer<CustomRole> onConfirmUpdate = this::updateObject;
        Runnable onCancelUpdate = this::cancelUpdateObject;

        confirmDialogUpdateListener = new RunnableConfirmDialogActionListener<>(onConfirmUpdate, onCancelUpdate);
    }

    private void loadUpdateConfirmDialog() {
        // Load confirm dialog
        FXMLLoader loader = fxmlLoaderService.setUpLoader("/org/chainoptim/desktop/shared/confirmdialog/GenericConfirmDialogView.fxml", controllerFactory::createController);

        ConfirmDialogInput confirmDialogInput = new ConfirmDialogInput("Confirm Custom Role Update", "Are you sure you want to update this role?", "/org/chainoptim/desktop/core/user/UsersListByCustomRoleView.fxml");
        CustomRole customRole = new CustomRole();
        customRole.setId(1);

        try {
            Node view = loader.load();
            GenericConfirmDialogController<CustomRole> controller = loader.getController();
            controller.setData(customRole, confirmDialogInput);
            controller.setActionListener(confirmDialogUpdateListener);
            updateConfirmDialogPane.getChildren().add(view);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void loadDeleteConfirmDialog() {
        // Load confirm dialog
        FXMLLoader loader = fxmlLoaderService.setUpLoader("/org/chainoptim/desktop/shared/confirmdialog/GenericConfirmDialogView.fxml", controllerFactory::createController);

        ConfirmDialogInput confirmDialogInput = new ConfirmDialogInput("Confirm Custom Role Delete", "Are you sure you want to delete this role?", "/org/chainoptim/desktop/core/user/UsersListByCustomRoleView.fxml");
        CustomRole customRole = new CustomRole();
        customRole.setId(2);

        try {
            Node view = loader.load();
            GenericConfirmDialogController<CustomRole> controller = loader.getController();
            controller.setData(customRole, confirmDialogInput);
            controller.setActionListener(confirmDialogDeleteListener);
            deleteConfirmDialogPane.getChildren().add(view);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void deleteObject(CustomRole customRole) {
        System.out.println("Delete object" + customRole.getId());
    }

    private void cancelDeleteObject() {
        System.out.println("Cancel delete object");
    }

    private void updateObject(CustomRole customRole) {
        System.out.println("Update object" + customRole.getId());
    }

    private void cancelUpdateObject() {
        System.out.println("Cancel update object");
    }

}
