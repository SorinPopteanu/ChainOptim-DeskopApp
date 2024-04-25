package org.chainoptim.desktop.shared.caching;

public interface CachingService<T> {

    void add(String key, T data, float staleTime);
    T get(String key);
    boolean isCached(String key);
    boolean isStale(String key);
    void remove(String key);
    void clear();
}
