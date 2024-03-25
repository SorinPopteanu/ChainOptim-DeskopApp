package org.chainoptim.desktop.core.user.service;

import org.chainoptim.desktop.core.user.model.User;

import java.io.UnsupportedEncodingException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface UserService {
    CompletableFuture<Optional<User>> getUserByUsername(String username) throws UnsupportedEncodingException;
}
