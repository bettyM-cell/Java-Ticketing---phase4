package com.ticket.domain;

import java.util.Comparator;
import java.util.Map;

public record Seat(
        String id,
        String venueId,
        String section,
        String row,
        int number,
        Map<String, Object> attributes
) implements Comparable<Seat> {

    private static final Comparator<Seat> SEAT_COMPARATOR = Comparator
            .comparing(Seat::section)
            .thenComparing(Seat::row)
            .thenComparing(Seat::number);

    @Override
    public int compareTo(Seat other) {
        return SEAT_COMPARATOR.compare(this, other);
    }
}
