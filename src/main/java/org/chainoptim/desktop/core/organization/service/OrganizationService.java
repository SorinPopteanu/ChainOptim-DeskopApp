package org.chainoptim.desktop.core.organization.service;

import org.chainoptim.desktop.core.organization.model.Organization;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.shared.httphandling.Result;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface OrganizationService {

    CompletableFuture<Result<Organization>> getOrganizationById(Integer organizationId, boolean includeUsers);

}
