package org.chainoptim.desktop.features.productpipeline.service;

import org.chainoptim.desktop.features.productpipeline.dto.ComponentsSearchDTO;
import org.chainoptim.desktop.features.productpipeline.dto.CreateComponentDTO;
import org.chainoptim.desktop.features.productpipeline.model.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ComponentService {

    CompletableFuture<Optional<List<Component>>> getComponentsByOrganizationId(Integer organizationId);

    CompletableFuture<Optional<List<ComponentsSearchDTO>>> getComponentsByOrganizationIdSmall(Integer organizationId);

    CompletableFuture<Optional<Component>> createComponent(CreateComponentDTO componentDTO);
}
