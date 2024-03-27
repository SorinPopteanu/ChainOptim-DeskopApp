package org.chainoptim.desktop.core.user.service;

import org.chainoptim.desktop.core.user.model.User;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface UserService {
    CompletableFuture<Optional<User>> getUserByUsername(String username);
    CompletableFuture<Optional<List<User>>> getUsersByCustomRoleId(Integer customRoleId);
    CompletableFuture<Optional<User>> assignBasicRoleToUser(String userId, User.Role role);
    CompletableFuture<Optional<User>> assignCustomRoleToUser(String userId, Integer roleId);
    CompletableFuture<Optional<User>> removeUserFromOrganization(String userId, Integer organizationId);
}
