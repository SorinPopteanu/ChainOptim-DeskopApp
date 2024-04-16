package org.chainoptim.desktop.shared.common.uielements;

import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.productpipeline.dto.ComponentsSearchDTO;
import org.chainoptim.desktop.features.productpipeline.service.ComponentService;
import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import java.util.*;

public class SelectComponentController {

    private final ComponentService componentService;
    private Map<Integer, String> componentsMap = new HashMap<>();

//    @FXML
//    private ComboBox<ComponentsSearchDTO> componentComboBox;

    @Inject
    public SelectComponentController(ComponentService componentService) {
        this.componentService = componentService;
    }

    public void initialize() {
        loadComponents();
    }

//    public ComponentsSearchDTO getSelectedComponent() {
//        return componentComboBox.getSelectionModel().getSelectedItem();
//    }

    public List<String> getComponentsName() {
        return new ArrayList<>(componentsMap.values());
    }

    public Integer getComponentIdByName(String name) {
        for (Map.Entry<Integer, String> entry : componentsMap.entrySet()) {
            if (entry.getValue().equals(name)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void loadComponents() {
//        componentComboBox.setCellFactory(lv -> new ListCell<ComponentsSearchDTO>() {
//            @Override
//            protected void updateItem(ComponentsSearchDTO item, boolean empty) {
//                super.updateItem(item, empty);
//                setText(empty ? "" : item.getName());
//            }
//        });
//
//        componentComboBox.setButtonCell(new ListCell<ComponentsSearchDTO>() {
//            @Override
//            protected void updateItem(ComponentsSearchDTO item, boolean empty) {
//                super.updateItem(item, empty);
//                if (empty || item == null) {
//                    setText(null);
//                } else {
//                    setText(item.getName());
//                }
//            }
//        });

        User currentUser = TenantContext.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        Integer organizationId = currentUser.getOrganization().getId();
        componentService.getComponentsByOrganizationIdSmall(organizationId)
                .thenApply(this::handleComponentsResponse)
                .exceptionally(this::handleComponentsException);
    }

    private Optional<List<ComponentsSearchDTO>> handleComponentsResponse(Optional<List<ComponentsSearchDTO>> componentsOptional) {
        Platform.runLater(() -> {
            if (componentsOptional.isEmpty()) {
                return;
            }
//            componentComboBox.getItems().setAll(componentsOptional.get());
            componentsMap.clear();
            componentsOptional.get().forEach(component -> componentsMap.put(component.getId(), component.getName()));
        });
        return componentsOptional;
    }

    private Optional<List<ComponentsSearchDTO>> handleComponentsException(Throwable ex) {
        return Optional.empty();
    }
}
