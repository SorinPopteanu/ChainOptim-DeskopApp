package org.chainoptim.desktop.core.organization.controller;

import org.chainoptim.desktop.core.abstraction.ControllerFactory;
import org.chainoptim.desktop.core.organization.model.CustomRole;
import org.chainoptim.desktop.core.organization.model.Organization;
import org.chainoptim.desktop.shared.confirmdialog.controller.GenericConfirmDialogActionListener;
import org.chainoptim.desktop.shared.confirmdialog.controller.GenericConfirmDialogController;
import org.chainoptim.desktop.shared.confirmdialog.model.ConfirmDialogInput;
import org.chainoptim.desktop.shared.util.DataReceiver;
import org.chainoptim.desktop.shared.util.resourceloader.FXMLLoaderService;
import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class OrganizationSubscriptionPlanController implements DataReceiver<Organization>, GenericConfirmDialogActionListener<CustomRole> {

    private final FXMLLoaderService fxmlLoaderService;
    private final ControllerFactory controllerFactory;

    private Organization organization;

    @FXML
    private StackPane confirmDialogPane;

    @Inject
    public OrganizationSubscriptionPlanController(FXMLLoaderService fxmlLoaderService, ControllerFactory controllerFactory) {
        this.fxmlLoaderService = fxmlLoaderService;
        this.controllerFactory = controllerFactory;
    }

    @Override
    public void setData(Organization data) {
        this.organization = data;

        loadConfirmDialog();
    }

    private void loadConfirmDialog() {
        // Load confirm dialog
        FXMLLoader loader = fxmlLoaderService.setUpLoader("/org/chainoptim/desktop/shared/confirmdialog/GenericConfirmDialogView.fxml", controllerFactory::createController);

        ConfirmDialogInput confirmDialogInput = new ConfirmDialogInput("Subscription Plan", "Please select a subscription plan Please select a subscription plan Please select a subscription plan Please select a subscription plan Please select a subscription plan Please select a subscription plan Please select a subscription plan", "/org/chainoptim/desktop/core/user/UsersListByCustomRoleView.fxml");
        CustomRole customRole = new CustomRole();
        customRole.setId(1);

        try {
            Node view = loader.load();
            GenericConfirmDialogController<CustomRole> controller = loader.getController();
            controller.setData(customRole, confirmDialogInput);
            controller.setActionListener(this);
            confirmDialogPane.getChildren().add(view);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onConfirmAction(CustomRole data) {
        System.out.println("Confirm action" + data.getId());
    }

    @Override
    public void onCancelAction() {
        System.out.println("Cancel action");
    }
}
