package org.chainoptim.desktop.features.productpipeline.repository;

import org.chainoptim.desktop.features.factory.model.Factory;
import org.chainoptim.desktop.features.productpipeline.model.Component;

import java.util.List;
import java.util.Optional;

public interface ComponentRepository {

    public Optional<List<Component>> getComponentsByOrganizationId(Integer organizationId);
}
