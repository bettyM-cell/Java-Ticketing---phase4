package com.ticket.domain;

public record ReservationSeat(
        String reservationId,
        String seatId,
        Money price,
        String discountCode
) {}
