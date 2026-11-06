package com.ticket.service;

import com.ticket.domain.Reservation;
import com.ticket.domain.ReservationSeat;
import com.ticket.domain.ReservationStatus;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportingEngine {

    /**
     * Reports total reservations per event.
     */
    public Map<String, Long> getReservationsCountPerEvent(List<Reservation> reservations) {
        return reservations.stream()
                .collect(Collectors.groupingBy(
                        Reservation::eventId,
                        Collectors.counting()
                ));
    }

    /**
     * Aggregates total booked seats across non-cancelled reservations.
     */
    public Map<String, Long> getSeatsBookedPerEvent(List<Reservation> reservations, List<ReservationSeat> reservationSeats) {
        List<String> validReservationIds = reservations.stream()
                .filter(r -> r.status() != ReservationStatus.CANCELLED)
                .map(Reservation::id)
                .toList();

        return reservationSeats.stream()
                .filter(rs -> validReservationIds.contains(rs.reservationId()))
                .collect(Collectors.groupingBy(
                        ReservationSeat::reservationId,
                        Collectors.counting()
                ));
    }
}
