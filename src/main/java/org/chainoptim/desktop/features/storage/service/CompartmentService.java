package org.chainoptim.desktop.features.storage.service;

import org.chainoptim.desktop.features.storage.dto.CreateCompartmentDTO;
import org.chainoptim.desktop.features.storage.dto.UpdateCompartmentDTO;
import org.chainoptim.desktop.features.storage.model.Compartment;
import org.chainoptim.desktop.shared.httphandling.Result;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface CompartmentService {

    CompletableFuture<Result<List<Compartment>>> getCompartmentsByWarehouseId(Integer warehouseId);
    CompletableFuture<Result<Compartment>> createCompartment(CreateCompartmentDTO compartmentDTO);
    CompletableFuture<Result<Compartment>> updateCompartment(UpdateCompartmentDTO compartmentDTO);
    CompletableFuture<Result<Integer>> deleteCompartment(Integer compartmentId);
}
