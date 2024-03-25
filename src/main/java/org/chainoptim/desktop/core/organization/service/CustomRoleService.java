package org.chainoptim.desktop.core.organization.service;

import org.chainoptim.desktop.core.organization.dto.CreateCustomRoleDTO;
import org.chainoptim.desktop.core.organization.dto.UpdateCustomRoleDTO;
import org.chainoptim.desktop.core.organization.model.CustomRole;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface CustomRoleService {

    CompletableFuture<Optional<List<CustomRole>>> getCustomRolesByOrganizationId(Integer organizationId);
    CompletableFuture<Optional<CustomRole>> createCustomRole(CreateCustomRoleDTO roleDTO);
    CompletableFuture<Optional<CustomRole>> updateCustomRole(UpdateCustomRoleDTO roleDTO);
    CompletableFuture<Optional<Integer>> deleteCustomRole(Integer roleId);
}
