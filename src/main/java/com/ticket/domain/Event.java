package com.ticket.domain;

import java.time.ZonedDateTime;

public record Event(
        String id,
        String venueId,
        String title,
        ZonedDateTime startAt,
        ZonedDateTime endAt,
        EventStatus status
) {}
