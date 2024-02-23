package org.chainoptim.desktop.core.user.repository;

import org.chainoptim.desktop.core.user.model.User;

import java.io.UnsupportedEncodingException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface UserRepository {
    public CompletableFuture<Optional<User>> getUserByUsername(String username) throws UnsupportedEncodingException;
}
