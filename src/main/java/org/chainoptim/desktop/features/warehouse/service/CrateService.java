package org.chainoptim.desktop.features.warehouse.service;

import org.chainoptim.desktop.features.warehouse.model.Compartment;
import org.chainoptim.desktop.features.warehouse.model.Crate;
import org.chainoptim.desktop.shared.httphandling.Result;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface CrateService {

    CompletableFuture<Result<List<Crate>>> getCratesByOrganizationId(Integer organizationId);
}
