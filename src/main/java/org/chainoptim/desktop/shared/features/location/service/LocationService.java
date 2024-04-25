package org.chainoptim.desktop.shared.features.location.service;

import org.chainoptim.desktop.shared.features.location.dto.CreateLocationDTO;
import org.chainoptim.desktop.shared.features.location.dto.UpdateLocationDTO;
import org.chainoptim.desktop.shared.features.location.model.Location;
import org.chainoptim.desktop.shared.httphandling.Result;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface LocationService {

    CompletableFuture<Result<List<Location>>> getLocationsByOrganizationId(Integer organizationId);
    CompletableFuture<Result<Location>> createLocation(CreateLocationDTO locationDTO);
    CompletableFuture<Result<Location>> updateLocation(UpdateLocationDTO locationDTO);
    CompletableFuture<Result<Integer>> deleteLocation(Integer id);
}
