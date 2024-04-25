package org.chainoptim.desktop.core.user.service;

import org.chainoptim.desktop.core.user.dto.UserSearchResultDTO;
import org.chainoptim.desktop.core.user.model.User;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface UserService {
    // Read
    CompletableFuture<Result<User>> getUserByUsername(String username);
    CompletableFuture<Result<List<User>>> getUsersByCustomRoleId(Integer customRoleId);
    CompletableFuture<Result<PaginatedResults<UserSearchResultDTO>>> searchPublicUsers(String searchQuery, int page, int itemsPerPage);
    // Write
    CompletableFuture<Result<User>> assignBasicRoleToUser(String userId, User.Role role);
    CompletableFuture<Result<User>> assignCustomRoleToUser(String userId, Integer roleId);
    CompletableFuture<Result<User>> removeUserFromOrganization(String userId, Integer organizationId);
}
