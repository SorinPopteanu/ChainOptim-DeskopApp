package org.chainoptim.desktop.shared.features.location.service;

import org.chainoptim.desktop.shared.features.location.dto.CreateLocationDTO;
import org.chainoptim.desktop.shared.features.location.dto.UpdateLocationDTO;
import org.chainoptim.desktop.shared.features.location.model.Location;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface LocationService {

    CompletableFuture<Optional<List<Location>>> getLocationsByOrganizationId(Integer organizationId);
    CompletableFuture<Optional<Location>> createLocation(CreateLocationDTO locationDTO);
    CompletableFuture<Optional<Location>> updateLocation(UpdateLocationDTO locationDTO);
    CompletableFuture<Optional<Integer>> deleteLocation(Integer id);
}
