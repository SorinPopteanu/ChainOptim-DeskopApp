package org.chainoptim.desktop.shared.common.uielements;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.productpipeline.dto.StagesSearchDTO;
import org.chainoptim.desktop.features.productpipeline.service.StageService;

import com.google.inject.Inject;
import javafx.application.Platform;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SelectStageController {

    private final StageService stageService;

    @FXML
    private ComboBox<StagesSearchDTO> stageComboBox;

    @Inject
    public SelectStageController(StageService stageService) {
        this.stageService = stageService;
    }

    public void initialize() {
        loadStages();
    }

    public StagesSearchDTO getSelectedStage() {
        return stageComboBox.getSelectionModel().getSelectedItem();
    }

    private void loadStages() {
        stageComboBox.setCellFactory(lv -> new ListCell<StagesSearchDTO>() {
            @Override
            protected void updateItem(StagesSearchDTO item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getName());
            }
        });

        stageComboBox.setButtonCell(new ListCell<StagesSearchDTO>() {
            @Override
            protected void updateItem(StagesSearchDTO item, boolean empty) {
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
        stageService.getStagesByOrganizationIdSmall(organizationId)
                .thenApply(this::handleStagesResponse)
                .exceptionally(this::handleStagesException);
    }

    private Optional<List<StagesSearchDTO>> handleStagesResponse(Optional<List<StagesSearchDTO>> stagesOptional) {
        Platform.runLater(() -> {
            if (stagesOptional.isEmpty()) {
                return;
            }
            stageComboBox.getItems().setAll(stagesOptional.get());

        });
        return stagesOptional;
    }

    private Optional<List<StagesSearchDTO>> handleStagesException(Throwable ex) {
        return Optional.empty();
    }
}
