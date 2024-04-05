package org.chainoptim.desktop.shared.caching;

import java.util.HashMap;
import java.util.Map;

public class CachingServiceImpl<T> implements CachingService<T> {

    private final Map<String, CachedData<T>> cache = new HashMap<>();

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


}
