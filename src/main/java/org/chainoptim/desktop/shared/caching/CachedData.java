package org.chainoptim.desktop.shared.caching;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
public class CachedData<T> {

    private T data;
    private long staleTime; // Seconds
    private LocalDateTime cachedAt;

    public CachedData(T data, long staleTime) {
        this.data = data;
        this.staleTime = staleTime;
        this.cachedAt = LocalDateTime.now();
    }

    public boolean isStale() {
        return LocalDateTime.now().isAfter(cachedAt.plusSeconds(staleTime));
    }


}
