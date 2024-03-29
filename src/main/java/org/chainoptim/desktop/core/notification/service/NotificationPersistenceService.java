package org.chainoptim.desktop.core.notification.service;

import org.chainoptim.desktop.core.notification.model.Notification;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface NotificationPersistenceService {

    CompletableFuture<Optional<List<Notification>>> getNotificationsByUserId(String userId);
}
