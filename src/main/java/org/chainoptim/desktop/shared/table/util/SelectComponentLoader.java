package org.chainoptim.desktop.shared.table.util;

import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.productpipeline.dto.ComponentsSearchDTO;
import org.chainoptim.desktop.features.productpipeline.service.ComponentService;
import com.google.inject.Inject;
import javafx.application.Platform;

import java.util.*;

public class SelectComponentLoader {


    private final ComponentService componentService;
    private final Map<Integer, String> componentsMap = new HashMap<>();

    @Inject
    public SelectComponentLoader(ComponentService componentService) {
        this.componentService = componentService;
    }

    public void initialize() {
        loadComponents();
    }

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
            componentsMap.clear();
            componentsOptional.get().forEach(component -> componentsMap.put(component.getId(), component.getName()));
        });
        return componentsOptional;
    }

    private Optional<List<ComponentsSearchDTO>> handleComponentsException(Throwable ex) {
        return Optional.empty();
    }
}
