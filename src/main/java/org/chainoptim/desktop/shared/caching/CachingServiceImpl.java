package org.chainoptim.desktop.shared.caching;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CachingServiceImpl<T> implements CachingService<T> {

    private final Map<String, CachedData<T>> cache;
    private final ScheduledExecutorService executorService;

    public CachingServiceImpl() {
        cache = new ConcurrentHashMap<>();
        executorService = Executors.newSingleThreadScheduledExecutor();
        scheduleCacheCleanup();
    }

    public void add(String key, T data, long staleTime) {
        cache.put(key, new CachedData<>(data, staleTime));
    }

    public T get(String key) {
        CachedData<T> cachedData = cache.get(key);
        if (cachedData == null || cachedData.isStale()) {
            return null;
        }
        return cachedData.getData();
    }

    public boolean isCached(String key) {
        return cache.containsKey(key);
    }

    public boolean isStale(String key) {
        CachedData<T> cachedData = cache.get(key);
        return cachedData == null || cachedData.isStale();
    }

    public void remove(String key) {
        cache.remove(key);
    }

    public void clear() {
        cache.clear();
    }

    // Cleanup Scheduled Service
    private void scheduleCacheCleanup() {
        executorService.scheduleAtFixedRate(this::cleanupStaleEntries, 20, 20, TimeUnit.MINUTES);
    }

    private void cleanupStaleEntries() {
        cache.entrySet().removeIf(entry -> entry.getValue().isStale());
    }

    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }

}
