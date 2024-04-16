package org.chainoptim.desktop.shared.common.uielements.select;

import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.factory.dto.FactoriesSearchDTO;
import org.chainoptim.desktop.features.factory.service.FactoryService;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;

import java.util.List;
import java.util.Optional;

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

    private Optional<List<FactoriesSearchDTO>> handleFactoriesResponse(Optional<List<FactoriesSearchDTO>> factoriesOptional) {
        Platform.runLater(() -> {
            if (factoriesOptional.isEmpty()) {
                return;
            }
            factoryComboBox.getItems().setAll(factoriesOptional.get());

        });
        return factoriesOptional;
    }

    private Optional<List<FactoriesSearchDTO>> handleFactoriesException(Throwable ex) {
        return Optional.empty();
    }
}
