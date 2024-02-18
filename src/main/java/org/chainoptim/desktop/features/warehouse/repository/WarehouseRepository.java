package org.chainoptim.desktop.features.warehouse.repository;

import org.chainoptim.desktop.features.warehouse.model.Warehouse;

import java.util.List;
import java.util.Optional;

public interface WarehouseRepository {

    public Optional<List<Warehouse>> getWarehousesByOrganizationId(Integer organizationId);
    public Optional<Warehouse> getWarehouseById(Integer warehouseId);
}
