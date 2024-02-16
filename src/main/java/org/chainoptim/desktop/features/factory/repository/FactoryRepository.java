package org.chainoptim.desktop.features.factory.repository;

import org.chainoptim.desktop.features.factory.model.Factory;
import org.chainoptim.desktop.features.product.model.Product;

import java.util.List;
import java.util.Optional;

public interface FactoryRepository {

    public Optional<List<Factory>> getFactoriesByOrganizationId(Integer organizationId);
    public Optional<Factory> getFactoryById(Integer factoryId);
}
