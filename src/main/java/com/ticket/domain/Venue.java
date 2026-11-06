package com.ticket.domain;

import java.time.ZoneId;

public record Venue(
        String id,
        String name,
        String address,
        ZoneId timezone
) {}
