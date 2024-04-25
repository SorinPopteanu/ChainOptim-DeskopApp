package org.chainoptim.desktop.shared.caching;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
public class CachedData<T> {

    private T data;
    private float staleTimeMillis;
    private Instant cachedAt;

    public CachedData(T data, float staleTime) {
        this.data = data;
        this.staleTimeMillis = staleTime * 1000;
        this.cachedAt = Instant.now();
    }

    public boolean isStale() {
        return Instant.now().isAfter(cachedAt.plusMillis((long) staleTimeMillis));
    }

}
