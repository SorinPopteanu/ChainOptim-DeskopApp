package org.chainoptim.desktop.shared.table.util;

import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.features.goods.component.dto.ComponentsSearchDTO;
import org.chainoptim.desktop.features.goods.component.service.ComponentService;
import org.chainoptim.desktop.shared.httphandling.Result;
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

    private Result<List<ComponentsSearchDTO>> handleComponentsResponse(Result<List<ComponentsSearchDTO>> result) {
        Platform.runLater(() -> {
            if (result.getError() != null) {
                return;
            }
            componentsMap.clear();
            result.getData().forEach(component -> componentsMap.put(component.getId(), component.getName()));
        });
        return result;
    }

    private Result<List<ComponentsSearchDTO>> handleComponentsException(Throwable ex) {
        return new Result<>();
    }
}
