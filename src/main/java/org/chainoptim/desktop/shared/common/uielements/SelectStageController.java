package org.chainoptim.desktop.shared.common.uielements;

import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.productpipeline.dto.StagesSearchDTO;
import org.chainoptim.desktop.features.productpipeline.service.StageService;

import com.google.inject.Inject;
import javafx.application.Platform;

import java.util.List;
import java.util.Optional;

public class SelectStageController {

    private final StageService stageService;

    @Inject
    public SelectStageController(StageService stageService) {
        this.stageService = stageService;
    }

    public void initialize() {
        System.out.println("Initializing");
        loadStages();
    }

    private void loadStages() {
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
            List<StagesSearchDTO> stages = stagesOptional.get();
            System.out.println("Stages: " + stages);

            if (!stages.isEmpty()) {
                for (StagesSearchDTO stage : stages) {
//                    Platform.runLater(() -> pageSelectorController.initialize(totalCount));
                }
            }

        });
        return stagesOptional;
    }

    private Optional<List<StagesSearchDTO>> handleStagesException(Throwable ex) {
        return Optional.empty();
    }
}
