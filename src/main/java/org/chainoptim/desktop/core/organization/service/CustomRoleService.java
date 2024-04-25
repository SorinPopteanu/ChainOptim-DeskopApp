package org.chainoptim.desktop.core.organization.service;

import org.chainoptim.desktop.core.organization.dto.CreateCustomRoleDTO;
import org.chainoptim.desktop.core.organization.dto.UpdateCustomRoleDTO;
import org.chainoptim.desktop.core.organization.model.CustomRole;
import org.chainoptim.desktop.shared.httphandling.Result;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface CustomRoleService {

    CompletableFuture<Result<List<CustomRole>>> getCustomRolesByOrganizationId(Integer organizationId);
    CompletableFuture<Result<CustomRole>> createCustomRole(CreateCustomRoleDTO roleDTO);
    CompletableFuture<Result<CustomRole>> updateCustomRole(UpdateCustomRoleDTO roleDTO);
    CompletableFuture<Result<Integer>> deleteCustomRole(Integer roleId);
}
