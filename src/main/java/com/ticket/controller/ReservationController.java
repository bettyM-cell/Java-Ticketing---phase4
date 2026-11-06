package com.ticket.controller;

import com.ticket.domain.Reservation;
import com.ticket.domain.Seat;
import com.ticket.service.TicketingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final TicketingService ticketingService = new TicketingService();

    @GetMapping("/hold")
    public ResponseEntity<?> createSeatHold(
            @RequestParam String eventId,
            @RequestParam String seatId,
            @RequestParam String email) {

        Seat temporarySeat = new Seat(seatId, "V-01", "Orchestra", "Row-A", 1, Map.of());
        Reservation holdResult = ticketingService.placeHold(eventId, temporarySeat, email, 300);
        return ResponseEntity.ok(holdResult);
    }
}
