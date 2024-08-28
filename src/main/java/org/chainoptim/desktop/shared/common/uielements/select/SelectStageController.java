package org.chainoptim.desktop.shared.common.uielements.select;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.goods.stage.dto.StagesSearchDTO;
import org.chainoptim.desktop.features.goods.stage.service.StageService;
import org.chainoptim.desktop.shared.httphandling.Result;

import com.google.inject.Inject;
import javafx.application.Platform;

import java.util.List;

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

    private Result<List<StagesSearchDTO>> handleStagesResponse(Result<List<StagesSearchDTO>> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                return;
            }
            stageComboBox.getItems().setAll(result.getData());

        });
        return result;
    }

    private Result<List<StagesSearchDTO>> handleStagesException(Throwable ex) {
        return new Result<>();
    }
}
