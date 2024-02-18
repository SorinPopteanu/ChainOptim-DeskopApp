package org.chainoptim.desktop.features.product.repository;

import org.chainoptim.desktop.features.factory.model.Factory;
import org.chainoptim.desktop.features.product.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    public Optional<List<Product>> getProductsByOrganizationId(Integer organizationId);
    public Optional<Product> getProductWithStages(Integer productId);
}
