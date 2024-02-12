package org.chainoptim.desktop.core.user.repository;

import org.chainoptim.desktop.core.user.model.User;

import java.util.Optional;

public interface UserRepository {
    public Optional<User> getUserByUsername(String username);
}
