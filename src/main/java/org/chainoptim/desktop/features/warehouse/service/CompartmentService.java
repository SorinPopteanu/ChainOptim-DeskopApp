package org.chainoptim.desktop.features.warehouse.service;

import org.chainoptim.desktop.features.warehouse.model.Compartment;
import org.chainoptim.desktop.shared.httphandling.Result;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface CompartmentService {

    CompletableFuture<Result<List<Compartment>>> getCompartmentsByWarehouseId(Integer warehouseId);
}
