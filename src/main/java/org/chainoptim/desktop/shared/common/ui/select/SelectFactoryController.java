package org.chainoptim.desktop.shared.common.ui.select;

import org.chainoptim.desktop.core.main.context.TenantContext;
import org.chainoptim.desktop.core.tenant.user.model.User;
import org.chainoptim.desktop.features.production.factory.dto.FactoriesSearchDTO;
import org.chainoptim.desktop.features.production.factory.service.FactoryService;
import org.chainoptim.desktop.shared.httphandling.Result;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;

import java.util.List;

public class SelectFactoryController {

    private final FactoryService factoryService;

    @FXML
    private ComboBox<FactoriesSearchDTO> factoryComboBox;

    @Inject
    public SelectFactoryController(FactoryService factoryService) {
        this.factoryService = factoryService;
    }

    public void initialize() {
        loadFactories();
    }

    public FactoriesSearchDTO getSelectedFactory() {
        return factoryComboBox.getSelectionModel().getSelectedItem();
    }

    private void loadFactories() {
        factoryComboBox.setCellFactory(lv -> new ListCell<FactoriesSearchDTO>() {
            @Override
            protected void updateItem(FactoriesSearchDTO item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getName());
            }
        });

        factoryComboBox.setButtonCell(new ListCell<FactoriesSearchDTO>() {
            @Override
            protected void updateItem(FactoriesSearchDTO item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });

        User currentUser = TenantContext.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        Integer organizationId = currentUser.getOrganization().getId();
        factoryService.getFactoriesByOrganizationIdSmall(organizationId)
                .thenApply(this::handleFactoriesResponse)
                .exceptionally(this::handleFactoriesException);
    }

    private Result<List<FactoriesSearchDTO>> handleFactoriesResponse(Result<List<FactoriesSearchDTO>> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                return;
            }
            factoryComboBox.getItems().setAll(result.getData());

        });
        return result;
    }

    private Result<List<FactoriesSearchDTO>> handleFactoriesException(Throwable ex) {
        return new Result<>();
    }
}
