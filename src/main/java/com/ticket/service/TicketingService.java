package com.ticket.service;

import com.ticket.domain.*;
import com.ticket.exception.InvalidSeatException;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class TicketingService {
    // Thread-safe memory tables to mitigate data race conditions
    private final Map<String, Reservation> reservationTable = new ConcurrentHashMap<>();
    private final Map<String, ReservationSeat> seatAssignmentTable = new ConcurrentHashMap<>();

    // Per-Event structural lock registry to eliminate double-booking window vulnerabilities
    private final Map<String, ReentrantLock> eventLocks = new ConcurrentHashMap<>();

    // Background single-threaded scheduler engine to manage expired holds eviction
    private final ScheduledExecutorService backgroundSweeper;

    public TicketingService() {
        this.backgroundSweeper = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "Hold-Sweeper-Thread");
            thread.setDaemon(true); // Allows the CLI application context to close without blocking
            return thread;
        });

        // Start background eviction loops every 1 second
        this.backgroundSweeper.scheduleAtFixedRate(this::purgeExpiredHolds, 1, 1, TimeUnit.SECONDS);
    }

    private ReentrantLock getLockForEvent(String eventId) {
        return eventLocks.computeIfAbsent(eventId, k -> new ReentrantLock());
    }

    /**
     * Secures a thread-safe seat hold using isolated per-event boundaries.
     */
    public Reservation placeHold(String eventId, Seat seat, String customerEmail, long ttlSeconds) {
        ReentrantLock eventLock = getLockForEvent(eventId);

        // Block competing worker threads gracefully to ensure mutual exclusion
        eventLock.lock();
        try {
            // Business rule validation: check if seat is already bound to an active transaction
            boolean seatIsBlocked = seatAssignmentTable.values().stream()
                    .anyMatch(rs -> rs.seatId().equals(seat.id()) && isReservationActive(rs.reservationId()));

            if (seatIsBlocked) {
                throw new InvalidSeatException("Transaction conflict: Target seat code is currently reserved or locked.");
            }

            String resId = "RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            Instant now = Instant.now();
            Instant expiresAt = now.plusSeconds(ttlSeconds);

            Reservation hold = new Reservation(resId, eventId, customerEmail, ReservationStatus.HOLD, now, null, expiresAt);
            ReservationSeat holdSeat = new ReservationSeat(resId, seat.id(), Money.of(120.00), "NONE");

            reservationTable.put(resId, hold);
            seatAssignmentTable.put(resId, holdSeat);

            return hold;
        } finally {
            eventLock.unlock(); // Always release lock inside a finally block to prevent thread deadlocks
        }
    }

    private boolean isReservationActive(String reservationId) {
        Reservation res = reservationTable.get(reservationId);
        if (res == null) return false;
        if (res.status() == ReservationStatus.CANCELLED) return false;
        // If it's a hold but expired, it's inactive
        return !res.isExpired();
    }

    /**
     * Concurrency Sweeper background runner loop task
     */
    private void purgeExpiredHolds() {
        Instant executionTime = Instant.now();
        reservationTable.values().stream()
                .filter(Reservation::isExpired)
                .forEach(expiredRes -> {
                    // Mutate status context safely
                    Reservation cancelledRes = new Reservation(
                            expiredRes.id(), expiredRes.eventId(), expiredRes.customerEmail(),
                            ReservationStatus.CANCELLED, expiredRes.createdAt(), null, expiredRes.holdExpiresAt()
                    );
                    reservationTable.put(expiredRes.id(), cancelledRes);
                    // Evict bound asset fields so other customers can book immediately
                    seatAssignmentTable.remove(expiredRes.id());
                    System.out.printf("\n⏱️ [BACKGROUND SWEEPER] Evicted expired reservation hold: %s\n", expiredRes.id());
                });
    }

    public Map<String, Reservation> getReservationTable() {
        return Collections.unmodifiableMap(reservationTable);
    }

    public void shutdown() {
        backgroundSweeper.shutdown();
    }
}
