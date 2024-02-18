package org.chainoptim.desktop.features.supplier.repository;

import org.chainoptim.desktop.features.supplier.model.Supplier;

import java.util.List;
import java.util.Optional;

public interface SupplierRepository {

    public Optional<List<Supplier>> getSuppliersByOrganizationId(Integer organizationId);
    public Optional<Supplier> getSupplierById(Integer supplierId);
}
