package com.ticket.domain;

import java.time.Instant;

public record Reservation(
        String id,
        String eventId,
        String customerEmail,
        ReservationStatus status,
        Instant createdAt,
        Instant confirmedAt,
        Instant holdExpiresAt
) {
    /**
     * Checks if a temporary hold has passed its expiration window.
     */
    public boolean isExpired() {
        return status == ReservationStatus.HOLD && holdExpiresAt != null && Instant.now().isAfter(holdExpiresAt);
    }
}
