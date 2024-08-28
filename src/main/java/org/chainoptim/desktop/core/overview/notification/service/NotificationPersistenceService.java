package org.chainoptim.desktop.core.overview.notification.service;

import org.chainoptim.desktop.core.overview.notification.model.NotificationUser;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.search.model.PaginatedResults;
import org.chainoptim.desktop.shared.search.model.SearchParams;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface NotificationPersistenceService {

    CompletableFuture<Result<List<NotificationUser>>> getNotificationsByUserId(String userId);

    CompletableFuture<Result<PaginatedResults<NotificationUser>>> getNotificationsByUserIdAdvanced(String userId, SearchParams searchParams);
}
