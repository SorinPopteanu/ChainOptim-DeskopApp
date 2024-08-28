package org.chainoptim.desktop.features.storage.crate.service;

import org.chainoptim.desktop.features.storage.crate.model.Crate;
import org.chainoptim.desktop.shared.httphandling.Result;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface CrateService {

    CompletableFuture<Result<List<Crate>>> getCratesByOrganizationId(Integer organizationId);
}
