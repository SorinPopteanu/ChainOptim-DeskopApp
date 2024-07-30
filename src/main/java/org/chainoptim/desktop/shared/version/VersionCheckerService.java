package org.chainoptim.desktop.shared.version;

import org.chainoptim.desktop.shared.httphandling.Result;

import java.util.concurrent.CompletableFuture;

public interface VersionCheckerService {

    CompletableFuture<Result<CheckVersionResponse>> checkVersion(String currentVersion);
}
