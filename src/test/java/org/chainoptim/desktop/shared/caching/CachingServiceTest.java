package org.chainoptim.desktop.shared.caching;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CachingServiceTest {

    private CachingServiceImpl<String> cachingService;

    @BeforeEach
    void setUp() {
        cachingService = new CachingServiceImpl<>();
    }

    @Test
    void testAdd() {
        // Arrange
        String key = "testKey";
        String value = "testValue";

        // Act
        cachingService.add(key, value, 1000);

        // Assert
        assertEquals(value, cachingService.get(key));
    }

    @Test
    void testIsStale() throws InterruptedException {
        // Arrange
        String key = "testKey";
        String value = "testValue";
        float staleTimeSeconds = 0.05f;
        long waitTime = (long) (staleTimeSeconds * 1000 + 100);

        // Act
        cachingService.add(key, value, staleTimeSeconds);
        Thread.sleep(waitTime);

        // Assert
        assertTrue(cachingService.isStale(key));
    }

    @Test
    void testRemove() {
        // Arrange
        String key = "testKey";
        String value = "testValue";

        // Act
        cachingService.add(key, value, 1000);
        cachingService.remove(key);

        // Assert
        assertNull(cachingService.get(key));
    }

    @Test
    void testCleanupTask() throws InterruptedException {
        // Arrange
        String key = "testKey";
        String value = "testValue";
        float staleTimeSeconds = 0.05f;
        long waitTime = (long) (staleTimeSeconds * 1000 + 100);

        // Act
        cachingService.add(key, value, staleTimeSeconds);
        Thread.sleep(waitTime);
        cachingService.cleanupStaleEntries();

        // Assert
        assertFalse(cachingService.isCached(key));
    }

    @Test
    void testShutdown() {
        // Act
        cachingService.shutdown();

        // Assert
        assertTrue(cachingService.getExecutorService().isShutdown());
    }
}
