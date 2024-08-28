package org.chainoptim.desktop.core.tenant.organization.service;

import org.chainoptim.desktop.core.tenant.organization.dto.UpdateOrganizationDTO;
import org.chainoptim.desktop.core.tenant.organization.model.Organization;
import org.chainoptim.desktop.shared.httphandling.Result;

import java.util.concurrent.CompletableFuture;

public interface OrganizationService {

    CompletableFuture<Result<Organization>> getOrganizationById(Integer organizationId, boolean includeUsers);
    CompletableFuture<Result<Organization>> updateOrganization(UpdateOrganizationDTO organizationDTO);

}
