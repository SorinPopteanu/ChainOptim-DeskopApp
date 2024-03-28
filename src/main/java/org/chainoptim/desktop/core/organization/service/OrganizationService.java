package org.chainoptim.desktop.core.organization.service;

import org.chainoptim.desktop.core.organization.model.Organization;
import org.chainoptim.desktop.core.user.model.User;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface OrganizationService {

    CompletableFuture<Optional<Organization>> getOrganizationById(Integer organizationId, boolean includeUsers);

}
