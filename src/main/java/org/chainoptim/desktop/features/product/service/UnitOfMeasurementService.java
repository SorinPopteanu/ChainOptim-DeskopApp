package org.chainoptim.desktop.features.product.service;

import org.chainoptim.desktop.features.product.dto.CreateUnitOfMeasurementDTO;
import org.chainoptim.desktop.features.product.dto.UpdateUnitOfMeasurementDTO;
import org.chainoptim.desktop.features.product.model.UnitOfMeasurement;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface UnitOfMeasurementService {

    CompletableFuture<Optional<List<UnitOfMeasurement>>> getUnitsOfMeasurementByOrganizationId(Integer organizationId);
    CompletableFuture<Optional<UnitOfMeasurement>> createUnitOfMeasurement(CreateUnitOfMeasurementDTO unitDTO);
    CompletableFuture<Optional<UnitOfMeasurement>> updateUnitOfMeasurement(UpdateUnitOfMeasurementDTO unitDTO);
    CompletableFuture<Optional<Integer>> deleteUnitOfMeasurement(Integer id);
}
