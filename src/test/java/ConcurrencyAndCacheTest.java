package com.ticket;

import com.ticket.domain.Reservation;
import com.ticket.domain.Seat;
import com.ticket.exception.InvalidSeatException;
import com.ticket.service.LruCache;
import com.ticket.service.TicketingService;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class ConcurrencyAndCacheTest {

    @Test
    void testParallelRaceConditionOnlyOneSucceeds() throws InterruptedException {
        // Arrange
        TicketingService ticketingService = new TicketingService();
        Seat targetSeat = new Seat("S-99", "V-01", "Orchestra", "Row-A", 1, Map.of());

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endGate = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // Act
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    startGate.await(); // Sync threads to strike at the exact same moment
                    ticketingService.placeHold("E-01", targetSeat, "user" + index + "@test.com", 10);
                    successCount.incrementAndGet();
                } catch (InvalidSeatException e) {
                    failureCount.incrementAndGet();
                } catch (Exception e) {
                    fail("Unexpected runtime interruption");
                } finally {
                    endGate.countDown();
                }
            });
        }

        startGate.countDown(); // Fire race condition signal
        endGate.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        // Assert rules: Exactly 1 worker wins, 9 fail with proper domain boundaries
        assertEquals(1, successCount.get(), "Only exactly one client can secure a hold transaction.");
        assertEquals(9, failureCount.get(), "Remaining 9 parallel threads must fail deterministically.");
        ticketingService.shutdown();
    }

    @Test
    void testLruCacheEvictionMetrics() {
        // Arrange (Cache capacity capped at 3 items)
        LruCache<String, String> cache = new LruCache<>(3);

        // Act
        cache.put("A", "Data-A");
        cache.put("B", "Data-B");
        cache.put("C", "Data-C");

        // Touch item A to move it to the top of recent usage stack
        cache.get("A");

        // Push new item to break capacity barrier
        cache.put("D", "Data-D");

        // Assert: Item B should be evicted since it was the oldest untouched element
        assertTrue(cache.get("A").isPresent());
        assertTrue(cache.get("C").isPresent());
        assertTrue(cache.get("D").isPresent());
        assertFalse(cache.get("B").isPresent(), "Item B should be evicted by LRU constraints.");
    }
}
