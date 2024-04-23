package org.chainoptim.desktop.features.product.service;

import org.chainoptim.desktop.features.product.dto.CreateUnitOfMeasurementDTO;
import org.chainoptim.desktop.features.product.dto.UpdateUnitOfMeasurementDTO;
import org.chainoptim.desktop.features.product.model.UnitOfMeasurement;
import org.chainoptim.desktop.shared.httphandling.Result;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface UnitOfMeasurementService {

    CompletableFuture<Result<List<UnitOfMeasurement>>> getUnitsOfMeasurementByOrganizationId(Integer organizationId);
    CompletableFuture<Result<UnitOfMeasurement>> createUnitOfMeasurement(CreateUnitOfMeasurementDTO unitDTO);
    CompletableFuture<Result<UnitOfMeasurement>> updateUnitOfMeasurement(UpdateUnitOfMeasurementDTO unitDTO);
    CompletableFuture<Result<Integer>> deleteUnitOfMeasurement(Integer id);
}
